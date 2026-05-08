package com.klaudia.transactionsimporter.imports;

import com.klaudia.transactionsimporter.imports.model.ImportStatus;
import com.klaudia.transactionsimporter.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ImportControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ImportRepository importRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        transactionRepository.deleteAll();
        importRepository.deleteAll();
    }

    @Test
    void importFile_shouldReturnAcceptedAndStartProcessing() throws Exception {
        // Given
        String validIban = "DE89370400440532013000";
        String csvContent = "IBAN,Title,Date,Currency,Category,Amount\n" +
                validIban + ",Title 1,2023-01-01,PLN,FOOD,100.50";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        MvcResult result = mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.importStatus").value("PROCESSING"))
                .andReturn();

        String jobId = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        // Verify status endpoint eventually shows COMPLETED
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            mockMvc.perform(get("/api/imports/status/" + jobId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.successRows").value(1));
        });
    }

    @Test
    void getStatus_shouldReturnNotFound_whenJobDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/imports/status/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void importFile_shouldReturnBadRequest_whenFileIsEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importFile_shouldReturnBadRequest_whenFileIsNotCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "some content".getBytes());

        mockMvc.perform(multipart("/api/imports").file(file))
                .andExpect(status().is4xxClientError());
    }
}
