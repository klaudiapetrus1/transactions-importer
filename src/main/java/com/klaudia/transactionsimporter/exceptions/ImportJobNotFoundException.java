package com.klaudia.transactionsimporter.exceptions;

public class ImportJobNotFoundException extends RuntimeException {
    public ImportJobNotFoundException(String message) {
        super(message);
    }
}
