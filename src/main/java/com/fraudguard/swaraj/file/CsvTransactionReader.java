package com.fraudguard.swaraj.file;

import com.fraudguard.deep.dto.TransactionRequest;
import com.fraudguard.hardik.exception.InvalidTransactionException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import java.util.function.Consumer;

@Component
public class CsvTransactionReader {

    public void readAndProcess(MultipartFile file, Consumer<TransactionRequest> processor) {
        if (file == null || file.isEmpty()) {
            throw new InvalidTransactionException("CSV file is empty.");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                if (firstLine) {
                    firstLine = false;
                    if (looksLikeHeader(line)) {
                        continue;
                    }
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    throw new InvalidTransactionException(
                            "Each CSV row must contain 6 columns: transactionId,transactionType,amount,transactionTime,sourceAccountId,destinationAccountId"
                    );
                }

                processor.accept(new TransactionRequest(
                        parts[0].trim(),
                        parts[1].trim(),
                        new BigDecimal(parts[2].trim()),
                        parts[3].trim(),
                        emptyToNull(parts[4]),
                        emptyToNull(parts[5])
                ));
            }
        } catch (IOException exception) {
            throw new InvalidTransactionException("Failed to read CSV file.");
        } catch (NumberFormatException exception) {
            throw new InvalidTransactionException("CSV amount column must be numeric.");
        }
    }

    public List<TransactionRequest> read(MultipartFile file) {
        List<TransactionRequest> list = new ArrayList<>();
        readAndProcess(file, list::add);
        return list;
    }

    private boolean looksLikeHeader(String line) {
        return line.toLowerCase().contains("transactionid")
                || line.toLowerCase().contains("transaction_type");
    }

    private String emptyToNull(String value) {
        String trimmed = value == null ? null : value.trim();
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
