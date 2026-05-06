package com.klaudia.transactionsimporter.exceptions;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException(String message) {
        super(message);
    }
}
