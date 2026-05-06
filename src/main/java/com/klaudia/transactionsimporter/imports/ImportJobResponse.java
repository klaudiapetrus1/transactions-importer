package com.klaudia.transactionsimporter.imports;

public record ImportJobResponse(String id, ImportStatus importStatus) {

    public static ImportJobResponse from(ImportJob importJob) {
        return new ImportJobResponse(importJob.getId(), importJob.getStatus());
    }
}
