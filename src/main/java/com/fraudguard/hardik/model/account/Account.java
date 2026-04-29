package com.fraudguard.hardik.model.account;

import com.fraudguard.hardik.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Account {

    private final String accountId;
    private final String accountHolderName;
    protected BigDecimal balance;
    private int version;

    protected Account(String accountId, String accountHolderName, BigDecimal openingBalance) {
        this(accountId, accountHolderName, openingBalance, 0);
    }

    protected Account(String accountId, String accountHolderName, BigDecimal openingBalance, int version) {
        if (accountId == null || accountId.isBlank()) {
            throw new InvalidTransactionException("Account id cannot be blank.");
        }
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new InvalidTransactionException("Account holder name cannot be blank.");
        }
        if (openingBalance == null || openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("Opening balance cannot be negative.");
        }

        this.accountId = accountId;
        this.accountHolderName = accountHolderName;
        this.balance = openingBalance.setScale(2, RoundingMode.HALF_UP);
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public abstract AccountType getAccountType();

    public void credit(BigDecimal amount) {
        validatePositiveAmount(amount);
        balance = balance.add(normalize(amount));
    }

    public void debit(BigDecimal amount) {
        validatePositiveAmount(amount);
        BigDecimal normalizedAmount = normalize(amount);
        if (balance.compareTo(normalizedAmount) < 0) {
            throw new InvalidTransactionException(
                    "Insufficient balance in account " + accountId
            );
        }
        balance = balance.subtract(normalizedAmount);
    }

    protected void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero.");
        }
    }

    protected BigDecimal normalize(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
