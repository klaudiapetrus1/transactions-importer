package com.klaudia.transactionsimporter.exceptions.imports;

public class DuplicatedImportJobException extends RuntimeException {
    public DuplicatedImportJobException(String message) {
        super(message);
    }
}
