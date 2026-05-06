package com.klaudia.transactionsimporter.exceptions.imports;

public class ImportJobNotFoundException extends RuntimeException {
    public ImportJobNotFoundException(String message) {
        super(message);
    }
}
