package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.TransactionDtos;
import com.expensetracker.backend.model.TransactionType;
import com.expensetracker.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping
	public ResponseEntity<TransactionDtos.TransactionResponse> add(
			Authentication auth,
			@Valid @RequestBody TransactionDtos.CreateTransactionRequest req
	) {
		var tx = transactionService.create(auth.getName(), req);
		return ResponseEntity.ok(toResponse(tx));
	}

	@GetMapping
	public ResponseEntity<List<TransactionDtos.TransactionResponse>> listAll(Authentication auth) {
		var list = transactionService.listAll(auth.getName()).stream().map(this::toResponse).toList();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/filter")
	public ResponseEntity<List<TransactionDtos.TransactionResponse>> filter(
			Authentication auth,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) TransactionType type,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
	) {
		var list = transactionService.filter(auth.getName(), category, type, startDate, endDate)
				.stream()
				.map(this::toResponse)
				.toList();
		return ResponseEntity.ok(list);
	}

	private TransactionDtos.TransactionResponse toResponse(com.expensetracker.backend.model.Transaction tx) {
		return TransactionDtos.TransactionResponse.builder()
				.id(tx.getId())
				.amount(tx.getAmount())
				.type(tx.getType())
				.category(tx.getCategory())
				.date(tx.getDate())
				.build();
	}
}

