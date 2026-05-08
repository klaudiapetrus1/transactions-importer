package com.klaudia.transactionsimporter.statistics;

import com.klaudia.transactionsimporter.statistics.dto.MonthStatisticResponse;
import com.klaudia.transactionsimporter.statistics.dto.StatisticResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Validated
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/category")
    public ResponseEntity<List<StatisticResponse>> getByCategory(@RequestParam String category) {
        List<StatisticResponse> statsByCategory = statisticService.getStatsByCategory(category);
        return ResponseEntity.ok(statsByCategory);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<StatisticResponse>> getAllCategories() {
        List<StatisticResponse> statsForAllCategories = statisticService.getStatsForAllCategories();
        return ResponseEntity.ok(statsForAllCategories);
    }

    @GetMapping("/month")
    public ResponseEntity<List<MonthStatisticResponse>> getByMonth(@RequestParam int year,
                                                                   @RequestParam @Min(1) @Max(12) int month) {
        List<MonthStatisticResponse> statsByMonth = statisticService.getStatsByMonth(year, month);
        return ResponseEntity.ok(statsByMonth);
    }

    @GetMapping("/iban")
    public ResponseEntity<StatisticResponse> getByIban(
            @RequestParam String iban) {
        StatisticResponse statsByIban = statisticService.getStatsByIban(iban);
        return ResponseEntity.ok(statsByIban);
    }
}
