package com.fraudguard.deep.service.analysis;

import com.fraudguard.deep.dto.FraudAnalysisResponse;
import com.fraudguard.deep.dto.TransactionRequest;
import com.fraudguard.deep.repository.FraudAlertRepository;
import com.fraudguard.deep.repository.TransactionAuditRepository;
import com.fraudguard.deep.service.account.BankAccountRegistry;
import com.fraudguard.swaraj.file.CsvTransactionReader;
import com.fraudguard.deep.service.logging.AlertLogger;
import com.fraudguard.hardik.profile.TransactionProfileService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fraudguard.tejas.detection.FraudDetectionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;

class FraudAnalysisServiceTest {

    private TransactionAuditRepository auditRepository;
    private FraudAlertRepository alertRepository;
    private AlertLogger alertLogger;
    private BankAccountRegistry bankAccountRegistry;
    private TransactionProfileService transactionProfileService;
    private CsvTransactionReader csvTransactionReader;
    private ReportingService reportingService;
    private FraudDetectionEngine fraudDetectionEngine;
    private FraudAnalysisService service;

    @BeforeEach
    void setUp() {
        auditRepository = mock(TransactionAuditRepository.class);
        alertRepository = mock(FraudAlertRepository.class);
        alertLogger = mock(AlertLogger.class);
        bankAccountRegistry = mock(BankAccountRegistry.class);
        transactionProfileService = mock(TransactionProfileService.class);
        csvTransactionReader = mock(CsvTransactionReader.class);
        reportingService = mock(ReportingService.class);
        fraudDetectionEngine = mock(FraudDetectionEngine.class);

        service = new FraudAnalysisService(
                bankAccountRegistry,
                transactionProfileService,
                csvTransactionReader,
                auditRepository,
                alertRepository,
                alertLogger,
                reportingService,
                fraudDetectionEngine
        );
    }

    @Test
    void shouldPersistAndLogHighRiskTransaction() {
        // Need to mock fraudDetectionEngine behavior as well if analyzeTransaction is called
        com.fraudguard.tejas.risk.RiskAssessment assessment = new com.fraudguard.tejas.risk.RiskAssessment(
                90, com.fraudguard.tejas.risk.RiskLevel.HIGH, java.util.Collections.emptyList());
        when(fraudDetectionEngine.analyze(any())).thenReturn(assessment);

        FraudAnalysisResponse response = service.analyzeTransaction(new TransactionRequest(
                "TXN-9001",
                "WITHDRAWAL",
                new BigDecimal("30000"),
                "2026-04-16T02:15:00",
                "ACC1001",
                null
        ));

        assertTrue(response.flagged());
        assertEquals("HIGH", response.riskLevel());
        verify(auditRepository, times(1)).saveTransaction(any(), anyInt(), any(), anyBoolean());
        verify(alertRepository, times(1)).saveAlert(any());
        verify(alertLogger, times(1)).log(any());
    }
}
