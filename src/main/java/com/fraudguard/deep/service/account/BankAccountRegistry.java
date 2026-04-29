package com.fraudguard.deep.service.account;

import com.fraudguard.hardik.exception.AccountNotFoundException;
import com.fraudguard.hardik.model.account.BankAccount;
import com.fraudguard.hardik.model.account.CurrentAccount;
import com.fraudguard.hardik.model.account.SavingsAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class BankAccountRegistry {

    private final JdbcTemplate jdbcTemplate;

    public BankAccountRegistry(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BankAccount getRequiredAccount(String accountId) {
        BankAccount account = findAccount(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    public BankAccount findAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return null;
        }
        return jdbcTemplate.query(
                "SELECT account_id, account_type, account_holder_name, balance, version FROM bank_account WHERE account_id = ?",
                rs -> {
                    if (rs.next()) {
                        String id = rs.getString("account_id");
                        String type = rs.getString("account_type");
                        String name = rs.getString("account_holder_name");
                        BigDecimal balance = rs.getBigDecimal("balance");
                        int version = rs.getInt("version");
                        
                        if ("SAVINGS".equalsIgnoreCase(type)) {
                            return new SavingsAccount(id, name, balance, version);
                        } else {
                            return new CurrentAccount(id, name, balance, version);
                        }
                    }
                    return null;
                },
                accountId
        );
    }
    
    public java.util.List<BankAccount> getAllAccounts() {
        return jdbcTemplate.query(
                "SELECT account_id, account_type, account_holder_name, balance, version FROM bank_account",
                (rs, rowNum) -> {
                    String id = rs.getString("account_id");
                    String type = rs.getString("account_type");
                    String name = rs.getString("account_holder_name");
                    BigDecimal balance = rs.getBigDecimal("balance");
                    int version = rs.getInt("version");
                    
                    if ("SAVINGS".equalsIgnoreCase(type)) {
                        return new SavingsAccount(id, name, balance, version);
                    } else {
                        return new CurrentAccount(id, name, balance, version);
                    }
                }
        );
    }

    
    @Transactional
    public void updateBalance(BankAccount account) {
        int rows = jdbcTemplate.update(
                "UPDATE bank_account SET balance = ?, version = version + 1 WHERE account_id = ? AND version = ?",
                account.getBalance(),
                account.getAccountId(),
                account.getVersion()
        );
        
        if (rows == 0) {
            throw new java.util.ConcurrentModificationException(
                "Account " + account.getAccountId() + " was updated by another transaction.");
        }
        // Update local version to reflect DB change
        account.setVersion(account.getVersion() + 1);
    }
}
