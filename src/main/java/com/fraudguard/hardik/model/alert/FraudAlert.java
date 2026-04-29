package com.fraudguard.hardik.model.alert;

import java.time.LocalDateTime;

public class FraudAlert {

    private final String transactionId;
    private final int riskScore;
    private final String riskLevel;
    private final String triggeredRules;
    private final String reasons;
    private final LocalDateTime createdAt;

    public FraudAlert(
            String transactionId,
            int riskScore,
            String riskLevel,
            String triggeredRules,
            String reasons,
            LocalDateTime createdAt
    ) {
        this.transactionId = transactionId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.triggeredRules = triggeredRules;
        this.reasons = reasons;
        this.createdAt = createdAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getTriggeredRules() {
        return triggeredRules;
    }

    public String getReasons() {
        return reasons;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
