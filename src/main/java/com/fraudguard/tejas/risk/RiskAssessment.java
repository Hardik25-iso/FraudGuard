package com.fraudguard.tejas.risk;

import com.fraudguard.tejas.rule.FraudRuleResult;

import java.util.List;

public record RiskAssessment(
        int riskScore,
        RiskLevel riskLevel,
        List<FraudRuleResult> triggeredRules
) {
}
