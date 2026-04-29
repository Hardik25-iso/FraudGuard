package com.fraudguard.hardik.model.transaction;

import com.fraudguard.hardik.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Withdrawal extends Transaction {

    public Withdrawal(
            String transactionId,
            BigDecimal amount,
            LocalDateTime transactionTime,
            String sourceAccountId
    ) {
        super(
                transactionId,
                TransactionType.WITHDRAWAL,
                amount,
                transactionTime,
                sourceAccountId,
                null
        );
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            throw new InvalidTransactionException("Withdrawal requires a source account.");
        }
    }
}
