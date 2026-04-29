package com.fraudguard.deep.repository;

import com.fraudguard.hardik.model.transaction.Transaction;
import com.fraudguard.deep.dto.TransactionAuditRecordResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class TransactionAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveTransaction(Transaction transaction, int riskScore, String riskLevel, boolean flagged) {
        jdbcTemplate.update(
                """
                INSERT INTO transaction_audit (
                    transaction_id,
                    transaction_type,
                    amount,
                    transaction_time,
                    source_account_id,
                    destination_account_id,
                    risk_score,
                    risk_level,
                    flagged
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                transaction.getTransactionId(),
                transaction.getTransactionType().name(),
                transaction.getAmount(),
                Timestamp.valueOf(transaction.getTransactionTime()),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                riskScore,
                riskLevel,
                flagged
        );
    }

    public List<TransactionAuditRecordResponse> findRecentTransactions() {
        return jdbcTemplate.query(
                """
                SELECT id, transaction_id, transaction_type, amount, transaction_time,
                       source_account_id, destination_account_id, risk_score, risk_level, flagged
                FROM transaction_audit
                ORDER BY id DESC
                """,
                (rs, rowNum) -> new TransactionAuditRecordResponse(
                        rs.getLong("id"),
                        rs.getString("transaction_id"),
                        rs.getString("transaction_type"),
                        rs.getBigDecimal("amount").toPlainString(),
                        rs.getTimestamp("transaction_time").toLocalDateTime().toString(),
                        rs.getString("source_account_id"),
                        rs.getString("destination_account_id"),
                        rs.getInt("risk_score"),
                        rs.getString("risk_level"),
                        rs.getBoolean("flagged")
                )
        );
    }
}
