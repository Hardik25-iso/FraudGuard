package com.fraudguard.tejas.risk;

import com.fraudguard.tejas.rule.FraudRuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskScoringEngineTest {

    private final RiskScoringEngine engine = new RiskScoringEngine();

    @Test
    void shouldMapHighRiskCorrectly() {
        RiskAssessment assessment = engine.assessRisk(List.of(
                FraudRuleResult.triggered("LargeAmountRule", 50, "large"),
                FraudRuleResult.triggered("RapidFireRule", 50, "rapid")
        ));

        // 1 - (0.5 * 0.5) = 0.75 -> 75
        assertEquals(75, assessment.riskScore());
        assertEquals(RiskLevel.HIGH, assessment.riskLevel());
    }

    @Test
    void shouldMapMediumRiskCorrectly() {
        RiskAssessment assessment = engine.assessRisk(List.of(
                FraudRuleResult.triggered("LargeAmountRule", 30, "large"),
                FraudRuleResult.triggered("OddHoursRule", 20, "odd")
        ));

        // 1 - (0.7 * 0.8) = 0.44 -> 44
        assertEquals(44, assessment.riskScore());
        assertEquals(RiskLevel.MEDIUM, assessment.riskLevel());
    }

    @Test
    void shouldApproachHundredWithoutHardCap() {
        RiskAssessment assessment = engine.assessRisk(List.of(
                FraudRuleResult.triggered("A", 90, "a"),
                FraudRuleResult.triggered("B", 90, "b")
        ));

        // 1 - (0.1 * 0.1) = 0.99 -> 99
        assertEquals(99, assessment.riskScore());
        assertEquals(RiskLevel.HIGH, assessment.riskLevel());
    }

    @Test
    void shouldHandleHundredPercentRule() {
        RiskAssessment assessment = engine.assessRisk(List.of(
                FraudRuleResult.triggered("AbsoluteCertainty", 100, "certain")
        ));

        assertEquals(100, assessment.riskScore());
        assertEquals(RiskLevel.HIGH, assessment.riskLevel());
    }
}
