package com.klaudia.transactionsimporter.statistics;

import com.klaudia.transactionsimporter.exceptions.statistics.CategoryNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.IbanNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.MonthStatisticsNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final MongoTemplate mongoTemplate;

    public StatisticResponse getStatsByCategory(String category) {
        MatchOperation match = Aggregation.match(
                Criteria.where("category").is(category.toUpperCase())
        );
        return aggregate(Aggregation.newAggregation(
                match,
                buildGroupStage("category"),
                buildProjectStage()
        )).stream()
                .findFirst()
                .orElseThrow(() -> new CategoryNotFoundException(
                        "No transactions found for category: " + category));
    }

    public List<StatisticResponse> getStatsForAllCategories() {
        return aggregate(Aggregation.newAggregation(
                buildGroupStage("category"),
                buildProjectStage()
        ));
    }

    public StatisticResponse getStatsByMonth(int year, int month) {
        MatchOperation match = Aggregation.match(
                Criteria.where("year").is(year).and("month").is(month)
        );
        return aggregate(Aggregation.newAggregation(
                match,
                buildGroupStage("month"),
                buildProjectStage()
        )).stream()
                .findFirst()
                .orElseThrow(() -> new MonthStatisticsNotFoundException(
                        "No transactions found for: " + year + "-" + String.format("%02d", month)));
    }

    public StatisticResponse getStatsByIban(String iban) {
        MatchOperation match = Aggregation.match(
                Criteria.where("iban").is(iban)
        );
        return aggregate(Aggregation.newAggregation(
                match,
                buildGroupStage("iban"),
                buildProjectStage()
        )).stream()
                .findFirst()
                .orElseThrow(() -> new IbanNotFoundException(
                        "No transactions found for IBAN: " + iban));
    }

    private GroupOperation buildGroupStage(String field) {
        return Aggregation.group(field)
                .sum("amount").as("totalAmount")
                .avg("amount").as("averageAmount")
                .count().as("transactionCount");
    }

    private ProjectionOperation buildProjectStage() {
        return Aggregation.project()
                .and("_id").as("groupKey")
                .and("totalAmount").as("totalAmount")
                .and("averageAmount").as("averageAmount")
                .and("transactionCount").as("transactionCount");
    }

    private List<StatisticResponse> aggregate(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, "transactions", StatisticResponse.class)
                .getMappedResults();
    }
}
