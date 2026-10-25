package com.skipthesyllabus.repository;

import com.skipthesyllabus.model.DocumentSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentSectionRepository extends JpaRepository<DocumentSection, Long> {
    List<DocumentSection> findByDocumentIdOrderByPageNumberAscSectionIndexAsc(Long documentId);
    void deleteByDocumentId(Long documentId);
}
