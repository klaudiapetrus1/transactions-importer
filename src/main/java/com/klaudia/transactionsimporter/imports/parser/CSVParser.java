package com.klaudia.transactionsimporter.imports.parser;

import com.klaudia.transactionsimporter.exceptions.imports.CsvValidationRowException;
import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.service.ImportJobUpdater;
import com.klaudia.transactionsimporter.transactions.Currency;
import com.klaudia.transactionsimporter.transactions.Transaction;
import com.klaudia.transactionsimporter.transactions.TransactionRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.IbanUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CSVParser {

    private static final int BATCH_SIZE = 500;
    private final TransactionRepository transactionRepository;
    private final ImportJobUpdater importJobUpdater;

    @Async
    public void parse(byte[] fileBytes, String importJobId) {
        List<String> errors = new ArrayList<>();
        List<Transaction> batch = new ArrayList<>(BATCH_SIZE);
        int totalRows = 0;
        int successRows = 0;
        int failedRows = 0;

        try (Reader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withSkipLines(1)
                     .build()) {

            String[] line;
            int rowNumber = 1;

            while ((line = csvReader.readNext()) != null) {
                totalRows++;
                rowNumber++;

                try {
                    Transaction transaction = mapAndValidate(line, rowNumber, importJobId);
                    batch.add(transaction);
                    successRows++;
                } catch (CsvValidationRowException e) {
                    //for now, we ignore not valid transactions, but if we need their details in the future we could save them in different collection
                    failedRows++;
                    errors.add(e.getMessage());
                    log.warn("Validation failed at row {}: {}", rowNumber, e.getMessage());
                }

                if (batch.size() == BATCH_SIZE) {
                    transactionRepository.saveAll(batch);
                    batch.clear();
                    ImportJob importJobToUpdate = ImportJob.builder()
                            .id(importJobId)
                            .totalProcessedRows(totalRows)
                            .successRows(successRows)
                            .failedRows(failedRows)
                            .errors(new ArrayList<>(errors))
                            .build();

                    importJobUpdater.updateProgress(importJobToUpdate, false);
                }
            }

            if (!batch.isEmpty()) {
                transactionRepository.saveAll(batch);
            }

        } catch (IOException | CsvValidationException e) {
            log.error("Error while parsing CSV for job {}: {}", importJobId, e.getMessage());
            ImportJob failedImportJobToUpdate = ImportJob.builder()
                    .id(importJobId)
                    .errors(new ArrayList<>(errors))
                    .build();
            importJobUpdater.markAsFailed(failedImportJobToUpdate);
            return;
        }
        ImportJob finalImportJobToUpdate = ImportJob.builder()
                .id(importJobId)
                .totalProcessedRows(totalRows)
                .successRows(successRows)
                .failedRows(failedRows)
                .finishedAt(Instant.now())
                .errors(new ArrayList<>(errors))
                .build();

        importJobUpdater.updateProgress(finalImportJobToUpdate, true);
    }

    private Transaction mapAndValidate(String[] line, int rowNumber, String importJobId) {
        if (line.length < 6) {
            throw new CsvValidationRowException(
                    "Row " + rowNumber + ": expected 6 columns, got " + line.length
            );
        }
        String iban = line[0].trim();
        String title = line[1].trim();
        String dateStr = line[2].trim();
        String currency = line[3].trim();
        String category = line[4].trim();
        String amountStr = line[5].trim();

        //IbanUtil.validate() method can also be used instead if we would like to know what exactly went wrong
        boolean validIban = IbanUtil.isValid(iban);
        if (!validIban) throw new CsvValidationRowException("Invalid IBAN: " + iban);

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new CsvValidationRowException("Row " + rowNumber + ": invalid date format: " + dateStr);
        }
        Currency parsedCurrency;
        try {
            parsedCurrency = Currency.valueOf(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CsvValidationRowException("Row " + rowNumber + ": unknown currency: " + currency);
        }

        if (category.isBlank()) {
            throw new CsvValidationRowException("Row " + rowNumber + ": category is blank");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            throw new CsvValidationRowException("Row " + rowNumber + ": invalid amount: " + amountStr);
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new CsvValidationRowException("Row " + rowNumber + ": amount cannot be zero");
        }

        return Transaction.builder()
                .iban(iban)
                .transactionDate(date)
                .currency(parsedCurrency)
                .category(category)
                .amount(amount)
                .title(title)
                .month(date.getMonthValue())
                .year(date.getYear())
                .importJobId(importJobId)
                .build();
    }

}