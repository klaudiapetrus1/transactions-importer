package com.klaudia.transactionsimporter.imports.service;

import com.klaudia.transactionsimporter.exceptions.imports.ImportJobNotFoundException;
import com.klaudia.transactionsimporter.imports.ImportRepository;
import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.model.ImportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportJobUpdaterTest {

    @Mock
    private ImportRepository importRepository;

    @InjectMocks
    private ImportJobUpdater importJobUpdater;

    private ImportJob existingJob;
    private String jobId = "job-123";

    @BeforeEach
    void setUp() {
        existingJob = ImportJob.builder()
                .id(jobId)
                .filename("test.csv")
                .status(ImportStatus.CREATED)
                .totalProcessedRows(0)
                .successRows(0)
                .failedRows(0)
                .errors(Collections.emptyList())
                .build();
    }

    @Test
    void updateProgress_shouldUpdateFields_whenJobExistsAndNotFinished() {
        // Given
        ImportJob updateInfo = ImportJob.builder()
                .id(jobId)
                .totalProcessedRows(10)
                .successRows(8)
                .failedRows(2)
                .errors(List.of("Error 1", "Error 2"))
                .build();

        when(importRepository.findById(jobId)).thenReturn(Optional.of(existingJob));

        // When
        importJobUpdater.updateProgress(updateInfo, false);

        // Then
        ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
        verify(importRepository).save(captor.capture());
        ImportJob savedJob = captor.getValue();

        assertEquals(10, savedJob.getTotalProcessedRows());
        assertEquals(8, savedJob.getSuccessRows());
        assertEquals(2, savedJob.getFailedRows());
        assertEquals(2, savedJob.getErrors().size());
        assertNull(savedJob.getFinishedAt());
        assertEquals(ImportStatus.CREATED, savedJob.getStatus());
    }

    @Test
    void updateProgress_shouldSetStatusCompleted_whenFinishedAndNoFailures() {
        // Given
        ImportJob updateInfo = ImportJob.builder()
                .id(jobId)
                .totalProcessedRows(10)
                .successRows(10)
                .failedRows(0)
                .errors(Collections.emptyList())
                .build();

        when(importRepository.findById(jobId)).thenReturn(Optional.of(existingJob));

        // When
        importJobUpdater.updateProgress(updateInfo, true);

        // Then
        ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
        verify(importRepository).save(captor.capture());
        ImportJob savedJob = captor.getValue();

        assertEquals(ImportStatus.COMPLETED, savedJob.getStatus());
        assertNotNull(savedJob.getFinishedAt());
    }

    @Test
    void updateProgress_shouldSetStatusPartiallyCompleted_whenFinishedAndHasFailures() {
        // Given
        ImportJob updateInfo = ImportJob.builder()
                .id(jobId)
                .totalProcessedRows(10)
                .successRows(9)
                .failedRows(1)
                .errors(List.of("Error 1"))
                .build();

        when(importRepository.findById(jobId)).thenReturn(Optional.of(existingJob));

        // When
        importJobUpdater.updateProgress(updateInfo, true);

        // Then
        ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
        verify(importRepository).save(captor.capture());
        ImportJob savedJob = captor.getValue();

        assertEquals(ImportStatus.PARTIALLY_COMPLETED, savedJob.getStatus());
        assertNotNull(savedJob.getFinishedAt());
    }

    @Test
    void updateProgress_shouldThrowException_whenJobNotFound() {
        // Given
        ImportJob updateInfo = ImportJob.builder().id(jobId).build();
        when(importRepository.findById(jobId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ImportJobNotFoundException.class, () -> importJobUpdater.updateProgress(updateInfo, false));
        verify(importRepository, never()).save(any());
    }

    @Test
    void markAsFailed_shouldUpdateStatusToFailed_whenJobExists() {
        // Given
        ImportJob failedInfo = ImportJob.builder()
                .id(jobId)
                .errors(List.of("Critical error"))
                .build();

        when(importRepository.findById(jobId)).thenReturn(Optional.of(existingJob));

        // When
        importJobUpdater.markAsFailed(failedInfo);

        // Then
        ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
        verify(importRepository).save(captor.capture());
        ImportJob savedJob = captor.getValue();

        assertEquals(ImportStatus.FAILED, savedJob.getStatus());
        assertNotNull(savedJob.getFinishedAt());
        assertEquals(List.of("Critical error"), savedJob.getErrors());
    }

    @Test
    void markAsFailed_shouldThrowException_whenJobNotFound() {
        // Given
        ImportJob failedInfo = ImportJob.builder().id(jobId).build();
        when(importRepository.findById(jobId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ImportJobNotFoundException.class, () -> importJobUpdater.markAsFailed(failedInfo));
        verify(importRepository, never()).save(any());
    }
}
