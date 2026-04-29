package com.fraudguard.tejas.rule;

import org.springframework.stereotype.Component;

@Component
public class RapidFireRule implements FraudRule {

    private static final String RULE_NAME = "RapidFireRule";
    private static final int RAPID_FIRE_THRESHOLD = 3;
    private static final int RISK_POINTS = 25;

    @Override
    public FraudRuleResult evaluate(FraudRuleContext context) {
        if (context == null || context.transactionProfile() == null) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }
        if (context.transactionProfile().getTransactionCountLastMinute() >= RAPID_FIRE_THRESHOLD) {
            return FraudRuleResult.triggered(
                    RULE_NAME,
                    RISK_POINTS,
                    "Three or more transactions were observed within the last minute."
            );
        }
        return FraudRuleResult.notTriggered(RULE_NAME);
    }
}
