package com.fraudguard.deep.dto;

import java.util.List;

public record FraudAnalysisResponse(
        String transactionId,
        String transactionType,
        int riskScore,
        String riskLevel,
        boolean flagged,
        List<String> triggeredRules,
        List<String> reasons,
        String sourceBalance,
        String destinationBalance
) {
}
