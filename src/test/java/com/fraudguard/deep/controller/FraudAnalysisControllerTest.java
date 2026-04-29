package com.fraudguard.deep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudguard.deep.dto.FraudAnalysisResponse;
import com.fraudguard.deep.dto.TransactionRequest;
import com.fraudguard.deep.service.analysis.FraudAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FraudAnalysisController.class)
class FraudAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FraudAnalysisService fraudAnalysisService;

    @MockBean
    private com.fraudguard.deep.repository.TransactionAuditRepository transactionAuditRepository;

    @MockBean
    private com.fraudguard.deep.repository.FraudAlertRepository fraudAlertRepository;

    @Test
    void shouldReturnFraudAnalysisResponse() throws Exception {
        TransactionRequest request = new TransactionRequest(
                "TXN-123",
                "TRANSFER",
                new BigDecimal("15000"),
                "2026-04-16T01:10:00",
                "ACC1001",
                "ACC1002"
        );

        FraudAnalysisResponse response = new FraudAnalysisResponse(
                "TXN-123",
                "TRANSFER",
                75,
                "HIGH",
                true,
                List.of("LargeAmountRule", "OddHoursRule"),
                List.of("large", "odd"),
                "1,000.00",
                "15,000.00"
        );

        when(fraudAnalysisService.analyzeTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/fraud/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-123"))
                .andExpect(jsonPath("$.riskScore").value(75))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.flagged").value(true));
    }
}
