package com.expensetracker.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDtos {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegisterRequest {
		@NotBlank
		private String name;

		@Email
		@NotBlank
		private String email;

		@NotBlank
		@Size(min = 6, max = 100)
		private String password;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginRequest {
		@Email
		@NotBlank
		private String email;

		@NotBlank
		private String password;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class AuthResponse {
		private String token;
	}
}

