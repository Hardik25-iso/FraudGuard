package com.fraudguard.deep.repository;

import com.fraudguard.deep.dto.AlertRecordResponse;
import com.fraudguard.hardik.model.alert.FraudAlert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class FraudAlertRepository {

    private final JdbcTemplate jdbcTemplate;

    public FraudAlertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAlert(FraudAlert alert) {
        jdbcTemplate.update(
                """
                INSERT INTO fraud_alert (
                    transaction_id,
                    risk_score,
                    risk_level,
                    triggered_rules,
                    reasons,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                alert.getTransactionId(),
                alert.getRiskScore(),
                alert.getRiskLevel(),
                alert.getTriggeredRules(),
                alert.getReasons(),
                Timestamp.valueOf(alert.getCreatedAt())
        );
    }

    public List<AlertRecordResponse> findRecentAlerts() {
        return jdbcTemplate.query(
                """
                SELECT id, transaction_id, risk_score, risk_level, triggered_rules, reasons, created_at
                FROM fraud_alert
                ORDER BY id DESC
                """,
                (rs, rowNum) -> new AlertRecordResponse(
                        rs.getLong("id"),
                        rs.getString("transaction_id"),
                        rs.getInt("risk_score"),
                        rs.getString("risk_level"),
                        rs.getString("triggered_rules"),
                        rs.getString("reasons"),
                        rs.getTimestamp("created_at").toLocalDateTime().toString()
                )
        );
    }
}
