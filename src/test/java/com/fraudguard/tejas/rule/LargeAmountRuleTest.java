package com.fraudguard.tejas.rule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargeAmountRuleTest {

    private final LargeAmountRule rule = new LargeAmountRule();

    @Test
    void shouldTriggerForAmountAboveThreshold() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.withdrawal("60000", 11),
                FraudRuleTestSupport.account("ACC1001", "100000"),
                null,
                FraudRuleTestSupport.profile("ACC1001", 1, "0")
        ));

        assertTrue(result.triggered());
    }

    @Test
    void shouldNotTriggerForAmountAtThreshold() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.withdrawal("50000", 11),
                FraudRuleTestSupport.account("ACC1001", "100000"),
                null,
                FraudRuleTestSupport.profile("ACC1001", 1, "0")
        ));

        assertFalse(result.triggered());
    }
}
