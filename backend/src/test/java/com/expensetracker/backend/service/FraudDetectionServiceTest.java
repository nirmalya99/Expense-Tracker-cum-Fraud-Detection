package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FraudDetectionServiceTest {

    private FraudDetectionService fraudDetectionService;

    @BeforeEach
    void setUp() {
        fraudDetectionService = new FraudDetectionService();
    }

    @Test
    void check_NullOrEmptyList_ReturnsEmptyReasons() {
        assertTrue(fraudDetectionService.check(null).isEmpty());
        assertTrue(fraudDetectionService.check(List.of()).isEmpty());
    }

    @Test
    void check_HighValueDebit_FlagsTransaction() {
        Transaction tx = Transaction.builder()
                .amount(BigDecimal.valueOf(50001))
                .type(TransactionType.DEBIT)
                .category("Electronics")
                .date(LocalDate.now())
                .build();

        List<String> reasons = fraudDetectionService.check(List.of(tx));

        assertFalse(reasons.isEmpty());
        assertTrue(reasons.stream().anyMatch(r -> r.contains("High-value debit detected")));
    }

    @Test
    void check_TooManyTransactionsInOneDay_FlagsActivity() {
        List<Transaction> transactions = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Add 10 transactions for the same day (threshold is 10)
        for (int i = 0; i < 10; i++) {
            transactions.add(Transaction.builder()
                    .amount(BigDecimal.valueOf(100))
                    .type(TransactionType.DEBIT)
                    .category("Food")
                    .date(today)
                    .build());
        }

        List<String> reasons = fraudDetectionService.check(transactions);

        assertFalse(reasons.isEmpty());
        assertTrue(reasons.stream().anyMatch(r -> r.contains("Unusually high activity")));
    }

    @Test
    void check_RepeatedRoundNumberDebits_FlagsActivity() {
        List<Transaction> transactions = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Threshold for recent is 3, threshold for overall is 5.
        // We'll add 3 recent transactions of 10,000 to trigger the recent round-number rule
        for (int i = 0; i < 3; i++) {
            transactions.add(Transaction.builder()
                    .amount(BigDecimal.valueOf(10000))
                    .type(TransactionType.DEBIT)
                    .category("Cash Withdrawal")
                    .date(today)
                    .build());
        }

        List<String> reasons = fraudDetectionService.check(transactions);

        assertFalse(reasons.isEmpty());
        assertTrue(reasons.stream().anyMatch(r -> r.contains("Repeated round-number debit (recent)")));
    }

    @Test
    void check_UnusuallyLargeDebitVsAverage_FlagsTransaction() {
        List<Transaction> transactions = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 3 small transactions to set a low average (average = 100)
        for (int i = 0; i < 3; i++) {
            transactions.add(Transaction.builder()
                    .amount(BigDecimal.valueOf(100))
                    .type(TransactionType.DEBIT)
                    .category("Food")
                    .date(today)
                    .build());
        }

        // 1 large transaction that is > 3x average (e.g. 1000)
        transactions.add(Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .type(TransactionType.DEBIT)
                .category("Misc")
                .date(today)
                .build());

        List<String> reasons = fraudDetectionService.check(transactions);

        assertFalse(reasons.isEmpty());
        assertTrue(reasons.stream().anyMatch(r -> r.contains("Unusually large debit vs average")));
    }
}
