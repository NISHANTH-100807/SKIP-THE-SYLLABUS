package com.skipthesyllabus.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(Long id) {
        super("Document not found: " + id);
    }
}
