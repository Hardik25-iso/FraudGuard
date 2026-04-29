package com.fraudguard.hardik.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountId) {
        super("Bank account not found: " + accountId);
    }
}
