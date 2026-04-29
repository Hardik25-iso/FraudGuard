package com.fraudguard.deep.service.logging;

import com.fraudguard.deep.dto.FraudAnalysisResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

@Component
public class AlertLogger {

    private static final Path LOG_PATH = Path.of("logs", "fraud-alerts.log");

    public void log(FraudAnalysisResponse response) {
        if (!response.flagged()) {
            return;
        }

        try {
            Files.createDirectories(LOG_PATH.getParent());
            String line = String.format(
                    "%s | transactionId=%s | type=%s | riskScore=%d | riskLevel=%s | rules=%s | reasons=%s%n",
                    LocalDateTime.now(),
                    response.transactionId(),
                    response.transactionType(),
                    response.riskScore(),
                    response.riskLevel(),
                    response.triggeredRules(),
                    response.reasons()
            );
            Files.writeString(
                    LOG_PATH,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write fraud alert log.", exception);
        }
    }
}
