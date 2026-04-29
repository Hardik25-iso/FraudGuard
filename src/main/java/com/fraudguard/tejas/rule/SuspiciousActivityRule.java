package com.fraudguard.tejas.rule;

import com.fraudguard.hardik.model.transaction.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SuspiciousActivityRule implements FraudRule {

    private static final String RULE_NAME = "SuspiciousActivityRule";
    private static final BigDecimal MULTIPLIER_THRESHOLD = new BigDecimal("10.0");
    private static final BigDecimal MINIMUM_SIGNIFICANT_AMOUNT = new BigDecimal("10000.00");
    private static final int RISK_POINTS = 45;

    @Override
    public FraudRuleResult evaluate(FraudRuleContext context) {
        if (context == null || context.transaction() == null || context.destinationAccount() == null || context.transactionProfile() == null) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }

        // Trigger for deposits and incoming transfers (which increase balance)
        if (context.transaction().getTransactionType() != TransactionType.DEPOSIT && 
            context.transaction().getTransactionType() != TransactionType.TRANSFER) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }

        BigDecimal currentBalance = context.destinationAccount().getBalance();
        BigDecimal depositAmount = context.transaction().getAmount();
        java.time.LocalDateTime lastTxTime = context.transactionProfile().getLastTransactionTime();

        // 1. Dormant Account Detection (Primary check)
        // If last transaction was more than 30 days ago, or if it's the first ever transaction on an old account (count is 0 but account might be old - though here we use lastTxTime)
        if (lastTxTime != null) {
            java.time.Duration duration = java.time.Duration.between(lastTxTime, context.transaction().getTransactionTime());
            if (duration.toDays() > 30) {
                 return FraudRuleResult.triggered(
                    RULE_NAME,
                    RISK_POINTS,
                    String.format("Sudden activity on dormant account (last activity: %d days ago).", duration.toDays())
                );
            }
        }

        // 2. Large deposit on low balance account (Secondary check)
        if (depositAmount.compareTo(MINIMUM_SIGNIFICANT_AMOUNT) > 0) {
            // If balance is basically zero, any large deposit is suspicious
            if (currentBalance.compareTo(BigDecimal.ONE) < 0) {
                 return FraudRuleResult.triggered(
                    RULE_NAME,
                    RISK_POINTS,
                    String.format("Large deposit of %,.2f on a zero-balance account.", depositAmount)
                );
            }

            BigDecimal ratio = depositAmount.divide(currentBalance, 2, java.math.RoundingMode.HALF_UP);
            if (ratio.compareTo(MULTIPLIER_THRESHOLD) > 0) {
                return FraudRuleResult.triggered(
                    RULE_NAME,
                    RISK_POINTS,
                    String.format("Inflow of %,.2f is %sx current balance (Potential Money Laundering).", depositAmount, ratio.toPlainString())
                );
            }
        }

        return FraudRuleResult.notTriggered(RULE_NAME);
    }
}
