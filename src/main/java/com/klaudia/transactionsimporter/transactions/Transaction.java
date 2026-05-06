package com.klaudia.transactionsimporter.transactions;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Document("transactions")
@CompoundIndex(name = "year_month_idx", def = "{'year': 1, 'month': 1}")
public class Transaction {
    @Id
    private String id;
    private String importJobId;
    private String title;
    @Indexed
    private String iban;
    private LocalDate transactionDate;
    private Integer year;
    private Integer month;
    private Currency currency;
    @Indexed
    private String category;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;
}
