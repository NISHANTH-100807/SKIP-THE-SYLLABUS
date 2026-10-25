package com.skipthesyllabus.repository;

import com.skipthesyllabus.model.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
    List<LearningSession> findAllByOrderByStartedAtDesc();
    long countBySessionType(LearningSession.SessionType type);
}
