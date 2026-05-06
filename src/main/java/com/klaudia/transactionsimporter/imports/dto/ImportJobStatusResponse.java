package com.klaudia.transactionsimporter.imports.dto;

import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.model.ImportStatus;

import java.util.List;

public record ImportJobStatusResponse(
        String id,
        ImportStatus status,
        int totalProcessedRows,
        int successRows,
        int failedRows,
        List<String> errors
        ) {

    public static ImportJobStatusResponse from(ImportJob importJob) {
        return new ImportJobStatusResponse(
                importJob.getId(),
                importJob.getStatus(),
                importJob.getTotalProcessedRows(),
                importJob.getSuccessRows(),
                importJob.getFailedRows(),
                importJob.getErrors());
    }
}
