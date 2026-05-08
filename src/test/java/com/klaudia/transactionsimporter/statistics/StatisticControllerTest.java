package com.klaudia.transactionsimporter.statistics;

import com.klaudia.transactionsimporter.imports.ImportRepository;
import com.klaudia.transactionsimporter.transactions.Currency;
import com.klaudia.transactionsimporter.transactions.Transaction;
import com.klaudia.transactionsimporter.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class StatisticControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ImportRepository importRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        transactionRepository.deleteAll();
        importRepository.deleteAll();

        // Seed some data
        Transaction t1 = Transaction.builder()
                .iban("DE89370400440532013000")
                .title("Food 1")
                .transactionDate(LocalDate.of(2023, 1, 1))
                .year(2023)
                .month(1)
                .currency(Currency.PLN)
                .category("FOOD")
                .amount(new BigDecimal("100.00"))
                .build();

        Transaction t2 = Transaction.builder()
                .iban("DE89370400440532013000")
                .title("Food 2")
                .transactionDate(LocalDate.of(2023, 1, 2))
                .year(2023)
                .month(1)
                .currency(Currency.PLN)
                .category("FOOD")
                .amount(new BigDecimal("50.00"))
                .build();

        Transaction t3 = Transaction.builder()
                .iban("PL12345678901234567890123456")
                .title("Fuel")
                .transactionDate(LocalDate.of(2023, 2, 1))
                .year(2023)
                .month(2)
                .currency(Currency.PLN)
                .category("FUEL")
                .amount(new BigDecimal("200.00"))
                .build();

        transactionRepository.saveAll(List.of(t1, t2, t3));
    }

    @Test
    void getByCategory_shouldReturnStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/category").param("category", "FOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].groupKey").value("FOOD"))
                .andExpect(jsonPath("$[0].totalAmount").value(150.00))
                .andExpect(jsonPath("$[0].transactionCount").value(2));
    }

    @Test
    void getAllCategories_shouldReturnAllStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].groupKey", containsInAnyOrder("FOOD", "FUEL")));
    }

    @Test
    void getByMonth_shouldReturnMonthlyStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/month")
                        .param("year", "2023")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].year").value(2023))
                .andExpect(jsonPath("$[0].month").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(150.00));
    }

    @Test
    void getByIban_shouldReturnIbanStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/iban").param("iban", "DE89370400440532013000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupKey").value("DE89370400440532013000"))
                .andExpect(jsonPath("$.totalAmount").value(150.00))
                .andExpect(jsonPath("$.averageAmount").value(75.00));
    }

    @Test
    void getByCategory_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/statistics/category").param("category", "NON_EXISTENT"))
                .andExpect(status().isNotFound());
    }
}
