package com.fraudguard.deep.service.analysis;

import com.fraudguard.deep.dto.FraudAnalysisResponse;
import com.fraudguard.deep.dto.TransactionAuditRecordResponse;
import com.fraudguard.deep.repository.TransactionAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);
    private final TransactionAuditRepository transactionAuditRepository;
    private static final Path REPORT_PATH = Path.of("logs", "fraud-summary-report.txt");

    public ReportingService(TransactionAuditRepository transactionAuditRepository) {
        this.transactionAuditRepository = transactionAuditRepository;
    }

    @Async
    public void generateAsyncReport(FraudAnalysisResponse response) {
        try {
            // Real background work: Calculate daily volume summary
            List<TransactionAuditRecordResponse> audits = transactionAuditRepository.findRecentTransactions();
            
            BigDecimal totalVolume = audits.stream()
                    .map(a -> new BigDecimal(a.amount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long flaggedCount = audits.stream().filter(TransactionAuditRecordResponse::flagged).count();

            String reportLine = String.format(
                    "[%s] THREAD: %s | REPORT GENERATED | New Txn: %s | Current Daily Volume: INR %s | Total Flagged: %d%n",
                    LocalDateTime.now(),
                    Thread.currentThread().getName(),
                    response.transactionId(),
                    totalVolume.toPlainString(),
                    flaggedCount
            );
            
            Files.createDirectories(REPORT_PATH.getParent());
            Files.writeString(
                    REPORT_PATH,
                    reportLine,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            logger.error("IO Error while writing fraud report: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in async reporting: {}", e.getMessage(), e);
        }
    }
}
