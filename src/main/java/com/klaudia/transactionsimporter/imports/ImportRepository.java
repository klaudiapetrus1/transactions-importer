package com.klaudia.transactionsimporter.imports;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportRepository extends MongoRepository<ImportJob, String> {
    boolean existsByFilenameAndFileSize(String filename, long fileSize);

}
