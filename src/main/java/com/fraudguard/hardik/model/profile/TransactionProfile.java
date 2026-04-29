package com.fraudguard.hardik.model.profile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

public class TransactionProfile {

    private final String accountId;
    private int transactionCountLastMinute;
    private int totalTransactionCount;
    private BigDecimal totalWithdrawalToday;
    private BigDecimal averageTransactionAmount;
    private LocalDateTime lastTransactionTime;
    private java.time.LocalDate lastResetDate;
    private final Deque<LocalDateTime> recentTimestamps = new ArrayDeque<>();

    public TransactionProfile(String accountId) {
        this.accountId = accountId;
        this.transactionCountLastMinute = 0;
        this.totalTransactionCount = 0;
        this.totalWithdrawalToday = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.averageTransactionAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public java.time.LocalDate getLastResetDate() {
        return lastResetDate;
    }

    public void setLastResetDate(java.time.LocalDate lastResetDate) {
        this.lastResetDate = lastResetDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public int getTotalTransactionCount() {
        return totalTransactionCount;
    }

    public void setTotalTransactionCount(int totalTransactionCount) {
        this.totalTransactionCount = totalTransactionCount;
    }

    public int getTransactionCountLastMinute() {
        return transactionCountLastMinute;
    }

    public void setTransactionCountLastMinute(int transactionCountLastMinute) {
        this.transactionCountLastMinute = transactionCountLastMinute;
    }

    public BigDecimal getTotalWithdrawalToday() {
        return totalWithdrawalToday;
    }

    public void setTotalWithdrawalToday(BigDecimal totalWithdrawalToday) {
        this.totalWithdrawalToday = totalWithdrawalToday.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAverageTransactionAmount() {
        return averageTransactionAmount;
    }

    public void setAverageTransactionAmount(BigDecimal averageTransactionAmount) {
        this.averageTransactionAmount = averageTransactionAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public LocalDateTime getLastTransactionTime() {
        return lastTransactionTime;
    }

    public void setLastTransactionTime(LocalDateTime lastTransactionTime) {
        this.lastTransactionTime = lastTransactionTime;
    }

    public Deque<LocalDateTime> getRecentTimestamps() {
        return recentTimestamps;
    }
}
