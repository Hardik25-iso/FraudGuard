package com.fraudguard.deep.controller;

import com.fraudguard.deep.dto.CsvAnalysisResponse;
import com.fraudguard.deep.dto.FraudAnalysisResponse;
import com.fraudguard.deep.dto.AlertRecordResponse;
import com.fraudguard.deep.dto.TransactionRequest;
import com.fraudguard.deep.dto.TransactionAuditRecordResponse;
import com.fraudguard.deep.repository.FraudAlertRepository;
import com.fraudguard.deep.repository.TransactionAuditRepository;
import com.fraudguard.deep.service.analysis.FraudAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
public class FraudAnalysisController {

    private final FraudAnalysisService fraudAnalysisService;
    private final TransactionAuditRepository transactionAuditRepository;
    private final FraudAlertRepository fraudAlertRepository;

    public FraudAnalysisController(
            FraudAnalysisService fraudAnalysisService,
            TransactionAuditRepository transactionAuditRepository,
            FraudAlertRepository fraudAlertRepository
    ) {
        this.fraudAnalysisService = fraudAnalysisService;
        this.transactionAuditRepository = transactionAuditRepository;
        this.fraudAlertRepository = fraudAlertRepository;
    }

    @GetMapping("/health")
    public String health() {
        return "FraudGuard API is running";
    }

    @PostMapping("/analyze")
    public FraudAnalysisResponse analyzeTransaction(@Valid @RequestBody TransactionRequest request) {
        return fraudAnalysisService.analyzeTransaction(request);
    }

    @PostMapping(path = "/analyze-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvAnalysisResponse analyzeCsv(@RequestParam("file") MultipartFile file) {
        return fraudAnalysisService.analyzeCsv(file);
    }

    @GetMapping("/audits")
    public List<TransactionAuditRecordResponse> audits() {
        return transactionAuditRepository.findRecentTransactions();
    }

    @GetMapping("/alerts")
    public List<AlertRecordResponse> alerts() {
        return fraudAlertRepository.findRecentAlerts();
    }
}
