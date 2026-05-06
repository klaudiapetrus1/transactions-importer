package com.klaudia.transactionsimporter.exceptions;

public class IllegalFileFormatException extends RuntimeException {
    public IllegalFileFormatException(String message) {
        super(message);
    }
}
