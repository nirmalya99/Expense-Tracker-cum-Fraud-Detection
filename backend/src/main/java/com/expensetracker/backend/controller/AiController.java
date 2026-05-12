package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.AiDtos;
import com.expensetracker.backend.service.AiAnalysisService;
import com.expensetracker.backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
	private final TransactionService transactionService;
	private final AiAnalysisService aiAnalysisService;

	public AiController(TransactionService transactionService, AiAnalysisService aiAnalysisService) {
		this.transactionService = transactionService;
		this.aiAnalysisService = aiAnalysisService;
	}

	@GetMapping("/analyze")
	public ResponseEntity<AiDtos.AnalyzeResponse> analyze(Authentication auth) {
		var txs = transactionService.listAll(auth.getName());
		return ResponseEntity.ok(aiAnalysisService.analyze(txs));
	}
}

