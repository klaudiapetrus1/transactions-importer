package com.klaudia.transactionsimporter.imports;

public enum ImportStatus {
    CREATED,
    PROCESSING,
    COMPLETED,
    FAILED,
    PARTIALLY_COMPLETED //when some transactions were rejected/had errors
}
