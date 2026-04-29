package com.fraudguard.tejas.rule;

import com.fraudguard.hardik.model.transaction.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class VelocityRule implements FraudRule {

    private static final String RULE_NAME = "VelocityRule";
    private static final BigDecimal VELOCITY_THRESHOLD = new BigDecimal("0.50");
    private static final int RISK_POINTS = 25;

    @Override
    public FraudRuleResult evaluate(FraudRuleContext context) {
        if (context == null || context.transaction() == null || context.sourceAccount() == null || context.transactionProfile() == null) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }

        if (context.transaction().getTransactionType() == TransactionType.DEPOSIT) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }

        BigDecimal balance = context.sourceAccount().getBalance();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }

        BigDecimal velocityRatio = context.transactionProfile()
                .getTotalWithdrawalToday()
                .divide(balance, 4, RoundingMode.HALF_UP);

        if (velocityRatio.compareTo(VELOCITY_THRESHOLD) > 0) {
            return FraudRuleResult.triggered(
                    RULE_NAME,
                    RISK_POINTS,
                    "Daily withdrawal amount exceeds 50% of the account balance."
            );
        }

        return FraudRuleResult.notTriggered(RULE_NAME);
    }
}
