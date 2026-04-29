package com.fraudguard.tejas.rule;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class LargeAmountRule implements FraudRule {

    private static final String RULE_NAME = "LargeAmountRule";
    private static final BigDecimal THRESHOLD_MEDIUM = new BigDecimal("50000.00");
    private static final BigDecimal THRESHOLD_EXTREME = new BigDecimal("500000.00");

    @Override
    public FraudRuleResult evaluate(FraudRuleContext context) {
        if (context == null || context.transaction() == null) {
            return FraudRuleResult.notTriggered(RULE_NAME);
        }
        BigDecimal amount = context.transaction().getAmount();
        
        if (amount.compareTo(THRESHOLD_EXTREME) >= 0) {
            return FraudRuleResult.triggered(
                    RULE_NAME,
                    60,
                    String.format("Extreme transaction amount exceeding INR %,.2f.", THRESHOLD_EXTREME)
            );
        }
        
        if (amount.compareTo(THRESHOLD_MEDIUM) > 0) {
            return FraudRuleResult.triggered(
                    RULE_NAME,
                    30,
                    String.format("Large transaction amount exceeding INR %,.2f.", THRESHOLD_MEDIUM)
            );
        }
        
        return FraudRuleResult.notTriggered(RULE_NAME);
    }
}
