# FraudGuard Step 1 Review And Plan

## What is currently in the repo

- `FraudGuard_ML_PaySim.ipynb` is the best ML asset in the repository right now.
- `fraudguard_paysim_rf_pipeline.pkl` and `fraudguard_paysim_metadata.pkl` show that a PaySim-based model was already trained.
- There is no Java source tree, no `src/`, no Maven/Gradle build file, and no test structure yet.

## Corrections needed right now

- The Java project does not exist in code yet. The PPT may describe classes, but those classes are not present in the repository.
- `FraudGuard_ML_Model.ipynb` uses synthetic labels derived from the same fraud rules it later trains on. That is label leakage and should not be your main evidence in the final report.
- `FraudGuard_ML_Model_Colab.ipynb` is malformed JSON at the end, so it is not reliable as the canonical notebook file.
- `FruadGuard.pdf` should be renamed to `FraudGuard.pdf` for submission quality.
- Because your course is a Java project, the ML model should remain a supporting module. The grading focus should be on Java architecture, file handling or JDBC, exception handling, threading, testing, and code quality.

## Recommended final architecture

- Phase 1 submission: strong Java rule-based fraud detection system with file handling, good OOP design, reports, alerts, and tests.
- ML module: one clean notebook or script showing PaySim training and exported model artifacts.
- Phase 2 enhancement: JDBC persistence, dashboard or report generation, optional model-assisted scoring.

## Ownership split for 4 members

- Swaraj Deogirkar: PaySim training pipeline, dataset preparation, model export, later `CSVReader` and dataset loading utilities.
- Hardik Gulati: core domain model classes such as `Account`, `BankAccount`, transaction hierarchy, profiles, and custom exceptions.
- Tejas Kale: fraud rules, rule engine, risk scoring engine, and unit tests for rule behavior.
- Deep Thadeshwar: service layer, alert logging, JDBC integration, thread-based monitoring, `Main` class, and integration tests.

## Suggested git commit split

- Swaraj branch: `feature/ml-paysim-training`
- Hardik branch: `feature/java-core-models`
- Tejas branch: `feature/fraud-rules-engine`
- Deep branch: `feature/service-jdbc-alerting`

## Step order

1. Finalize the ML script and artifact paths.
2. Create a Maven Java project with packages matching the PPT.
3. Implement core models and rules.
4. Add file handling, alert logging, and test cases.
5. Add JDBC only after the rule engine runs correctly through CSV input.
