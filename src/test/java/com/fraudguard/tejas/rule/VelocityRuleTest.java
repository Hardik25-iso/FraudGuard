package com.fraudguard.tejas.rule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelocityRuleTest {

    private final VelocityRule rule = new VelocityRule();

    @Test
    void shouldTriggerWhenDailyWithdrawalExceedsHalfOfBalance() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.withdrawal("20000", 12),
                FraudRuleTestSupport.account("ACC1001", "50000"),
                null,
                FraudRuleTestSupport.profile("ACC1001", 1, "30000")
        ));

        assertTrue(result.triggered());
    }

    @Test
    void shouldNotTriggerForDepositTransactions() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.deposit("20000", 12),
                null,
                FraudRuleTestSupport.account("ACC1002", "50000"),
                FraudRuleTestSupport.profile("ACC1002", 1, "30000")
        ));

        assertFalse(result.triggered());
    }
}
