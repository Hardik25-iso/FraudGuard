package com.fraudguard.tejas.rule;

import com.fraudguard.hardik.model.account.BankAccount;
import com.fraudguard.hardik.model.account.SavingsAccount;
import com.fraudguard.hardik.model.profile.TransactionProfile;
import com.fraudguard.hardik.model.transaction.Deposit;
import com.fraudguard.hardik.model.transaction.Transaction;
import com.fraudguard.hardik.model.transaction.Transfer;
import com.fraudguard.hardik.model.transaction.Withdrawal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

final class FraudRuleTestSupport {

    private FraudRuleTestSupport() {
    }

    static BankAccount account(String id, String balance) {
        return new SavingsAccount(id, "Test User", new BigDecimal(balance));
    }

    static TransactionProfile profile(String accountId, int countLastMinute, String totalWithdrawalToday) {
        TransactionProfile profile = new TransactionProfile(accountId);
        profile.setTransactionCountLastMinute(countLastMinute);
        profile.setTotalWithdrawalToday(new BigDecimal(totalWithdrawalToday));
        profile.setAverageTransactionAmount(BigDecimal.ZERO);
        profile.setLastTransactionTime(LocalDateTime.of(2026, 4, 16, 10, 0));
        return profile;
    }

    static Transaction withdrawal(String amount, int hour) {
        return new Withdrawal(
                "TXN-W",
                new BigDecimal(amount),
                LocalDateTime.of(2026, 4, 16, hour, 0),
                "ACC1001"
        );
    }

    static Transaction transfer(String amount, int hour) {
        return new Transfer(
                "TXN-T",
                new BigDecimal(amount),
                LocalDateTime.of(2026, 4, 16, hour, 0),
                "ACC1001",
                "ACC1002"
        );
    }

    static Transaction deposit(String amount, int hour) {
        return new Deposit(
                "TXN-D",
                new BigDecimal(amount),
                LocalDateTime.of(2026, 4, 16, hour, 0),
                "ACC1002"
        );
    }
}
