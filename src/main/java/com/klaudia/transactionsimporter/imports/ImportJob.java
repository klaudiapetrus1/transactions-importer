package com.klaudia.transactionsimporter.imports;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("import_jobs")
public class ImportJob {
    @Id
    private String id;
    private String filename;
    private ImportStatus status;
    private Instant createdAt;
    private Instant finishedAt;
    private int totalRows;
    private int successRows;
    private int failedRows;
    private List<String> errors;
}
