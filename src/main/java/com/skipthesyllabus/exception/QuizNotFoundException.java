package com.skipthesyllabus.exception;

public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException(Long id) {
        super("Quiz not found: " + id);
    }
}
