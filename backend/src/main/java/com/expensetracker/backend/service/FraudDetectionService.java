package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class FraudDetectionService {
	private static final BigDecimal LARGE_AMOUNT = BigDecimal.valueOf(50_000);
	private static final int TOO_MANY_TX_PER_DAY = 10;
	private static final int RECENT_DAYS_WINDOW = 30;
	private static final BigDecimal AVG_MULTIPLIER = BigDecimal.valueOf(3);
	private static final BigDecimal RECENT_MAX_MULTIPLIER = BigDecimal.valueOf(2);
	private static final BigDecimal ROUND_BASE_5K = BigDecimal.valueOf(5_000);
	private static final BigDecimal ROUND_BASE_10K = BigDecimal.valueOf(10_000);
	private static final int ROUND_REPEAT_RECENT_THRESHOLD = 3;
	private static final int ROUND_REPEAT_OVERALL_THRESHOLD = 5;

	public List<String> check(List<Transaction> transactions) {
		if (transactions == null) transactions = List.of();

		var reasons = new ArrayList<String>();

		// Baselines for "unusually large compared to normal"
		var debitTxs = transactions.stream()
				.filter(t -> t.getType() == TransactionType.DEBIT)
				.filter(t -> t.getAmount() != null)
				.toList();

		BigDecimal averageDebit = averageAmount(debitTxs);
		BigDecimal maxRecentDebit = maxRecentDebit(debitTxs);
		LocalDate recentWindowEnd = latestDate(debitTxs);
		LocalDate recentWindowStart = recentWindowEnd != null ? recentWindowEnd.minusDays(RECENT_DAYS_WINDOW) : null;

		// Rule: repeated round-number debits (e.g., ₹5000/₹10000) may indicate scripted/structured activity
		if (!debitTxs.isEmpty()) {
			Map<String, Integer> overallCounts = new HashMap<>();
			Map<String, Integer> recentCounts = new HashMap<>();

			for (Transaction t : debitTxs) {
				BigDecimal amt = t.getAmount();
				if (amt == null || amt.compareTo(BigDecimal.ZERO) <= 0) continue;
				if (!isRoundSuspicious(amt)) continue;

				String key = money(amt);
				overallCounts.merge(key, 1, Integer::sum);

				if (recentWindowStart != null && t.getDate() != null && !t.getDate().isBefore(recentWindowStart)) {
					recentCounts.merge(key, 1, Integer::sum);
				}
			}

			overallCounts.forEach((amountKey, count) -> {
				if (count >= ROUND_REPEAT_OVERALL_THRESHOLD) {
					reasons.add("Repeated round-number debit: ₹" + amountKey + " occurred " + count + " times");
				}
			});
			recentCounts.forEach((amountKey, count) -> {
				if (count >= ROUND_REPEAT_RECENT_THRESHOLD) {
					reasons.add("Repeated round-number debit (recent): ₹" + amountKey + " occurred " + count
							+ " times in last " + RECENT_DAYS_WINDOW + " days");
				}
			});
		}

		if (averageDebit.compareTo(BigDecimal.ZERO) > 0 || maxRecentDebit.compareTo(BigDecimal.ZERO) > 0) {
			for (Transaction t : debitTxs) {
				BigDecimal amt = t.getAmount();

				if (averageDebit.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal threshold = averageDebit.multiply(AVG_MULTIPLIER);
					if (amt.compareTo(threshold) > 0) {
						reasons.add("Unusually large debit vs average: ₹" + money(amt)
								+ " (> " + AVG_MULTIPLIER.toPlainString() + "× avg ₹" + money(averageDebit) + ") on "
								+ t.getCategory() + " (" + t.getDate() + ")");
					}
				}

				if (maxRecentDebit.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal threshold = maxRecentDebit.multiply(RECENT_MAX_MULTIPLIER);
					if (amt.compareTo(threshold) > 0) {
						reasons.add("Unusually large debit vs recent max: ₹" + money(amt)
								+ " (> " + RECENT_MAX_MULTIPLIER.toPlainString() + "× recent max ₹" + money(maxRecentDebit) + ") on "
								+ t.getCategory() + " (" + t.getDate() + ")");
					}
				}
			}
		}

		// Rule 1: any single debit above threshold
		Optional<Transaction> large = transactions.stream()
				.filter(t -> t.getType() == TransactionType.DEBIT)
				.max(Comparator.comparing(t -> Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO)));
		large.ifPresent(t -> {
			BigDecimal amt = Optional.ofNullable(t.getAmount()).orElse(BigDecimal.ZERO);
			if (amt.compareTo(LARGE_AMOUNT) > 0) {
				reasons.add("High-value debit detected: ₹" + money(amt) + " on " + t.getCategory() + " (" + t.getDate() + ")");
			}
		});

		// Rule 2: too many transactions in a short time window (per day heuristic)
		var countsByDay = transactions.stream()
				.map(Transaction::getDate)
				.filter(d -> d != null)
				.reduce(new java.util.HashMap<LocalDate, Integer>(),
						(map, d) -> {
							map.merge(d, 1, Integer::sum);
							return map;
						},
						(m1, m2) -> {
							m2.forEach((k, v) -> m1.merge(k, v, Integer::sum));
							return m1;
						});

		countsByDay.forEach((day, count) -> {
			if (count >= TOO_MANY_TX_PER_DAY) {
				reasons.add("Unusually high activity: " + count + " transactions on " + day);
			}
		});

		return reasons;
	}

	private LocalDate latestDate(List<Transaction> txs) {
		if (txs == null || txs.isEmpty()) return null;
		return txs.stream()
				.map(Transaction::getDate)
				.filter(d -> d != null)
				.max(LocalDate::compareTo)
				.orElse(null);
	}

	private boolean isRoundSuspicious(BigDecimal amount) {
		// Treat exact multiples of 5k or 10k as "round" patterns; amounts are stored at scale=2.
		BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
		return isMultipleOf(normalized, ROUND_BASE_10K) || isMultipleOf(normalized, ROUND_BASE_5K);
	}

	private boolean isMultipleOf(BigDecimal value, BigDecimal base) {
		if (base.compareTo(BigDecimal.ZERO) <= 0) return false;
		try {
			return value.remainder(base).compareTo(BigDecimal.ZERO) == 0;
		} catch (ArithmeticException e) {
			return false;
		}
	}

	private BigDecimal averageAmount(List<Transaction> debitTxs) {
		if (debitTxs == null || debitTxs.isEmpty()) return BigDecimal.ZERO;
		BigDecimal sum = BigDecimal.ZERO;
		int count = 0;
		for (Transaction t : debitTxs) {
			if (t.getAmount() == null) continue;
			sum = sum.add(t.getAmount());
			count++;
		}
		if (count == 0) return BigDecimal.ZERO;
		return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal maxRecentDebit(List<Transaction> debitTxs) {
		if (debitTxs == null || debitTxs.isEmpty()) return BigDecimal.ZERO;

		LocalDate maxDate = debitTxs.stream()
				.map(Transaction::getDate)
				.filter(d -> d != null)
				.max(LocalDate::compareTo)
				.orElse(null);
		if (maxDate == null) return BigDecimal.ZERO;

		LocalDate start = maxDate.minusDays(RECENT_DAYS_WINDOW);
		return debitTxs.stream()
				.filter(t -> t.getDate() != null)
				.filter(t -> !t.getDate().isBefore(start))
				.map(Transaction::getAmount)
				.filter(a -> a != null)
				.max(BigDecimal::compareTo)
				.orElse(BigDecimal.ZERO);
	}

	private String money(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}

