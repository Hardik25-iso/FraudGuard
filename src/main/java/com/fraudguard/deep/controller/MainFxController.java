package com.fraudguard.deep.controller;

import com.fraudguard.deep.dto.AlertRecordResponse;
import com.fraudguard.deep.dto.FraudAnalysisResponse;
import com.fraudguard.deep.dto.TransactionAuditRecordResponse;
import com.fraudguard.deep.dto.TransactionRequest;
import com.fraudguard.deep.repository.FraudAlertRepository;
import com.fraudguard.deep.repository.TransactionAuditRepository;
import com.fraudguard.deep.service.account.BankAccountRegistry;
import com.fraudguard.deep.service.analysis.FraudAnalysisService;
import com.fraudguard.hardik.model.account.BankAccount;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

@Component
public class MainFxController {

    private final FraudAnalysisService fraudAnalysisService;
    private final TransactionAuditRepository transactionAuditRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final BankAccountRegistry bankAccountRegistry;

    @FXML private Label statusLabel;
    @FXML private TableView<TransactionAuditRecordResponse> auditTable;
    @FXML private TableColumn<TransactionAuditRecordResponse, String> colTxnId;
    @FXML private TableColumn<TransactionAuditRecordResponse, String> colType;
    @FXML private TableColumn<TransactionAuditRecordResponse, String> colAmount;
    @FXML private TableColumn<TransactionAuditRecordResponse, Integer> colRisk;
    @FXML private TableColumn<TransactionAuditRecordResponse, Boolean> colFlagged;
    
    @FXML private LineChart<String, Number> riskChart;
    @FXML private PieChart riskPieChart;
    @FXML private BarChart<String, Number> volumeBarChart;

    @FXML private VBox dashboardPane;
    @FXML private VBox analyzerPane;
    @FXML private VBox alertsPane;
    @FXML private VBox accountsPane;
    @FXML private VBox settingsPane;
    
    @FXML private Button btnDashboard;
    @FXML private Button btnAnalyzer;
    @FXML private Button btnAlerts;
    @FXML private Button btnAccounts;
    @FXML private Button btnSimulate;
    @FXML private Button btnSettings;

    @FXML private TextField txtTxnId;
    @FXML private ComboBox<String> comboTxnType;
    @FXML private TextField txtAmount;
    @FXML private TextField txtTxnTime;
    @FXML private TextField txtSource;
    @FXML private TextField txtDest;
    @FXML private Label analysisResultLabel;

    @FXML private TableView<AlertRecordResponse> alertsTable;
    @FXML private TableColumn<AlertRecordResponse, String> colAlertTxnId;
    @FXML private TableColumn<AlertRecordResponse, Integer> colAlertRisk;
    @FXML private TableColumn<AlertRecordResponse, String> colAlertLevel;
    @FXML private TableColumn<AlertRecordResponse, String> colAlertRules;
    @FXML private TableColumn<AlertRecordResponse, String> colAlertReasons;
    
    @FXML private TableView<BankAccount> accountsTable;
    @FXML private TableColumn<BankAccount, String> colAccId;
    @FXML private TableColumn<BankAccount, String> colAccName;
    @FXML private TableColumn<BankAccount, String> colAccType;
    @FXML private TableColumn<BankAccount, String> colAccBalance;

    private final ObservableList<TransactionAuditRecordResponse> auditData = FXCollections.observableArrayList();
    private final ObservableList<AlertRecordResponse> alertData = FXCollections.observableArrayList();
    private final ObservableList<BankAccount> accountData = FXCollections.observableArrayList();

    private Timeline simulatorTimeline;

    public MainFxController(FraudAnalysisService fraudAnalysisService, 
                            TransactionAuditRepository transactionAuditRepository,
                            FraudAlertRepository fraudAlertRepository,
                            BankAccountRegistry bankAccountRegistry) {
        this.fraudAnalysisService = fraudAnalysisService;
        this.transactionAuditRepository = transactionAuditRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.bankAccountRegistry = bankAccountRegistry;
    }

    @FXML
    public void initialize() {
        // Fix for Java Records: records don't have getX() methods, so PropertyValueFactory fails.
        colTxnId.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().transactionId()));
        colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().transactionType()));
        colAmount.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().amount()));
        colRisk.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().riskScore()));
        colFlagged.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().flagged()));

        if (colAlertTxnId != null) {
            colAlertTxnId.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().transactionId()));
            colAlertRisk.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().riskScore()));
            colAlertLevel.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().riskLevel()));
            colAlertRules.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().triggeredRules()));
            colAlertReasons.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().reasons()));
            alertsTable.setItems(alertData);
        }

        if (colAccId != null) {
            colAccId.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAccountId()));
            colAccName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAccountHolderName()));
            colAccType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAccountType().name()));
            colAccBalance.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%,.2f", cd.getValue().getBalance())));
            accountsTable.setItems(accountData);
        }

        auditTable.setItems(auditData);
        refreshData();
        refreshAlerts();
    }

    private void resetNavButtons() {
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnDashboard.getStyleClass().add("nav-button");
        btnAnalyzer.getStyleClass().remove("nav-button-active");
        btnAnalyzer.getStyleClass().add("nav-button");
        btnAlerts.getStyleClass().remove("nav-button-active");
        btnAlerts.getStyleClass().add("nav-button");
        btnAccounts.getStyleClass().remove("nav-button-active");
        btnAccounts.getStyleClass().add("nav-button");
        btnSettings.getStyleClass().remove("nav-button-active");
        btnSettings.getStyleClass().add("nav-button");
        
        dashboardPane.setVisible(false);
        analyzerPane.setVisible(false);
        alertsPane.setVisible(false);
        accountsPane.setVisible(false);
        settingsPane.setVisible(false);
    }

    @FXML
    public void showDashboard() {
        resetNavButtons();
        dashboardPane.setVisible(true);
        btnDashboard.getStyleClass().remove("nav-button");
        btnDashboard.getStyleClass().add("nav-button-active");
        refreshData();
    }

    @FXML
    public void showAnalyzer() {
        resetNavButtons();
        analyzerPane.setVisible(true);
        btnAnalyzer.getStyleClass().remove("nav-button");
        btnAnalyzer.getStyleClass().add("nav-button-active");
        analysisResultLabel.setText("");
    }

    @FXML
    public void showAlerts() {
        resetNavButtons();
        alertsPane.setVisible(true);
        btnAlerts.getStyleClass().remove("nav-button");
        btnAlerts.getStyleClass().add("nav-button-active");
        refreshAlerts();
    }

    @FXML
    public void showAccounts() {
        resetNavButtons();
        accountsPane.setVisible(true);
        btnAccounts.getStyleClass().remove("nav-button");
        btnAccounts.getStyleClass().add("nav-button-active");
        refreshAccounts();
    }

    @FXML
    public void showSettings() {
        resetNavButtons();
        settingsPane.setVisible(true);
        btnSettings.getStyleClass().remove("nav-button");
        btnSettings.getStyleClass().add("nav-button-active");
    }

    @FXML
    public void refreshAlerts() {
        List<AlertRecordResponse> alerts = fraudAlertRepository.findRecentAlerts();
        alertData.setAll(alerts);
    }

    @FXML
    public void refreshAccounts() {
        List<BankAccount> accounts = bankAccountRegistry.getAllAccounts();
        accountData.setAll(accounts);
    }

    @FXML
    public void toggleSimulation() {
        if (simulatorTimeline != null) {
            simulatorTimeline.stop();
            simulatorTimeline = null;
            btnSimulate.setText("START SIMULATION");
            btnSimulate.setStyle("-fx-background-color: #a855f7; -fx-text-fill: white;");
            statusLabel.setText("SIMULATION STOPPED");
        } else {
            btnSimulate.setText("STOPPING...");
            btnSimulate.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
            statusLabel.setText("SIMULATION RUNNING");
            
            simulatorTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
                runSimulatedTransaction();
            }));
            simulatorTimeline.setCycleCount(Timeline.INDEFINITE);
            simulatorTimeline.play();
        }
    }

    private void runSimulatedTransaction() {
        List<BankAccount> accounts = bankAccountRegistry.getAllAccounts();
        if (accounts.isEmpty()) return;

        boolean isFraud = Math.random() > 0.7;
        String[] types = {"TRANSFER", "WITHDRAWAL", "DEPOSIT"};
        String type = types[(int)(Math.random() * types.length)];
        
        BankAccount srcAcc = accounts.get((int)(Math.random() * accounts.size()));
        BankAccount destAcc = accounts.get((int)(Math.random() * accounts.size()));
        
        String source = "DEPOSIT".equals(type) ? null : srcAcc.getAccountId();
        String dest = "WITHDRAWAL".equals(type) ? null : destAcc.getAccountId();
        
        // Make standard transactions lower to not trigger LargeAmount rules,
        // and make fraud transactions realistic based on the rule thresholds (e.g. > 500k for high risk)
        BigDecimal amt = BigDecimal.valueOf(isFraud ? (Math.random() * 800000 + 600000) : (Math.random() * 5000 + 100));
        
        TransactionRequest request = new TransactionRequest(
                "SIM-" + (int)(Math.random() * 100000),
                type,
                amt,
                null,
                source,
                dest
        );
        
        try {
            fraudAnalysisService.analyzeTransaction(request);
            refreshData();
            refreshAlerts();
            if (accountsPane.isVisible()) refreshAccounts();
        } catch (Exception e) {
            // Ignore simulation errors (e.g. Insufficient balance)
        }
    }

    @FXML
    public void handleAnalyze() {
        try {
            String amtStr = txtAmount.getText();
            BigDecimal amount = (amtStr != null && !amtStr.isBlank()) ? new BigDecimal(amtStr) : BigDecimal.ZERO;
            
            TransactionRequest request = new TransactionRequest(
                    txtTxnId.getText(),
                    comboTxnType.getValue(),
                    amount,
                    txtTxnTime.getText(),
                    txtSource.getText(),
                    txtDest.getText()
            );

            FraudAnalysisResponse response = fraudAnalysisService.analyzeTransaction(request);
            
            String resultText = String.format("Risk Score: %d | Level: %s | Flagged: %b\nTriggered Rules: %s\nReasons: %s",
                    response.riskScore(),
                    response.riskLevel(),
                    response.flagged(),
                    String.join(", ", response.triggeredRules()),
                    String.join(", ", response.reasons())
            );
            analysisResultLabel.setText(resultText);
            
            if (response.flagged()) {
                analysisResultLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;"); // Red 500
                refreshAlerts();
            } else {
                analysisResultLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;"); // Green 500
            }
            refreshData();
            if (accountsPane.isVisible()) refreshAccounts();
        } catch (Exception e) {
            analysisResultLabel.setText("ERROR: " + e.getMessage());
            analysisResultLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void refreshData() {
        List<TransactionAuditRecordResponse> audits = transactionAuditRepository.findRecentTransactions();
        auditData.setAll(audits);
        updateChart(audits);
        statusLabel.setText("DATA SYNCED: " + audits.size() + " RECORDS");
    }

    private void updateChart(List<TransactionAuditRecordResponse> audits) {
        if (riskChart != null) {
            riskChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Risk Scores");

            List<TransactionAuditRecordResponse> recent = audits.stream().limit(15).toList();
            for (int i = recent.size() - 1; i >= 0; i--) {
                TransactionAuditRecordResponse record = recent.get(i);
                series.getData().add(new XYChart.Data<>(record.transactionId(), record.riskScore()));
            }
            riskChart.getData().add(series);
        }

        if (riskPieChart != null) {
            long low = audits.stream().filter(a -> "LOW".equals(a.riskLevel())).count();
            long medium = audits.stream().filter(a -> "MEDIUM".equals(a.riskLevel())).count();
            long high = audits.stream().filter(a -> "HIGH".equals(a.riskLevel())).count();
            
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Low Risk", low),
                new PieChart.Data("Medium Risk", medium),
                new PieChart.Data("High Risk", high)
            );
            riskPieChart.setData(pieData);
        }

        if (volumeBarChart != null) {
            volumeBarChart.getData().clear();
            long dep = audits.stream().filter(a -> "DEPOSIT".equals(a.transactionType())).count();
            long wit = audits.stream().filter(a -> "WITHDRAWAL".equals(a.transactionType())).count();
            long trans = audits.stream().filter(a -> "TRANSFER".equals(a.transactionType())).count();

            XYChart.Series<String, Number> volSeries = new XYChart.Series<>();
            volSeries.setName("Volume");
            volSeries.getData().add(new XYChart.Data<>("DEPOSIT", dep));
            volSeries.getData().add(new XYChart.Data<>("WITHDRAWAL", wit));
            volSeries.getData().add(new XYChart.Data<>("TRANSFER", trans));
            
            volumeBarChart.getData().add(volSeries);
        }
    }

    @FXML
    public void handleCsvUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(auditTable.getScene().getWindow());

        if (file != null) {
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", content);
                fraudAnalysisService.analyzeCsv(multipartFile);
                refreshData();
            } catch (Exception e) {
                statusLabel.setText("UPLOAD FAILED: " + e.getMessage());
            }
        }
    }
}
