package com.klaudia.transactionsimporter.transactions;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document("transactions")
public class Transaction {
    @Id
    private String id;
    private String importJobId;
    private String iban;
    private LocalDate transactionDate;
    private Integer year;
    private Integer month;
    private Currency currency;
    private String category;
    private BigDecimal amount;
}
