package com.fraudguard.tejas.detection;

import com.fraudguard.tejas.risk.RiskAssessment;
import com.fraudguard.tejas.risk.RiskScoringEngine;
import com.fraudguard.tejas.rule.FraudRule;
import com.fraudguard.tejas.rule.FraudRuleContext;
import com.fraudguard.tejas.rule.FraudRuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FraudDetectionEngine {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionEngine.class);
    private final List<FraudRule> rules;
    private final RiskScoringEngine riskScoringEngine;

    public FraudDetectionEngine(List<FraudRule> rules, RiskScoringEngine riskScoringEngine) {
        this.rules = rules;
        logger.info("Loaded {} fraud rules", rules.size());
        rules.forEach(rule -> logger.debug("Rule loaded: {}", rule.getClass().getSimpleName()));
        this.riskScoringEngine = riskScoringEngine;
    }

    public RiskAssessment analyze(FraudRuleContext context) {
        List<FraudRuleResult> results = rules.stream()
                .map(rule -> rule.evaluate(context))
                .toList();

        return riskScoringEngine.assessRisk(results);
    }
}
