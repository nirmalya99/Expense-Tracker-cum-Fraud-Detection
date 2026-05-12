package com.expensetracker.backend.dto;

import lombok.*;

import java.util.List;

public class FraudDtos {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FraudCheckResponse {
		private boolean suspicious;
		private List<String> reasons;
	}
}

