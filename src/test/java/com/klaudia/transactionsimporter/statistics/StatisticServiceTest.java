package com.klaudia.transactionsimporter.statistics;

import com.klaudia.transactionsimporter.exceptions.statistics.CategoryNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.IbanNotFoundException;
import com.klaudia.transactionsimporter.exceptions.statistics.MonthStatisticsNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private StatisticService statisticService;

    @Test
    void getStatsByCategory_ShouldReturnStats_WhenCategoryExists() {
        // Given
        String category = "FOOD";
        StatisticResponse response = new StatisticResponse();
        response.setGroupKey("FOOD");
        response.setCurrency("EUR");
        response.setTotalAmount(new BigDecimal("100.00"));
        response.setAverageAmount(new BigDecimal("50.00"));
        response.setTransactionCount(2L);

        AggregationResults<StatisticResponse> results = new AggregationResults<>(List.of(response), mock(org.bson.Document.class));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(StatisticResponse.class)))
                .thenReturn(results);

        // When
        List<StatisticResponse> actual = statisticService.getStatsByCategory(category);

        // Then
        assertEquals(1, actual.size());
        assertEquals("FOOD", actual.get(0).getGroupKey());
        assertEquals(new BigDecimal("100.00"), actual.get(0).getTotalAmount());
    }

    @Test
    void getStatsByCategory_ShouldThrowException_WhenNoTransactionsFound() {
        // Given
        String category = "NON_EXISTENT";
        AggregationResults<StatisticResponse> results = new AggregationResults<>(Collections.emptyList(), mock(org.bson.Document.class));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(StatisticResponse.class)))
                .thenReturn(results);

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> statisticService.getStatsByCategory(category));
    }

    @Test
    void getStatsForAllCategories_ShouldReturnStats() {
        // Given
        StatisticResponse response = new StatisticResponse();
        response.setGroupKey("FOOD");
        AggregationResults<StatisticResponse> results = new AggregationResults<>(List.of(response), mock(org.bson.Document.class));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(StatisticResponse.class)))
                .thenReturn(results);

        // When
        List<StatisticResponse> actual = statisticService.getStatsForAllCategories();

        // Then
        assertEquals(1, actual.size());
    }

    @Test
    void getStatsByMonth_ShouldReturnStats_WhenMonthExists() {
        // Given
        int year = 2024;
        int month = 5;
        MonthStatisticResponse response = new MonthStatisticResponse();
        response.setYear(2024);
        response.setMonth(5);
        response.setCurrency("EUR");
        response.setTotalAmount(new BigDecimal("200.00"));

        AggregationResults<MonthStatisticResponse> results = new AggregationResults<>(List.of(response), mock(org.bson.Document.class));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(MonthStatisticResponse.class)))
                .thenReturn(results);

        // When
        List<MonthStatisticResponse> actual = statisticService.getStatsByMonth(year, month);

        // Then
        assertEquals(1, actual.size());
        assertEquals(2024, actual.get(0).getYear());
        assertEquals(5, actual.get(0).getMonth());
    }

    @Test
    void getStatsByMonth_ShouldThrowException_WhenNoTransactionsFound() {
        // Given
        int year = 2024;
        int month = 5;
        AggregationResults<MonthStatisticResponse> results = new AggregationResults<>(Collections.emptyList(), mock(org.bson.Document.class));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(MonthStatisticResponse.class)))
                .thenReturn(results);

        // When & Then
        assertThrows(MonthStatisticsNotFoundException.class, () -> statisticService.getStatsByMonth(year, month));
    }

    @Test
    void getStatsByIban_ShouldReturnStats_WhenIbanExists() {
        // Given
        String iban = "PL1234567890";
        StatisticResponse response = new StatisticResponse();
        response.setGroupKey("PL1234567890");
        response.setCurrency("PLN");

        AggregationResults<StatisticResponse> results = new AggregationResults<>(List.of(response), mock(org.bson.Document.class));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(StatisticResponse.class)))
                .thenReturn(results);

        // When
        StatisticResponse actual = statisticService.getStatsByIban(iban);

        // Then
        assertNotNull(actual);
        assertEquals(iban, actual.getGroupKey());
    }

    @Test
    void getStatsByIban_ShouldThrowException_WhenNoTransactionsFound() {
        // Given
        String iban = "PL0000";
        AggregationResults<StatisticResponse> results = new AggregationResults<>(Collections.emptyList(), mock(org.bson.Document.class));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(StatisticResponse.class)))
                .thenReturn(results);

        // When & Then
        assertThrows(IbanNotFoundException.class, () -> statisticService.getStatsByIban(iban));
    }
}