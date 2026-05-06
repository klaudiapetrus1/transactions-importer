package com.klaudia.transactionsimporter.imports.service;

import com.klaudia.transactionsimporter.exceptions.imports.ImportJobNotFoundException;
import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.ImportRepository;
import com.klaudia.transactionsimporter.imports.model.ImportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportJobUpdater {
    private final ImportRepository importRepository;

    public void updateProgress(ImportJob importJob, boolean finished) {
        Optional<ImportJob> foundImportJob = importRepository.findById(importJob.getId());
        if (foundImportJob.isEmpty()) {
            throw new ImportJobNotFoundException("No import job found with id: " + importJob.getId());
        }
        ImportJob importJobToUpdate = foundImportJob.get();
        importJobToUpdate.setTotalProcessedRows(importJob.getTotalProcessedRows());
        importJobToUpdate.setSuccessRows(importJob.getSuccessRows());
        importJobToUpdate.setFailedRows(importJob.getFailedRows());
        importJobToUpdate.setErrors(new ArrayList<>(importJob.getErrors()));

        if (finished) {
            importJobToUpdate.setFinishedAt(Instant.now());
            importJobToUpdate.setStatus(importJob.getFailedRows() == 0 ? ImportStatus.COMPLETED : ImportStatus.PARTIALLY_COMPLETED);
        }

        importRepository.save(importJobToUpdate);

    }

    public void markAsFailed(ImportJob failedImportJobToUpdate) {
        Optional<ImportJob> foundImportJob = importRepository.findById(failedImportJobToUpdate.getId());
        if (foundImportJob.isEmpty()) {
            throw new ImportJobNotFoundException("No import job found with id: " + failedImportJobToUpdate.getId());
        }
        ImportJob importJobToUpdate = foundImportJob.get();
        importJobToUpdate.setStatus(ImportStatus.FAILED);
        importJobToUpdate.setFinishedAt(Instant.now());
        importJobToUpdate.setErrors(new ArrayList<>(failedImportJobToUpdate.getErrors()));
        importRepository.save(importJobToUpdate);
    }
}
