package com.klaudia.transactionsimporter.statistics.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MonthStatisticResponse {
    private Integer year;
    private Integer month;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private long transactionCount;
}
