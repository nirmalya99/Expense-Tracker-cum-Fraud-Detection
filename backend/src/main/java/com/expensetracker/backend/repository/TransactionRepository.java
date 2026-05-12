package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.TransactionType;
import com.expensetracker.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	List<Transaction> findAllByUserOrderByDateDescIdDesc(User user);

	@Query("""
			select t from Transaction t
			where t.user = :user
			  and (:category is null or lower(t.category) = lower(:category))
			  and (:type is null or t.type = :type)
			  and (:startDate is null or t.date >= :startDate)
			  and (:endDate is null or t.date <= :endDate)
			order by t.date desc, t.id desc
			""")
	List<Transaction> filter(
			@Param("user") User user,
			@Param("category") String category,
			@Param("type") TransactionType type,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);
}

