package com.fraudguard.tejas.rule;

public interface FraudRule {

    FraudRuleResult evaluate(FraudRuleContext context);
}
