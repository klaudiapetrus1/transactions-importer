package com.klaudia.transactionsimporter.imports.model;

public enum ImportStatus {
    CREATED,
    PROCESSING,
    COMPLETED,
    FAILED,
    PARTIALLY_COMPLETED //when some transactions were rejected because of errors
}
