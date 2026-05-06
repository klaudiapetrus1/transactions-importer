package com.klaudia.transactionsimporter.imports;

import com.klaudia.transactionsimporter.exceptions.DuplicatedImportJobException;
import com.klaudia.transactionsimporter.exceptions.EmptyFileException;
import com.klaudia.transactionsimporter.exceptions.IllegalFileFormatException;
import com.klaudia.transactionsimporter.exceptions.ImportJobNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        csvParser.parse(file);
        return ImportJobResponse.from(importJob);
    }

    private ImportJob saveInitialImportJob(MultipartFile file) {
        ImportJob importJob = ImportJob.builder()
                .filename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .status(ImportStatus.CREATED)
                .createdAt(Instant.now())
                .totalRows(0)
                .successRows(0)
                .failedRows(0)
                .errors(List.of())
                .build();

        return importRepository.save(importJob);
    }

    private boolean checkForDuplicate(MultipartFile file) {
        return importRepository.existsByFilenameAndFileSize(file.getOriginalFilename(), file.getSize());
    }

    public ImportJobResponse checkImportStatus(String id) {
        Optional<ImportJob> foundImportJob = importRepository.findById(id);
        if (foundImportJob.isPresent()) {
            ImportJob importJob = foundImportJob.get();
            return ImportJobResponse.from(importJob);
        } else throw new ImportJobNotFoundException("There is no import job with id: " + id);
    }
}
