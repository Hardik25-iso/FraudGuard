package com.fraudguard.deep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "Transaction ID is required")
        String transactionId,

        @NotBlank(message = "Transaction Type is required")
        String transactionType,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Transaction Time is required")
        String transactionTime,

        String sourceAccountId,
        String destinationAccountId
) {
}
