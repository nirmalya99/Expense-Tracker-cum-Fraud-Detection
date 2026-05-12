package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.FraudDtos;
import com.expensetracker.backend.service.FraudDetectionService;
import com.expensetracker.backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {
	private final TransactionService transactionService;
	private final FraudDetectionService fraudDetectionService;

	public FraudController(TransactionService transactionService, FraudDetectionService fraudDetectionService) {
		this.transactionService = transactionService;
		this.fraudDetectionService = fraudDetectionService;
	}

	@GetMapping("/check")
	public ResponseEntity<FraudDtos.FraudCheckResponse> check(Authentication auth) {
		var txs = transactionService.listAll(auth.getName());
		var reasons = fraudDetectionService.check(txs);
		return ResponseEntity.ok(new FraudDtos.FraudCheckResponse(!reasons.isEmpty(), reasons));
	}
}

