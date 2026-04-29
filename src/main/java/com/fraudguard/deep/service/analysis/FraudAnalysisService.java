package com.fraudguard.deep.service.analysis;

import com.fraudguard.deep.dto.CsvAnalysisResponse;
import com.fraudguard.deep.dto.FraudAnalysisResponse;
import com.fraudguard.deep.dto.TransactionRequest;
import com.fraudguard.hardik.exception.InvalidTransactionException;
import com.fraudguard.hardik.model.alert.FraudAlert;
import com.fraudguard.hardik.model.account.BankAccount;
import com.fraudguard.hardik.model.profile.TransactionProfile;
import com.fraudguard.hardik.model.transaction.Deposit;
import com.fraudguard.hardik.model.transaction.Transaction;
import com.fraudguard.hardik.model.transaction.TransactionType;
import com.fraudguard.hardik.model.transaction.Transfer;
import com.fraudguard.hardik.model.transaction.Withdrawal;
import com.fraudguard.deep.repository.FraudAlertRepository;
import com.fraudguard.deep.repository.TransactionAuditRepository;
import com.fraudguard.deep.service.account.BankAccountRegistry;
import com.fraudguard.tejas.detection.FraudDetectionEngine;
import com.fraudguard.swaraj.file.CsvTransactionReader;
import com.fraudguard.deep.service.logging.AlertLogger;
import com.fraudguard.hardik.profile.TransactionProfileService;
import com.fraudguard.tejas.risk.RiskAssessment;
import com.fraudguard.tejas.risk.RiskLevel;
import com.fraudguard.tejas.rule.FraudRuleContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

@Service
@Transactional
public class FraudAnalysisService {

    private final BankAccountRegistry bankAccountRegistry;
    private final TransactionProfileService transactionProfileService;
    private final CsvTransactionReader csvTransactionReader;
    private final TransactionAuditRepository transactionAuditRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final AlertLogger alertLogger;
    private final ReportingService reportingService;
    private final FraudDetectionEngine fraudDetectionEngine;

    public FraudAnalysisService(
            BankAccountRegistry bankAccountRegistry,
            TransactionProfileService transactionProfileService,
            CsvTransactionReader csvTransactionReader,
            TransactionAuditRepository transactionAuditRepository,
            FraudAlertRepository fraudAlertRepository,
            AlertLogger alertLogger,
            ReportingService reportingService,
            FraudDetectionEngine fraudDetectionEngine
    ) {
        this.bankAccountRegistry = bankAccountRegistry;
        this.transactionProfileService = transactionProfileService;
        this.csvTransactionReader = csvTransactionReader;
        this.transactionAuditRepository = transactionAuditRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.alertLogger = alertLogger;
        this.reportingService = reportingService;
        this.fraudDetectionEngine = fraudDetectionEngine;
    }

    public FraudAnalysisResponse analyzeTransaction(TransactionRequest request) {
        Transaction transaction = buildTransaction(request);

        BankAccount sourceAccount = resolveSourceAccount(transaction);
        BankAccount destinationAccount = resolveDestinationAccount(transaction);

        // Pre-validation: Ensure sufficient funds
        if (sourceAccount != null && (transaction.getTransactionType() == TransactionType.WITHDRAWAL || transaction.getTransactionType() == TransactionType.TRANSFER)) {
            if (sourceAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
                throw new InvalidTransactionException("Insufficient balance in account " + sourceAccount.getAccountId());
            }
        }

        TransactionProfile profile = transactionProfileService.buildEvaluationProfile(transaction);

        FraudRuleContext context = new FraudRuleContext(
                transaction,
                sourceAccount,
                destinationAccount,
                profile
        );

        RiskAssessment assessment = fraudDetectionEngine.analyze(context);
        
        // Register synchronization to update profile ONLY after commit
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    transactionProfileService.updateProfile(transaction);
                }
            });
        } else {
            transactionProfileService.updateProfile(transaction);
        }

        if (assessment.riskLevel() != RiskLevel.HIGH) {
            applyTransaction(transaction, sourceAccount, destinationAccount);
        }
        
        FraudAnalysisResponse response = new FraudAnalysisResponse(
                transaction.getTransactionId(),
                transaction.getTransactionType().name(),
                assessment.riskScore(),
                assessment.riskLevel().name(),
                assessment.riskLevel() == RiskLevel.HIGH,
                assessment.triggeredRules().stream().map(rule -> rule.ruleName()).toList(),
                assessment.triggeredRules().stream().map(rule -> rule.reason()).toList(),
                sourceAccount != null ? String.format("%,.2f", sourceAccount.getBalance()) : "N/A",
                destinationAccount != null ? String.format("%,.2f", destinationAccount.getBalance()) : "N/A"
        );
        persistAnalysis(transaction, response);
        reportingService.generateAsyncReport(response);
        return response;
    }

    public CsvAnalysisResponse analyzeCsv(MultipartFile file) {
        List<FraudAnalysisResponse> responses = new java.util.ArrayList<>();
        
        csvTransactionReader.readAndProcess(file, request -> {
            try {
                // Call analyzeTransaction via self-reference if it needs its own transaction,
                // but since the current class is @Transactional, each call here is in a transaction.
                // Note: Calling a @Transactional method from within the same class doesn't start a new transaction 
                // unless we use AOP proxy. For CSV, we might want each row to be independent.
                responses.add(this.analyzeTransaction(request));
            } catch (Exception e) {
                // Log and continue processing other rows
                responses.add(new FraudAnalysisResponse(
                    request.transactionId(),
                    request.transactionType(),
                    -1,
                    "ERROR",
                    false,
                    List.of("CSV_ERROR"),
                    List.of(e.getMessage()),
                    "N/A", "N/A"
                ));
            }
        });

        int flaggedCount = (int) responses.stream()
                .filter(FraudAnalysisResponse::flagged)
                .count();

        return new CsvAnalysisResponse(responses.size(), flaggedCount, responses);
    }

    private Transaction buildTransaction(TransactionRequest request) {
        if (request == null) {
            throw new InvalidTransactionException("Transaction request cannot be null.");
        }
        if (request.transactionType() == null || request.transactionType().isBlank()) {
            throw new InvalidTransactionException("Transaction type is required.");
        }

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(request.transactionType().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidTransactionException("Unsupported transaction type: " + request.transactionType());
        }

        LocalDateTime transactionTime;
        if (request.transactionTime() == null || request.transactionTime().isBlank()) {
            transactionTime = LocalDateTime.now();
        } else {
            transactionTime = LocalDateTime.parse(request.transactionTime().trim());
        }
        
        return switch (transactionType) {
            case DEPOSIT -> new Deposit(
                    request.transactionId(),
                    request.amount(),
                    transactionTime,
                    request.destinationAccountId()
            );
            case WITHDRAWAL -> new Withdrawal(
                    request.transactionId(),
                    request.amount(),
                    transactionTime,
                    request.sourceAccountId()
            );
            case TRANSFER -> new Transfer(
                    request.transactionId(),
                    request.amount(),
                    transactionTime,
                    request.sourceAccountId(),
                    request.destinationAccountId()
            );
        };
    }

    private BankAccount resolveSourceAccount(Transaction transaction) {
        if (transaction.getSourceAccountId() == null) {
            return null;
        }
        return bankAccountRegistry.getRequiredAccount(transaction.getSourceAccountId());
    }

    private BankAccount resolveDestinationAccount(Transaction transaction) {
        if (transaction.getDestinationAccountId() == null) {
            return null;
        }
        return bankAccountRegistry.getRequiredAccount(transaction.getDestinationAccountId());
    }

    private void applyTransaction(
            Transaction transaction,
            BankAccount sourceAccount,
            BankAccount destinationAccount
    ) {
        switch (transaction.getTransactionType()) {
            case DEPOSIT -> {
                destinationAccount.credit(transaction.getAmount());
                bankAccountRegistry.updateBalance(destinationAccount);
            }
            case WITHDRAWAL -> {
                sourceAccount.debit(transaction.getAmount());
                bankAccountRegistry.updateBalance(sourceAccount);
            }
            case TRANSFER -> {
                sourceAccount.debit(transaction.getAmount());
                destinationAccount.credit(transaction.getAmount());
                bankAccountRegistry.updateBalance(sourceAccount);
                bankAccountRegistry.updateBalance(destinationAccount);
            }
        }
    }

    private void persistAnalysis(Transaction transaction, FraudAnalysisResponse response) {
        transactionAuditRepository.saveTransaction(
                transaction,
                response.riskScore(),
                response.riskLevel(),
                response.flagged()
        );

        if (response.flagged()) {
            FraudAlert alert = new FraudAlert(
                    response.transactionId(),
                    response.riskScore(),
                    response.riskLevel(),
                    joinValues(response.triggeredRules()),
                    joinValues(response.reasons()),
                    LocalDateTime.now()
            );
            fraudAlertRepository.saveAlert(alert);
            alertLogger.log(response);
        }
    }

    private String joinValues(List<String> values) {
        StringJoiner joiner = new StringJoiner(" | ");
        values.forEach(joiner::add);
        return joiner.toString();
    }
}
