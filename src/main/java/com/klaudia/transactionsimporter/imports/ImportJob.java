package com.klaudia.transactionsimporter.imports;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
@Data
@Builder
@Document("import_jobs")
@CompoundIndex(name = "filename_size_idx" ,def = "{'filename': 1, 'fileSize': 1}")
public class ImportJob {
    @Id
    private String id;
    private String filename;
    @Indexed
    private ImportStatus status;
    @Indexed
    private Instant createdAt;
    private Instant finishedAt;
    private int totalRows;
    private int successRows;
    private int failedRows;
    private long fileSize;
    private List<String> errors;
}
