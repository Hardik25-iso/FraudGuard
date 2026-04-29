package com.fraudguard.tejas.rule;

public record FraudRuleResult(
        String ruleName,
        boolean triggered,
        int riskPoints,
        String reason
) {

    public static FraudRuleResult triggered(String ruleName, int riskPoints, String reason) {
        return new FraudRuleResult(ruleName, true, riskPoints, reason);
    }

    public static FraudRuleResult notTriggered(String ruleName) {
        return new FraudRuleResult(ruleName, false, 0, "");
    }
}
