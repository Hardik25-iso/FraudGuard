package com.fraudguard.tejas.rule;

import org.springframework.stereotype.Component;

@Component
public class OddHoursRule implements FraudRule {

    private static final String RULE_NAME = "OddHoursRule";
    private static final int START_HOUR = 0;
    private static final int END_HOUR = 4;
    private static final int RISK_POINTS = 25;

    @Override
    public FraudRuleResult evaluate(FraudRuleContext context) {
        if (context == null || context.transaction() == null) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }
        int hour = context.transaction().getTransactionTime().getHour();
        if (hour >= START_HOUR && hour <= END_HOUR) {
            return FraudRuleResult.triggered(
                    RULE_NAME,
                    RISK_POINTS,
                    "Transaction was performed between 12 AM and 4:59 AM."
            );
        }
        return FraudRuleResult.notTriggered(RULE_NAME);
    }
}
