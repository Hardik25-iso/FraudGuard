package com.fraudguard.deep.dto;

public record AlertRecordResponse(
        Long id,
        String transactionId,
        int riskScore,
        String riskLevel,
        String triggeredRules,
        String reasons,
        String createdAt
) {
}
