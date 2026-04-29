package com.fraudguard.tejas.rule;

import com.fraudguard.hardik.model.account.BankAccount;
import com.fraudguard.hardik.model.profile.TransactionProfile;
import com.fraudguard.hardik.model.transaction.Transaction;

public record FraudRuleContext(
        Transaction transaction,
        BankAccount sourceAccount,
        BankAccount destinationAccount,
        TransactionProfile transactionProfile
) {
}
