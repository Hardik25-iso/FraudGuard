package com.fraudguard.deep.dto;

public record TransactionAuditRecordResponse(
        Long id,
        String transactionId,
        String transactionType,
        String amount,
        String transactionTime,
        String sourceAccountId,
        String destinationAccountId,
        int riskScore,
        String riskLevel,
        boolean flagged
) {
}
