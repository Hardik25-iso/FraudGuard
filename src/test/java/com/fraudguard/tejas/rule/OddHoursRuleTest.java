package com.fraudguard.tejas.rule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OddHoursRuleTest {

    private final OddHoursRule rule = new OddHoursRule();

    @Test
    void shouldTriggerForTransactionBetweenMidnightAndFourAm() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.withdrawal("2000", 2),
                FraudRuleTestSupport.account("ACC1001", "50000"),
                null,
                FraudRuleTestSupport.profile("ACC1001", 1, "2000")
        ));

        assertTrue(result.triggered());
    }

    @Test
    void shouldNotTriggerOutsideOddHours() {
        FraudRuleResult result = rule.evaluate(new FraudRuleContext(
                FraudRuleTestSupport.withdrawal("2000", 10),
                FraudRuleTestSupport.account("ACC1001", "50000"),
                null,
                FraudRuleTestSupport.profile("ACC1001", 1, "2000")
        ));

        assertFalse(result.triggered());
    }
}
