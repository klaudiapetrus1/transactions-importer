package com.klaudia.transactionsimporter.exceptions.imports;

public class IllegalFileFormatException extends RuntimeException {
    public IllegalFileFormatException(String message) {
        super(message);
    }
}
