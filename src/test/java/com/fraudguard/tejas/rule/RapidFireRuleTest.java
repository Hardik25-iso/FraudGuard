package com.fraudguard.tejas.rule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RapidFireRuleTest {

    private final RapidFireRule rule = new RapidFireRule();

    @Test
    void shouldTriggerWhenThreeOrMoreTransactionsOccurWithinOneMinute() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.withdrawal("3000", 11),
                FraudRuleTestSupport.account("ACC1001", "50000"),
                null,
                FraudRuleTestSupport.profile("ACC1001", 3, "3000")
        ));

        assertTrue(result.triggered());
    }

    @Test
    void shouldNotTriggerWhenCountIsBelowThreshold() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.withdrawal("3000", 11),
                FraudRuleTestSupport.account("ACC1001", "50000"),
                null,
                FraudRuleTestSupport.profile("ACC1001", 2, "3000")
        ));

        assertFalse(result.triggered());
    }
}
