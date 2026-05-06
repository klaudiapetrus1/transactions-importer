package com.klaudia.transactionsimporter.exceptions;

public class DuplicatedImportJobException extends RuntimeException {
    public DuplicatedImportJobException(String message) {
        super(message);
    }
}
