package com.klaudia.transactionsimporter.imports;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImportRepository extends MongoRepository<ImportJob, String> {
}
