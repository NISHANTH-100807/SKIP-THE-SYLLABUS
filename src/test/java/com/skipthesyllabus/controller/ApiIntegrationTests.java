package com.skipthesyllabus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skipthesyllabus.dto.ExplainRequestDTO;
import com.skipthesyllabus.dto.QuizRequestDTO;
import com.skipthesyllabus.dto.QuizSubmitDTO;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.PDFDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "xai.api.key=",
    "spring.datasource.url=jdbc:h2:mem:apitestdb;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PDFDocumentRepository docRepo;

    @Autowired
    private com.skipthesyllabus.repository.QuizAttemptRepository attemptRepo;

    @Autowired
    private com.skipthesyllabus.repository.MCQQuestionRepository questionRepo;

    @Autowired
    private com.skipthesyllabus.repository.QuizRepository quizRepo;

    @Autowired
    private com.skipthesyllabus.repository.LearningSessionRepository sessionRepo;

    @Autowired
    private com.skipthesyllabus.repository.ConceptRepository conceptRepo;

    @Autowired
    private com.skipthesyllabus.repository.DocumentSectionRepository sectionRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private PDFDocument testDoc;

    @BeforeEach
    void setup() {
        attemptRepo.deleteAll();
        questionRepo.deleteAll();
        quizRepo.deleteAll();
        sessionRepo.deleteAll();
        conceptRepo.deleteAll();
        sectionRepo.deleteAll();
        docRepo.deleteAll();

        testDoc = new PDFDocument("data_structures.pdf", "data_structures.pdf", 2048L);
        testDoc.markAsReady(
            "Binary Search Tree is a binary tree data structure where each node has at most two children. " +
            "For every node, the key in the left child is strictly smaller, and the key in the right child is greater. " +
            "In-order traversal of a BST yields elements in sorted ascending order. " +
            "Bubble sort is an O(n^2) sorting algorithm that repeatedly swaps adjacent elements.",
            3
        );
        testDoc = docRepo.save(testDoc);
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.aiMode").value("local"));
    }

    @Test
    void testSettingsStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/settings/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.aiConfigured").value(false))
            .andExpect(jsonPath("$.data.mode").value("Local / Demo"));
    }

    @Test
    void testListDocuments() throws Exception {
        mockMvc.perform(get("/api/documents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.data[0].originalFilename").value("data_structures.pdf"));
    }

    @Test
    void testGetDocumentById() throws Exception {
        mockMvc.perform(get("/api/documents/" + testDoc.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(testDoc.getId()))
            .andExpect(jsonPath("$.data.status").value("READY"));
    }

    @Test
    void testGetDocumentSections() throws Exception {
        mockMvc.perform(get("/api/documents/" + testDoc.getId() + "/sections"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testExplainConceptInLocalMode() throws Exception {
        ExplainRequestDTO req = new ExplainRequestDTO();
        req.setConcept("Binary Search Tree");
        req.setLevel("beginner");

        mockMvc.perform(post("/api/documents/" + testDoc.getId() + "/explain")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.concept").value("Binary Search Tree"))
            .andExpect(jsonPath("$.data.level").value("beginner"))
            .andExpect(jsonPath("$.data.intuition").isNotEmpty())
            .andExpect(jsonPath("$.data.breakdown").isNotEmpty())
            .andExpect(jsonPath("$.data.aiGenerated").value(false));
    }

    @Test
    void testGenerateAndSubmitQuiz() throws Exception {
        QuizRequestDTO quizReq = new QuizRequestDTO();
        quizReq.setQuestionCount(3);
        quizReq.setDifficulty("medium");

        // 1. Generate Quiz
        MvcResult genResult = mockMvc.perform(post("/api/documents/" + testDoc.getId() + "/quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(quizReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").isNumber())
            .andExpect(jsonPath("$.data.questions", hasSize(greaterThanOrEqualTo(1))))
            .andReturn();

        String resStr = genResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(resStr).path("data");
        Long quizId = rootNode.path("id").asLong();
        Long questionId = rootNode.path("questions").get(0).path("id").asLong();

        // 2. Submit Quiz Answers
        QuizSubmitDTO submitReq = new QuizSubmitDTO();
        submitReq.setAnswers(Map.of(String.valueOf(questionId), "A"));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.quizId").value(quizId))
            .andExpect(jsonPath("$.data.scorePercent").isNumber())
            .andExpect(jsonPath("$.data.scoreMessage").isNotEmpty());

        // 3. Verify Quiz History endpoint
        mockMvc.perform(get("/api/quizzes/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testProgressEndpoint() throws Exception {
        mockMvc.perform(get("/api/progress"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalDocuments").isNumber());
    }

    @Test
    void testUploadInvalidFileTypeRejected() throws Exception {
        MockMultipartFile badFile = new MockMultipartFile(
            "file", "malicious.exe", "application/octet-stream", "MZ...not a pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/upload").file(badFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
