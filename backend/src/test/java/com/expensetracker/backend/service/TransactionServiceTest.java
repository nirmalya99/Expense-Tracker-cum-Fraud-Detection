package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.TransactionDtos;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.TransactionType;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private TransactionDtos.CreateTransactionRequest createRequest;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .password("password")
                .build();

        createRequest = new TransactionDtos.CreateTransactionRequest();
        createRequest.setAmount(BigDecimal.valueOf(100.50));
        createRequest.setType(TransactionType.DEBIT);
        createRequest.setCategory("Food");
        createRequest.setDate(LocalDate.now());

        testTransaction = Transaction.builder()
                .id(1L)
                .user(testUser)
                .amount(createRequest.getAmount())
                .type(createRequest.getType())
                .category(createRequest.getCategory())
                .date(createRequest.getDate())
                .build();
    }

    @Test
    void create_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.create("test@example.com", createRequest);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100.50), result.getAmount());
        assertEquals(TransactionType.DEBIT, result.getType());
        assertEquals("Food", result.getCategory());
        
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void create_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.create("test@example.com", createRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void listAll_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findAllByUserOrderByDateDescIdDesc(testUser)).thenReturn(List.of(testTransaction));

        List<Transaction> result = transactionService.listAll("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(transactionRepository, times(1)).findAllByUserOrderByDateDescIdDesc(testUser);
    }

    @Test
    void listAll_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.listAll("test@example.com");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(transactionRepository, never()).findAllByUserOrderByDateDescIdDesc(any(User.class));
    }

    @Test
    void filter_Success() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(transactionRepository.filter(testUser, "Food", TransactionType.DEBIT, startDate, endDate))
                .thenReturn(List.of(testTransaction));

        List<Transaction> result = transactionService.filter(
                "test@example.com", "Food", TransactionType.DEBIT, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(transactionRepository, times(1)).filter(testUser, "Food", TransactionType.DEBIT, startDate, endDate);
    }

    @Test
    void filter_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.filter("test@example.com", "Food", TransactionType.DEBIT, null, null);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(transactionRepository, never()).filter(any(), any(), any(), any(), any());
    }
}
