package com.klaudia.transactionsimporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TransactionsImporterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionsImporterApplication.class, args);
    }

}
