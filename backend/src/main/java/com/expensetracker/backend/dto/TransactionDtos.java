package com.expensetracker.backend.dto;

import com.expensetracker.backend.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDtos {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CreateTransactionRequest {
		@NotNull
		@DecimalMin(value = "0.01")
		private BigDecimal amount;

		@NotNull
		private TransactionType type;

		@NotBlank
		private String category;

		@NotNull
		private LocalDate date;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TransactionResponse {
		private Long id;
		private BigDecimal amount;
		private TransactionType type;
		private String category;
		private LocalDate date;
	}
}

