package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AiDtos;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class AiAnalysisService {
	public AiDtos.AnalyzeResponse analyze(List<Transaction> transactions) {
		if (transactions == null) transactions = List.of();

		BigDecimal totalDebit = BigDecimal.ZERO;
		BigDecimal totalCredit = BigDecimal.ZERO;
		Map<String, BigDecimal> debitByCategory = new HashMap<>();

		LocalDate min = null;
		LocalDate max = null;

		for (Transaction t : transactions) {
			if (t.getDate() != null) {
				min = (min == null || t.getDate().isBefore(min)) ? t.getDate() : min;
				max = (max == null || t.getDate().isAfter(max)) ? t.getDate() : max;
			}

			BigDecimal amt = Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO);
			if (t.getType() == TransactionType.DEBIT) {
				totalDebit = totalDebit.add(amt);
				String cat = Optional.ofNullable(t.getCategory()).orElse("uncategorized").trim();
				debitByCategory.merge(cat, amt, BigDecimal::add);
			} else if (t.getType() == TransactionType.CREDIT) {
				totalCredit = totalCredit.add(amt);
			}
		}

		String period = (min != null && max != null) ? (min + " to " + max) : "all time";

		var insights = new ArrayList<String>();
		var suggestions = new ArrayList<String>();

		if (totalDebit.compareTo(BigDecimal.ZERO) == 0 && totalCredit.compareTo(BigDecimal.ZERO) == 0) {
			insights.add("No transactions found yet.");
			suggestions.add("Add a few transactions to unlock insights.");
			return new AiDtos.AnalyzeResponse(period, totalDebit, totalCredit, Map.of(), insights, suggestions);
		}

		if (totalDebit.compareTo(BigDecimal.ZERO) > 0) {
			insights.add("Total spending (debit): ₹" + money(totalDebit));
		}
		if (totalCredit.compareTo(BigDecimal.ZERO) > 0) {
			insights.add("Total income (credit): ₹" + money(totalCredit));
		}

		// Top category share
		if (!debitByCategory.isEmpty() && totalDebit.compareTo(BigDecimal.ZERO) > 0) {
			var top = debitByCategory.entrySet().stream()
					.max(Map.Entry.comparingByValue())
					.orElse(null);
			if (top != null) {
				BigDecimal pct = top.getValue()
						.multiply(BigDecimal.valueOf(100))
						.divide(totalDebit, 1, RoundingMode.HALF_UP);
				insights.add("Top spending category: " + top.getKey() + " (" + pct + "% of debit)");

				// Suggest reduce if >40%
				if (pct.compareTo(BigDecimal.valueOf(40)) >= 0) {
					BigDecimal target = top.getValue().multiply(BigDecimal.valueOf(0.15)); // suggest 15% cut
					suggestions.add("Consider reducing " + top.getKey() + " by ~₹" + money(target) + " to save more.");
				}
			}
		}

		// Savings suggestion: if debit > credit
		if (totalCredit.compareTo(BigDecimal.ZERO) > 0 && totalDebit.compareTo(totalCredit) > 0) {
			BigDecimal diff = totalDebit.subtract(totalCredit);
			suggestions.add("Your spending exceeds income by ₹" + money(diff) + ". Try setting category budgets.");
		} else if (totalCredit.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal savings = totalCredit.subtract(totalDebit);
			suggestions.add("You saved approximately ₹" + money(savings) + " in this period.");
		}

		// Unusual activity: single large debit outlier vs median-ish heuristic
		Optional<Transaction> maxDebitTx = transactions.stream()
				.filter(t -> t.getType() == TransactionType.DEBIT)
				.max(Comparator.comparing(t -> Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO)));
		maxDebitTx.ifPresent(tx -> {
			BigDecimal amt = Optional.ofNullable(tx.getAmount()).orElse(BigDecimal.ZERO);
			if (amt.compareTo(BigDecimal.valueOf(50000)) >= 0) {
				insights.add("Unusual spend detected: ₹" + money(amt) + " on " + tx.getCategory() + " (" + tx.getDate() + ")");
			}
		});

		// Return stable ordering of categories
		Map<String, BigDecimal> sortedCats = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		sortedCats.putAll(debitByCategory);

		return new AiDtos.AnalyzeResponse(period, totalDebit, totalCredit, sortedCats, insights, suggestions);
	}

	private String money(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}

