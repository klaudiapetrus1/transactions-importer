package com.klaudia.transactionsimporter.imports.parser;

import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.service.ImportJobUpdater;
import com.klaudia.transactionsimporter.transactions.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CSVParserTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ImportJobUpdater importJobUpdater;

    @InjectMocks
    private CSVParser csvParser;

    @Test
    void parse_ShouldProcessValidCsv_Successfully() {
        // Given
        String jobId = "job-123";
        // Simple valid Belgian IBAN (shorter, less room for error)
        String validIban = "BE68539007547034";
        String csvContent = "iban,title,date,currency,category,amount\n" +
                validIban + ",Test Transaction,2024-05-08,EUR,FOOD,100.50";
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        // When
        csvParser.parse(bytes, jobId);

        // Then
        verify(transactionRepository).saveAll(anyList());
        verify(importJobUpdater).updateProgress(argThat(job -> 
            job.getId().equals(jobId) && 
            job.getSuccessRows() == 1 && 
            job.getTotalProcessedRows() == 1 &&
            job.getErrors().isEmpty()
        ), eq(true));
    }

    @Test
    void parse_ShouldHandleValidationErrors_AndReportThem() {
        // Given
        String jobId = "job-123";
        String validIban = "BE68539007547034";
        String csvContent = "iban,title,date,currency,category,amount\n" +
                "INVALID_IBAN,Title,2024-05-08,EUR,FOOD,100.00\n" + // Invalid IBAN
                validIban + ",Title,invalid-date,EUR,FOOD,100.00\n" + // Invalid Date
                validIban + ",Title,2024-05-08,XYZ,FOOD,100.00\n" + // Invalid Currency
                validIban + ",Title,2024-05-08,EUR,,100.00\n" + // Blank Category
                validIban + ",Title,2024-05-08,EUR,FOOD,0.00\n" + // Zero Amount
                validIban + ",Title,2024-05-08,EUR,FOOD,not-a-number"; // Invalid Amount
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        // When
        csvParser.parse(bytes, jobId);

        // Then
        verify(transactionRepository, never()).saveAll(anyList());
        ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
        verify(importJobUpdater).updateProgress(captor.capture(), eq(true));

        ImportJob result = captor.getValue();
        assertEquals(0, result.getSuccessRows());
        assertEquals(6, result.getFailedRows());
        assertEquals(6, result.getErrors().size());
        
        // Note: CSVParser has "Row" + rowNumber (no space) for IBAN error
        assertTrue(result.getErrors().get(0).contains("Invalid IBAN"));
        assertTrue(result.getErrors().get(1).contains("invalid date format"));
        assertTrue(result.getErrors().get(2).contains("unknown currency"));
        assertTrue(result.getErrors().get(3).contains("category is blank"));
        assertTrue(result.getErrors().get(4).contains("amount cannot be zero"));
        assertTrue(result.getErrors().get(5).contains("invalid amount"));
    }

    @Test
    void parse_ShouldHandleBatching_Correctly() {
        // Given
        String jobId = "job-123";
        String validIban = "BE68539007547034";
        StringBuilder csvBuilder = new StringBuilder("iban,title,date,currency,category,amount\n");
        for (int i = 0; i < 501; i++) {
            csvBuilder.append(validIban).append(",Title,2024-05-08,EUR,FOOD,10.00\n");
        }
        byte[] bytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);

        // When
        csvParser.parse(bytes, jobId);

        // Then
        verify(transactionRepository, times(2)).saveAll(anyList()); // 500 + 1
        verify(importJobUpdater).updateProgress(argThat(job -> job.getSuccessRows() == 500), eq(false));
        verify(importJobUpdater).updateProgress(argThat(job -> job.getSuccessRows() == 501), eq(true));
    }

    @Test
    void parse_ShouldHandleWrongColumnCount() {
        // Given
        String jobId = "job-123";
        String csvContent = "iban,title,date,currency,category,amount\n" +
                "PL89101010101010101010101010,Title,2024-05-08,EUR,FOOD"; // Only 5 columns
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        // When
        csvParser.parse(bytes, jobId);

        // Then
        verify(importJobUpdater).updateProgress(argThat(job ->
                job.getFailedRows() == 1 &&
                        job.getErrors().get(0).contains("expected 6 columns")
        ), eq(true));
    }

    @Test
    void parse_ShouldCallMarkAsFailed_OnCsvParsingError() {
        // Given
        String jobId = "job-123";
        // Malformed CSV (e.g., unclosed quote)
        String csvContent = "iban,title,date,currency,category,amount\n" +
                "\"PL89101010101010101010101010,Title,2024-05-08,EUR,FOOD,100.00";
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        // When
        csvParser.parse(bytes, jobId);

        // Then
        verify(importJobUpdater).markAsFailed(argThat(job -> job.getId().equals(jobId)));
    }
}