package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.TransactionDtos;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.TransactionType;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;

	public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public Transaction create(String userEmail, TransactionDtos.CreateTransactionRequest req) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Transaction tx = Transaction.builder()
				.user(user)
				.amount(req.getAmount())
				.type(req.getType())
				.category(req.getCategory())
				.date(req.getDate())
				.build();

		return transactionRepository.save(tx);
	}

	@Transactional(readOnly = true)
	public List<Transaction> listAll(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return transactionRepository.findAllByUserOrderByDateDescIdDesc(user);
	}

	@Transactional(readOnly = true)
	public List<Transaction> filter(
			String userEmail,
			String category,
			TransactionType type,
			LocalDate startDate,
			LocalDate endDate
	) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return transactionRepository.filter(user, category, type, startDate, endDate);
	}
}

