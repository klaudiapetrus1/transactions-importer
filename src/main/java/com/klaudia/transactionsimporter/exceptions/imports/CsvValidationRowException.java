package com.klaudia.transactionsimporter.exceptions.imports;

public class CsvValidationRowException extends RuntimeException {
    public CsvValidationRowException(String message) {
        super(message);
    }
}
