package com.fraudguard.hardik.model.transaction;

import com.fraudguard.hardik.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public abstract class Transaction {

    private final String transactionId;
    private final TransactionType transactionType;
    private final BigDecimal amount;
    private final LocalDateTime transactionTime;
    private final String sourceAccountId;
    private final String destinationAccountId;

    protected Transaction(
            String transactionId,
            TransactionType transactionType,
            BigDecimal amount,
            LocalDateTime transactionTime,
            String sourceAccountId,
            String destinationAccountId
    ) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new InvalidTransactionException("Transaction id cannot be blank.");
        }
        if (transactionType == null) {
            throw new InvalidTransactionException("Transaction type is required.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero.");
        }
        if (transactionTime == null) {
            throw new InvalidTransactionException("Transaction time is required.");
        }

        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.transactionTime = transactionTime;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }
}
