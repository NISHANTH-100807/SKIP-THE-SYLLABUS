package com.skipthesyllabus.repository;

import com.skipthesyllabus.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuizIdOrderByAttemptedAtDesc(Long quizId);
    List<QuizAttempt> findAllByOrderByAttemptedAtDesc();

    @Query("SELECT COUNT(a) FROM QuizAttempt a")
    long countAllAttempts();

    @Query("SELECT AVG(a.scorePercent) FROM QuizAttempt a")
    Optional<Double> findAverageScore();
}
