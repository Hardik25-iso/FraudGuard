package com.fraudguard.hardik.profile;

import com.fraudguard.hardik.model.profile.TransactionProfile;
import com.fraudguard.hardik.model.transaction.Transaction;
import com.fraudguard.hardik.model.transaction.TransactionType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionProfileService {

    private final Cache<String, TransactionProfile> profiles = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();
            
    public TransactionProfile getProfile(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return null;
        }
        return profiles.get(accountId, TransactionProfile::new);
    }

    public TransactionProfile buildEvaluationProfile(Transaction transaction) {
        String profileAccountId = resolveProfileAccountId(transaction);
        if (profileAccountId == null) {
            return null;
        }

        TransactionProfile storedProfile = getProfile(profileAccountId);
        
        // Synchronized deep copy to ensure thread safety during evaluation
        synchronized (storedProfile) {
            TransactionProfile evaluationProfile = new TransactionProfile(profileAccountId);
            evaluationProfile.setLastTransactionTime(storedProfile.getLastTransactionTime());
            evaluationProfile.setLastResetDate(storedProfile.getLastResetDate());
            evaluationProfile.setTransactionCountLastMinute(storedProfile.getTransactionCountLastMinute());
            evaluationProfile.setTotalTransactionCount(storedProfile.getTotalTransactionCount());
            evaluationProfile.setAverageTransactionAmount(storedProfile.getAverageTransactionAmount());
            evaluationProfile.setTotalWithdrawalToday(storedProfile.getTotalWithdrawalToday());
            evaluationProfile.getRecentTimestamps().addAll(storedProfile.getRecentTimestamps());

            // Tentatively apply this transaction for rule evaluation
            applyTransactionToProfileInternal(evaluationProfile, transaction);
            return evaluationProfile;
        }
    }

    public void updateProfile(Transaction transaction) {
        String profileAccountId = resolveProfileAccountId(transaction);
        if (profileAccountId == null) {
            return;
        }

        TransactionProfile profile = getProfile(profileAccountId);
        synchronized (profile) {
            applyTransactionToProfileInternal(profile, transaction);
        }
    }
    
    private void applyTransactionToProfileInternal(TransactionProfile profile, Transaction transaction) {
        LocalDate currentDate = transaction.getTransactionTime().toLocalDate();
        LocalDate lastResetDate = profile.getLastResetDate();

        if (lastResetDate == null || !lastResetDate.equals(currentDate)) {
            profile.setTotalWithdrawalToday(BigDecimal.ZERO);
            profile.setLastResetDate(currentDate);
        }

        // Sliding window logic:
        LocalDateTime cutoff = transaction.getTransactionTime().minusSeconds(60);
        profile.getRecentTimestamps().addLast(transaction.getTransactionTime());
        
        while (!profile.getRecentTimestamps().isEmpty() && 
               profile.getRecentTimestamps().peekFirst().isBefore(cutoff)) {
            profile.getRecentTimestamps().pollFirst();
        }
        profile.setTransactionCountLastMinute(profile.getRecentTimestamps().size());

        if (transaction.getTransactionType() == TransactionType.WITHDRAWAL
                || transaction.getTransactionType() == TransactionType.TRANSFER) {
            profile.setTotalWithdrawalToday(
                    profile.getTotalWithdrawalToday().add(transaction.getAmount())
            );
        }

        int newCount = profile.getTotalTransactionCount() + 1;
        BigDecimal previousTotal = profile.getAverageTransactionAmount().multiply(new BigDecimal(profile.getTotalTransactionCount()));
        BigDecimal newAverage = previousTotal.add(transaction.getAmount()).divide(new BigDecimal(newCount), 2, RoundingMode.HALF_UP);
        
        profile.setTotalTransactionCount(newCount);
        profile.setAverageTransactionAmount(newAverage);
        profile.setLastTransactionTime(transaction.getTransactionTime());
    }

    private String resolveProfileAccountId(Transaction transaction) {
        if (transaction.getSourceAccountId() != null && !transaction.getSourceAccountId().isBlank()) {
            return transaction.getSourceAccountId();
        }
        return transaction.getDestinationAccountId();
    }
}
