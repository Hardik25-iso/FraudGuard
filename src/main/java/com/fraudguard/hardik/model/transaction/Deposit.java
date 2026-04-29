package com.fraudguard.hardik.model.transaction;

import com.fraudguard.hardik.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Deposit extends Transaction {

    public Deposit(
            String transactionId,
            BigDecimal amount,
            LocalDateTime transactionTime,
            String destinationAccountId
    ) {
        super(
                transactionId,
                TransactionType.DEPOSIT,
                amount,
                transactionTime,
                null,
                destinationAccountId
        );
        if (destinationAccountId == null || destinationAccountId.isBlank()) {
            throw new InvalidTransactionException("Deposit requires a destination account.");
        }
    }
}
