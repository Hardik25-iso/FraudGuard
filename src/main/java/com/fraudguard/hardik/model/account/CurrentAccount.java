package com.fraudguard.hardik.model.account;

import java.math.BigDecimal;

public class CurrentAccount extends BankAccount {

    public CurrentAccount(String accountId, String accountHolderName, BigDecimal balance) {
        super(accountId, accountHolderName, balance);
    }

    public CurrentAccount(String accountId, String accountHolderName, BigDecimal balance, int version) {
        super(accountId, accountHolderName, balance, version);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CURRENT;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return BigDecimal.ZERO;
    }
}
