package com.klaudia.transactionsimporter.exceptions.imports;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException(String message) {
        super(message);
    }
}
