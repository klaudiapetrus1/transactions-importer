package com.klaudia.transactionsimporter.imports.dto;

import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.model.ImportStatus;

public record ImportJobResponse(String id, ImportStatus importStatus, int totalProcessedRows) {

    public static ImportJobResponse from(ImportJob importJob) {
        return new ImportJobResponse(importJob.getId(), importJob.getStatus(), importJob.getTotalProcessedRows());
    }
}
