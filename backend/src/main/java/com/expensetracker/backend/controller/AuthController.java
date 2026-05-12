package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.AuthDtos;
import com.expensetracker.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<Void> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
		authService.register(req);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/login")
	public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
		var token = authService.loginAndGetToken(req);
		return ResponseEntity.ok(AuthDtos.AuthResponse.builder().token(token).build());
	}
}

