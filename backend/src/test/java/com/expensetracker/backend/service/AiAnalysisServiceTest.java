package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AiDtos;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AiAnalysisServiceTest {

    private AiAnalysisService aiAnalysisService;

    @BeforeEach
    void setUp() {
        aiAnalysisService = new AiAnalysisService();
    }

    @Test
    void analyze_NullOrEmptyList_ReturnsDefaultResponse() {
        AiDtos.AnalyzeResponse response1 = aiAnalysisService.analyze(null);
        assertNotNull(response1);
        assertEquals(BigDecimal.ZERO, response1.getTotalDebit());
        assertEquals(BigDecimal.ZERO, response1.getTotalCredit());
        assertTrue(response1.getInsights().contains("No transactions found yet."));

        AiDtos.AnalyzeResponse response2 = aiAnalysisService.analyze(List.of());
        assertNotNull(response2);
        assertTrue(response2.getInsights().contains("No transactions found yet."));
    }

    @Test
    void analyze_CalculatesTotalsCorrectly() {
        List<Transaction> transactions = List.of(
                Transaction.builder().amount(BigDecimal.valueOf(100)).type(TransactionType.DEBIT).category("Food").date(LocalDate.now()).build(),
                Transaction.builder().amount(BigDecimal.valueOf(50)).type(TransactionType.DEBIT).category("Travel").date(LocalDate.now()).build(),
                Transaction.builder().amount(BigDecimal.valueOf(200)).type(TransactionType.CREDIT).category("Salary").date(LocalDate.now()).build()
        );

        AiDtos.AnalyzeResponse response = aiAnalysisService.analyze(transactions);

        assertEquals(BigDecimal.valueOf(150), response.getTotalDebit());
        assertEquals(BigDecimal.valueOf(200), response.getTotalCredit());
        assertEquals(2, response.getDebitByCategory().size());
        assertEquals(BigDecimal.valueOf(100), response.getDebitByCategory().get("Food"));
    }

    @Test
    void analyze_SpendingExceedsIncome_ReturnsSuggestion() {
        List<Transaction> transactions = List.of(
                Transaction.builder().amount(BigDecimal.valueOf(300)).type(TransactionType.DEBIT).category("Food").date(LocalDate.now()).build(),
                Transaction.builder().amount(BigDecimal.valueOf(200)).type(TransactionType.CREDIT).category("Salary").date(LocalDate.now()).build()
        );

        AiDtos.AnalyzeResponse response = aiAnalysisService.analyze(transactions);

        assertTrue(response.getSuggestions().stream().anyMatch(s -> s.contains("Your spending exceeds income")));
    }

    @Test
    void analyze_IncomeExceedsSpending_ReturnsSavingsSuggestion() {
        List<Transaction> transactions = List.of(
                Transaction.builder().amount(BigDecimal.valueOf(100)).type(TransactionType.DEBIT).category("Food").date(LocalDate.now()).build(),
                Transaction.builder().amount(BigDecimal.valueOf(500)).type(TransactionType.CREDIT).category("Salary").date(LocalDate.now()).build()
        );

        AiDtos.AnalyzeResponse response = aiAnalysisService.analyze(transactions);

        assertTrue(response.getSuggestions().stream().anyMatch(s -> s.contains("You saved approximately")));
    }

    @Test
    void analyze_TopCategoryShare_SuggestsReductionIfHigh() {
        List<Transaction> transactions = List.of(
                Transaction.builder().amount(BigDecimal.valueOf(800)).type(TransactionType.DEBIT).category("Rent").date(LocalDate.now()).build(),
                Transaction.builder().amount(BigDecimal.valueOf(200)).type(TransactionType.DEBIT).category("Food").date(LocalDate.now()).build()
        );

        AiDtos.AnalyzeResponse response = aiAnalysisService.analyze(transactions);

        assertTrue(response.getInsights().stream().anyMatch(i -> i.contains("Top spending category: Rent")));
        // 80% is > 40%, so it should suggest reducing
        assertTrue(response.getSuggestions().stream().anyMatch(s -> s.contains("Consider reducing Rent")));
    }

    @Test
    void analyze_UnusualSpendDetection_FlagsLargeDebit() {
        List<Transaction> transactions = List.of(
                Transaction.builder().amount(BigDecimal.valueOf(50001)).type(TransactionType.DEBIT).category("Electronics").date(LocalDate.now()).build()
        );

        AiDtos.AnalyzeResponse response = aiAnalysisService.analyze(transactions);

        assertTrue(response.getInsights().stream().anyMatch(i -> i.contains("Unusual spend detected")));
    }
}
