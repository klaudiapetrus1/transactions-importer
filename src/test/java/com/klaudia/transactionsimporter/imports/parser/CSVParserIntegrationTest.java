package com.klaudia.transactionsimporter.imports.parser;

import com.klaudia.transactionsimporter.imports.ImportRepository;
import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.model.ImportStatus;
import com.klaudia.transactionsimporter.transactions.Transaction;
import com.klaudia.transactionsimporter.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class CSVParserIntegrationTest {

    @Autowired
    private CSVParser csvParser;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ImportRepository importRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        importRepository.deleteAll();
    }

    @Test
    void parse_shouldSaveTransactionsAndUpdateJobStatus() {
        // Given
        // Using a valid German IBAN for validation
        // DE89370400440532013000 is a known valid test IBAN
        String validIban = "DE89370400440532013000";
        String csvContent = "IBAN,Title,Date,Currency,Category,Amount\n" +
                validIban + ",Title 1,2023-01-01,PLN,FOOD,100.50\n" +
                validIban + ",Title 2,2023-01-02,PLN,FUEL,200.00";
        byte[] fileBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        ImportJob job = ImportJob.builder()
                .id("job-1")
                .filename("test.csv")
                .status(ImportStatus.CREATED)
                .build();
        importRepository.save(job);

        // When
        csvParser.parse(fileBytes, "job-1");

        // Then
        // Since CSVParser.parse is @Async, we need to wait for it to finish
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Transaction> transactions = transactionRepository.findAll();
            assertEquals(2, transactions.size());

            ImportJob updatedJob = importRepository.findById("job-1").orElseThrow();
            assertEquals(ImportStatus.COMPLETED, updatedJob.getStatus());
            assertEquals(2, updatedJob.getSuccessRows());
            assertEquals(0, updatedJob.getFailedRows());
        });
    }

    @Test
    void parse_shouldHandleInvalidRows() {
        // Given
        String validIban = "DE89370400440532013000";
        String csvContent = "IBAN,Title,Date,Currency,Category,Amount\n" +
                "INVALID_IBAN,Title 1,2023-01-01,PLN,FOOD,100.50\n" +
                validIban + ",Title 2,2023-01-02,PLN,FUEL,200.00";
        byte[] fileBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        ImportJob job = ImportJob.builder()
                .id("job-2")
                .filename("test_invalid.csv")
                .status(ImportStatus.CREATED)
                .build();
        importRepository.save(job);

        // When
        csvParser.parse(fileBytes, "job-2");

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Transaction> transactions = transactionRepository.findAll();
            assertEquals(1, transactions.size());

            ImportJob updatedJob = importRepository.findById("job-2").orElseThrow();
            assertEquals(ImportStatus.PARTIALLY_COMPLETED, updatedJob.getStatus());
            assertEquals(1, updatedJob.getSuccessRows());
            assertEquals(1, updatedJob.getFailedRows());
            assertFalse(updatedJob.getErrors().isEmpty());
        });
    }
}
