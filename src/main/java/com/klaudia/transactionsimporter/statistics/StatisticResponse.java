package com.klaudia.transactionsimporter.statistics;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class StatisticResponse {
    private String groupKey;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private long transactionCount;
}
