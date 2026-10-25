package com.skipthesyllabus.repository;

import com.skipthesyllabus.model.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, Long> {
    List<Concept> findByDocumentId(Long documentId);
    java.util.Optional<Concept> findByNameAndDocumentId(String name, Long documentId);

    @Query("SELECT c FROM Concept c WHERE c.incorrectCount > 0 ORDER BY c.incorrectCount DESC")
    List<Concept> findWeakConcepts();
}
