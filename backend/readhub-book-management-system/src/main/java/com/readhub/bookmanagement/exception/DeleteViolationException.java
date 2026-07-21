package com.readhub.bookmanagement.exception;

public class DeleteViolationException extends RuntimeException {
    public DeleteViolationException(String message) {
        super(message);
    }
}
