package com.klaudia.transactionsimporter.statistics;

import com.klaudia.transactionsimporter.exceptions.statistics.CategoryNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.IbanNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.MonthStatisticsNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private static final String TRANSACTIONS_COLLECTION = "transactions";

    private static final String CATEGORY = "category";
    private static final String CURRENCY = "currency";
    private static final String IBAN = "iban";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String AMOUNT = "amount";
    private static final String GROUP_KEY = "groupKey";
    private static final String TOTAL_AMOUNT = "totalAmount";
    private static final String AVERAGE_AMOUNT = "averageAmount";
    private static final String TRANSACTION_COUNT = "transactionCount";

    private final MongoTemplate mongoTemplate;

    public List<StatisticResponse> getStatsByCategory(String category) {
        MatchOperation match = match(
                Criteria.where(CATEGORY).is(category.toUpperCase())
        );

        List<StatisticResponse> results = aggregate(
                newAggregation(
                        match,
                        buildGroupStage(),
                        buildProjectStage()
                )
        );

        if (results.isEmpty()) {
            throw new CategoryNotFoundException(
                    "No transactions found for category: " + category
            );
        }

        return results;
    }

    public List<StatisticResponse> getStatsForAllCategories() {
        return aggregate(
                newAggregation(
                        buildGroupStage(),
                        buildProjectStage()
                )
        );
    }

    public List<MonthStatisticResponse> getStatsByMonth(int year, int month) {
        MatchOperation match = match(
                Criteria.where(YEAR).is(year).and(MONTH).is(month)
        );

        List<MonthStatisticResponse> results = mongoTemplate.aggregate(
                newAggregation(
                        match,
                        buildMonthGroupStage(),
                        buildMonthProjectStage()
                ),
                TRANSACTIONS_COLLECTION,
                MonthStatisticResponse.class
        ).getMappedResults();

        if (results.isEmpty()) {
            throw new MonthStatisticsNotFoundException(
                    "No transactions found for: " + year + "-" + String.format("%02d", month)
            );
        }

        return results;
    }

    public StatisticResponse getStatsByIban(String iban) {
        MatchOperation match = match(
                Criteria.where(IBAN).is(iban)
        );

        List<StatisticResponse> results = aggregate(
                newAggregation(
                        match,
                        buildIbanGroupStage(),
                        buildIbanProjectStage()
                )
        );

        return results.stream()
                .findFirst()
                .orElseThrow(() ->
                        new IbanNotFoundException("No transactions found for IBAN: " + iban)
                );
    }

    private GroupOperation buildIbanGroupStage() {
        return group(IBAN)
                .first(CURRENCY).as(CURRENCY)
                .sum(AMOUNT).as(TOTAL_AMOUNT)
                .avg(AMOUNT).as(AVERAGE_AMOUNT)
                .count().as(TRANSACTION_COUNT);
    }

    private ProjectionOperation buildIbanProjectStage() {
        return project()
                .and("_id").as(GROUP_KEY)
                .and(CURRENCY).as(CURRENCY)
                .and(ArithmeticOperators.Round.roundValueOf(TOTAL_AMOUNT).place(2)).as(TOTAL_AMOUNT)
                .and(ArithmeticOperators.Round.roundValueOf(AVERAGE_AMOUNT).place(2)).as(AVERAGE_AMOUNT)
                .and(TRANSACTION_COUNT).as(TRANSACTION_COUNT);
    }

    private GroupOperation buildMonthGroupStage() {
        return group(YEAR, MONTH, CURRENCY)
                .sum(AMOUNT).as(TOTAL_AMOUNT)
                .avg(AMOUNT).as(AVERAGE_AMOUNT)
                .count().as(TRANSACTION_COUNT);
    }

    private ProjectionOperation buildMonthProjectStage() {
        return project()
                .and("_id." + YEAR).as(YEAR)
                .and("_id." + MONTH).as(MONTH)
                .and("_id." + CURRENCY).as(CURRENCY)
                .and(ArithmeticOperators.Round.roundValueOf(TOTAL_AMOUNT).place(2)).as(TOTAL_AMOUNT)
                .and(ArithmeticOperators.Round.roundValueOf(AVERAGE_AMOUNT).place(2)).as(AVERAGE_AMOUNT)
                .and(TRANSACTION_COUNT).as(TRANSACTION_COUNT);
    }

    private GroupOperation buildGroupStage() {
        return group(CATEGORY, CURRENCY)
                .sum(AMOUNT).as(TOTAL_AMOUNT)
                .avg(AMOUNT).as(AVERAGE_AMOUNT)
                .count().as(TRANSACTION_COUNT);
    }

    private ProjectionOperation buildProjectStage() {
        return project()
                .and("_id." + CATEGORY).as(GROUP_KEY)
                .and("_id." + CURRENCY).as(CURRENCY)
                .and(ArithmeticOperators.Round.roundValueOf(TOTAL_AMOUNT).place(2)).as(TOTAL_AMOUNT)
                .and(ArithmeticOperators.Round.roundValueOf(AVERAGE_AMOUNT).place(2)).as(AVERAGE_AMOUNT)
                .and(TRANSACTION_COUNT).as(TRANSACTION_COUNT);
    }

    private List<StatisticResponse> aggregate(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, TRANSACTIONS_COLLECTION, StatisticResponse.class)
                .getMappedResults();
    }
}