package com.klaudia.transactionsimporter.imports.service;

import com.klaudia.transactionsimporter.exceptions.imports.DuplicatedImportJobException;
import com.klaudia.transactionsimporter.exceptions.imports.EmptyFileException;
import com.klaudia.transactionsimporter.exceptions.imports.IllegalFileFormatException;
import com.klaudia.transactionsimporter.exceptions.imports.ImportJobNotFoundException;
import com.klaudia.transactionsimporter.imports.ImportRepository;
import com.klaudia.transactionsimporter.imports.dto.ImportJobResponse;
import com.klaudia.transactionsimporter.imports.dto.ImportJobStatusResponse;
import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.model.ImportStatus;
import com.klaudia.transactionsimporter.imports.parser.CSVParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private CSVParser csvParser;

    @Mock
    private ImportRepository importRepository;

    @InjectMocks
    private ImportService importService;

    @Test
    void importCsv_ShouldReturnResponse_WhenFileIsValid() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(file.getSize()).thenReturn(100L);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});

        when(importRepository.existsByFilenameAndFileSize("test.csv", 100L)).thenReturn(false);
        
        ImportJob savedJob = ImportJob.builder()
                .id("job-123")
                .filename("test.csv")
                .status(ImportStatus.CREATED)
                .build();
        
        when(importRepository.save(any(ImportJob.class))).thenReturn(savedJob);

        // When
        ImportJobResponse response = importService.importCsv(file);

        // Then
        assertNotNull(response);
        assertEquals("job-123", response.id());
        assertEquals(ImportStatus.PROCESSING, response.importStatus());
        verify(csvParser).parse(any(byte[].class), eq("job-123"));
        verify(importRepository, times(2)).save(any(ImportJob.class));
    }

    @Test
    void importCsv_ShouldThrowEmptyFileException_WhenFileIsEmpty() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // When & Then
        assertThrows(EmptyFileException.class, () -> importService.importCsv(file));
    }

    @Test
    void importCsv_ShouldThrowIllegalFileFormatException_WhenFileIsNotCsv() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");

        // When & Then
        assertThrows(IllegalFileFormatException.class, () -> importService.importCsv(file));
    }

    @Test
    void importCsv_ShouldThrowDuplicatedImportJobException_WhenFileAlreadyImported() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(file.getSize()).thenReturn(100L);

        when(importRepository.existsByFilenameAndFileSize("test.csv", 100L)).thenReturn(true);

        // When & Then
        assertThrows(DuplicatedImportJobException.class, () -> importService.importCsv(file));
    }

    @Test
    void importCsv_ShouldThrowEmptyFileException_WhenIOExceptionOccurs() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(file.getSize()).thenReturn(100L);
        when(file.getBytes()).thenThrow(new IOException("Read error"));

        when(importRepository.existsByFilenameAndFileSize("test.csv", 100L)).thenReturn(false);
        
        ImportJob savedJob = ImportJob.builder()
                .id("job-123")
                .build();
        when(importRepository.save(any(ImportJob.class))).thenReturn(savedJob);

        // When & Then
        assertThrows(EmptyFileException.class, () -> importService.importCsv(file));
    }

    @Test
    void checkImportStatus_ShouldReturnStatus_WhenJobExists() {
        // Given
        String jobId = "job-123";
        ImportJob job = ImportJob.builder()
                .id(jobId)
                .status(ImportStatus.PROCESSING)
                .totalProcessedRows(10)
                .successRows(8)
                .failedRows(2)
                .build();
        
        when(importRepository.findById(jobId)).thenReturn(Optional.of(job));

        // When
        ImportJobStatusResponse response = importService.checkImportStatus(jobId);

        // Then
        assertNotNull(response);
        assertEquals(jobId, response.id());
        assertEquals(ImportStatus.PROCESSING, response.status());
        assertEquals(10, response.totalProcessedRows());
    }

    @Test
    void checkImportStatus_ShouldThrowImportJobNotFoundException_WhenJobDoesNotExist() {
        // Given
        String jobId = "non-existent";
        when(importRepository.findById(jobId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ImportJobNotFoundException.class, () -> importService.checkImportStatus(jobId));
    }
}
