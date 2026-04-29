package com.fraudguard.deep.dto;

import java.util.List;

public record CsvAnalysisResponse(
        int totalTransactions,
        int flaggedTransactions,
        List<FraudAnalysisResponse> results
) {
}
