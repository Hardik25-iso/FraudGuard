package com.fraudguard.hardik.model.account;

import java.math.BigDecimal;

public abstract class BankAccount extends Account {

    protected BankAccount(String accountId, String accountHolderName, BigDecimal balance) {
        super(accountId, accountHolderName, balance);
    }

    protected BankAccount(String accountId, String accountHolderName, BigDecimal balance, int version) {
        super(accountId, accountHolderName, balance, version);
    }

    public abstract BigDecimal getMinimumBalance();

    @Override
    public void debit(BigDecimal amount) {
        validatePositiveAmount(amount);
        BigDecimal normalizedAmount = normalize(amount);
        
        BigDecimal remainingBalance = this.balance.subtract(normalizedAmount);
        if (remainingBalance.compareTo(getMinimumBalance()) < 0) {
            throw new com.fraudguard.hardik.exception.InvalidTransactionException(
                String.format("Transaction declined: Minimum balance requirement of INR %,.2f not met. Current balance: %,.2f, requested debit: %,.2f",
                    getMinimumBalance(), this.balance, normalizedAmount));
        }
        
        this.balance = remainingBalance;
    }
}
