package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.TransactionDto;
import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface TransactionService {
    TransactionDto mapToTransactionDto(Transaction transaction);
    TransactionDto requestBook(Long bookId, String userEmail);
    TransactionDto updateStatus(Long transactionId, TransactionStatus newStatus, String adminEmail);
    List<TransactionDto> getAllTransactions();
    List<TransactionDto> getAllTransactions(TransactionStatus status);
    List<TransactionDto> getAllTransactions(Sort sort);
    List<TransactionDto> getAllTransactions(TransactionStatus status, Sort sort);
    Page<TransactionDto> getAllTransactions(Pageable pageable);
    Page<TransactionDto> getAllTransactions(TransactionStatus status, Pageable pageable);
    List<TransactionDto> getMyHistory(String email);
    List<TransactionDto> getMyHistory(String email, TransactionStatus status);
    List<TransactionDto> getMyHistory(String email, Sort sort);
    List<TransactionDto> getMyHistory(String email, TransactionStatus status, Sort sort);
    Page<TransactionDto> getMyHistory(String email, Pageable pageable);
    Page<TransactionDto> getMyHistory(String email, TransactionStatus status, Pageable pageable);
}