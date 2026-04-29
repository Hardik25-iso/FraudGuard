package com.fraudguard.hardik.model.account;

import java.math.BigDecimal;

public class SavingsAccount extends BankAccount {

    public SavingsAccount(String accountId, String accountHolderName, BigDecimal balance) {
        super(accountId, accountHolderName, balance);
    }

    public SavingsAccount(String accountId, String accountHolderName, BigDecimal balance, int version) {
        super(accountId, accountHolderName, balance, version);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return BigDecimal.ZERO;
    }
}
