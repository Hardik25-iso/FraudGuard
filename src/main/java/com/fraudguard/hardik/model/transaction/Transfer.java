package com.fraudguard.hardik.model.transaction;

import com.fraudguard.hardik.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transfer extends Transaction {

    public Transfer(
            String transactionId,
            BigDecimal amount,
            LocalDateTime transactionTime,
            String sourceAccountId,
            String destinationAccountId
    ) {
        super(
                transactionId,
                TransactionType.TRANSFER,
                amount,
                transactionTime,
                sourceAccountId,
                destinationAccountId
        );
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            throw new InvalidTransactionException("Transfer requires a source account.");
        }
        if (destinationAccountId == null || destinationAccountId.isBlank()) {
            throw new InvalidTransactionException("Transfer requires a destination account.");
        }
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new InvalidTransactionException("Transfer source and destination cannot be the same.");
        }
    }
}
