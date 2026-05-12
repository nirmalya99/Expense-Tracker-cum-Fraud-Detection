package com.expensetracker.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AiDtos {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AnalyzeResponse {
		private String period;
		private BigDecimal totalDebit;
		private BigDecimal totalCredit;
		private Map<String, BigDecimal> debitByCategory;
		private List<String> insights;
		private List<String> suggestions;
	}
}

