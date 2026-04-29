package com.fraudguard.tejas.risk;

import com.fraudguard.tejas.rule.FraudRuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiskScoringEngine {

    private static final Logger logger = LoggerFactory.getLogger(RiskScoringEngine.class);

    public RiskAssessment assessRisk(List<FraudRuleResult> ruleResults) {
        List<FraudRuleResult> triggeredRules = ruleResults.stream()
                .filter(FraudRuleResult::triggered)
                .toList();

        if (triggeredRules.isEmpty()) {
            return new RiskAssessment(0, RiskLevel.LOW, triggeredRules);
        }

        // Weighted consideration using a diminishing returns formula (Probabilistic Sum)
        // Score = 100 * (1 - Product(1 - riskPoint_i / 100))
        double complementProduct = 1.0;
        for (FraudRuleResult rule : triggeredRules) {
            double weight = rule.riskPoints() / 100.0;
            complementProduct *= (1.0 - weight);
        }

        int finalScore = (int) Math.round(100.0 * (1.0 - complementProduct));
        
        // Ensure even with probabilistic sum, we stay within [0, 100]
        finalScore = Math.max(0, Math.min(finalScore, 100));

        RiskLevel riskLevel = mapRiskLevel(finalScore);
        
        logger.debug("Risk assessment complete. Triggered rules: {}, Calculated Score: {}, Risk Level: {}", 
                triggeredRules.size(), finalScore, riskLevel);

        return new RiskAssessment(finalScore, riskLevel, triggeredRules);
    }

    private RiskLevel mapRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return RiskLevel.HIGH;
        }
        if (riskScore >= 40) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
