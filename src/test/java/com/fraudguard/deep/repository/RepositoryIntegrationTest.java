package com.fraudguard.deep.repository;

import com.fraudguard.hardik.model.account.BankAccount;
import com.fraudguard.hardik.model.account.SavingsAccount;
import com.fraudguard.hardik.model.transaction.Deposit;
import com.fraudguard.hardik.model.transaction.Transaction;
import com.fraudguard.deep.service.account.BankAccountRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({TransactionAuditRepository.class, BankAccountRegistry.class})
@Sql(statements = "INSERT INTO bank_account (account_id, account_type, account_holder_name, balance) VALUES ('TEST-001', 'SAVINGS', 'Test User', 1000.00)")
class RepositoryIntegrationTest {

    @Autowired
    private TransactionAuditRepository auditRepository;

    @Autowired
    private BankAccountRegistry accountRegistry;

    @Test
    void shouldSaveAndRetrieveTransactionAudit() {
        Transaction txn = new Deposit("TXN-TEST", new BigDecimal("500.00"), LocalDateTime.now(), "TEST-001");
        
        auditRepository.saveTransaction(txn, 0, "LOW", false);
        
        var recent = auditRepository.findRecentTransactions();
        assertFalse(recent.isEmpty());
        assertEquals("TXN-TEST", recent.get(0).transactionId());
    }

    @Test
    void shouldUpdateAccountBalanceInDatabase() {
        BankAccount account = accountRegistry.getRequiredAccount("TEST-001");
        account.credit(new BigDecimal("200.00"));
        
        accountRegistry.updateBalance(account);
        
        BankAccount updated = accountRegistry.getRequiredAccount("TEST-001");
        assertEquals(new BigDecimal("1200.00").setScale(2), updated.getBalance().setScale(2));
    }
}
