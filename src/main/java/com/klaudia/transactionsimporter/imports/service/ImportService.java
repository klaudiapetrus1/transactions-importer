package com.klaudia.transactionsimporter.imports.service;

import com.klaudia.transactionsimporter.exceptions.imports.DuplicatedImportJobException;
import com.klaudia.transactionsimporter.exceptions.imports.EmptyFileException;
import com.klaudia.transactionsimporter.exceptions.imports.IllegalFileFormatException;
import com.klaudia.transactionsimporter.exceptions.imports.ImportJobNotFoundException;
import com.klaudia.transactionsimporter.imports.*;
import com.klaudia.transactionsimporter.imports.dto.ImportJobResponse;
import com.klaudia.transactionsimporter.imports.dto.ImportJobStatusResponse;
import com.klaudia.transactionsimporter.imports.model.ImportJob;
import com.klaudia.transactionsimporter.imports.model.ImportStatus;
import com.klaudia.transactionsimporter.imports.parser.CSVParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final CSVParser csvParser;
    private final ImportRepository importRepository;

    public ImportJobResponse importCsv(MultipartFile file) {
        if (file.isEmpty()) throw new EmptyFileException("The file is empty");
        if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
            throw new IllegalFileFormatException("Different file format then CSV is not supported");
        }
        if (checkForDuplicate(file)) {
            throw new DuplicatedImportJobException("The file you are trying to import was already imported");
        }
        ImportJob importJob = saveInitialImportJob(file);
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new EmptyFileException("Could not read file content");
        }
        csvParser.parse(fileBytes, importJob.getId());
        importJob.setStatus(ImportStatus.PROCESSING);
        importRepository.save(importJob);
        return ImportJobResponse.from(importJob);
    }

    private ImportJob saveInitialImportJob(MultipartFile file) {
        ImportJob importJob = ImportJob.builder()
                .filename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .status(ImportStatus.CREATED)
                .createdAt(Instant.now())
                .totalProcessedRows(0)
                .successRows(0)
                .failedRows(0)
                .errors(List.of())
                .build();

        return importRepository.save(importJob);
    }

    private boolean checkForDuplicate(MultipartFile file) {
        return importRepository.existsByFilenameAndFileSize(file.getOriginalFilename(), file.getSize());
    }

    public ImportJobStatusResponse checkImportStatus(String id) {
        Optional<ImportJob> foundImportJob = importRepository.findById(id);
        if (foundImportJob.isEmpty()) throw new ImportJobNotFoundException("There is no import job with id: " + id);
        ImportJob importJob = foundImportJob.get();
        return ImportJobStatusResponse.from(importJob);

    }
}
