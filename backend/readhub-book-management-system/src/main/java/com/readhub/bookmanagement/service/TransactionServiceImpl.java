package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.TransactionDto;
import com.readhub.bookmanagement.exception.BookCopyUnavailableException;
import com.readhub.bookmanagement.exception.InvalidTransactionStateException;
import com.readhub.bookmanagement.exception.ResourceNotFoundException;
import com.readhub.bookmanagement.model.*;
import com.readhub.bookmanagement.repository.BookRepository;
import com.readhub.bookmanagement.repository.TransactionRepository;
import com.readhub.bookmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public TransactionDto mapToTransactionDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionDto.builder()
                .transactionId(transaction.getTransactionId())
                .studentId(transaction.getUser() != null ? transaction.getUser().getUserId() : null)
                .studentName(transaction.getUser() != null ? (transaction.getUser().getFirstName() + " " + transaction.getUser().getLastName()) : null)
                .studentEmail(transaction.getUser() != null ? transaction.getUser().getEmail() : null)
                .bookId(transaction.getBook() != null ? transaction.getBook().getBookId() : null)
                .bookTitle(transaction.getBook() != null ? transaction.getBook().getTitle() : null)
                .bookAuthor(transaction.getBook() != null ? transaction.getBook().getAuthor() : null)
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .requestDate(transaction.getRequestDate())
                .borrowDate(transaction.getBorrowDate())
                .dueDate(transaction.getDueDate())
                .actualReturnDate(transaction.getActualReturnDate())
                .adminId(transaction.getAdmin() != null ? transaction.getAdmin().getUserId() : null)
                .adminName(transaction.getAdmin() != null ? (transaction.getAdmin().getFirstName() + " " + transaction.getAdmin().getLastName()) : null)
                .build();
    }

    @Override
    @Transactional
    public TransactionDto requestBook(Long bookId, String userEmail) {
        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new BookCopyUnavailableException("Book is currently unavailable");
        }

        Transaction transaction = Transaction.builder()
                .user(student)
                .book(book)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTxn = transactionRepository.save(transaction);

        // --- NOTIFY ADMINS ---
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            String msg = "New Request: " + student.getFirstName() + " " + student.getLastName() + " requested '" + book.getTitle() + "'";
            notificationService.sendNotification(admin, msg, savedTxn);
        }

        return mapToTransactionDto(savedTxn);
    }

    @Override
    @Transactional
    public TransactionDto updateStatus(Long transactionId, TransactionStatus newStatus, String adminEmail) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        switch (newStatus) {
            case APPROVED:
                if (transaction.getStatus() != TransactionStatus.PENDING) {
                    throw new InvalidTransactionStateException("Can only approve PENDING requests");
                }
                transaction.setStatus(TransactionStatus.APPROVED);
                transaction.setAdmin(admin);
                // Student Message
                notificationService.sendNotification(transaction.getUser(), "Good news! Your request for '" + transaction.getBook().getTitle() + "' was APPROVED.", transaction);
                break;

            case REJECTED:
                if (transaction.getStatus() != TransactionStatus.PENDING) {
                    throw new InvalidTransactionStateException("Can only reject PENDING requests");
                }
                transaction.setStatus(TransactionStatus.REJECTED);
                transaction.setAdmin(admin);
                // Student Message
                notificationService.sendNotification(transaction.getUser(), "Your request for '" + transaction.getBook().getTitle() + "' was REJECTED.", transaction);
                break;

            case BORROWED:
                if (transaction.getStatus() != TransactionStatus.APPROVED) {
                    throw new InvalidTransactionStateException("Must be APPROVED first");
                }
                Book bookToBorrow = transaction.getBook();
                if (bookToBorrow.getAvailableCopies() <= 0) {
                    throw new BookCopyUnavailableException("No copies left!");
                }
                
                bookToBorrow.setAvailableCopies(bookToBorrow.getAvailableCopies() - 1);
                bookRepository.save(bookToBorrow);

                transaction.setStatus(TransactionStatus.BORROWED);
                transaction.setBorrowDate(LocalDateTime.now());
                transaction.setDueDate(LocalDate.now().plusDays(7));
                
                // Student Message
                notificationService.sendNotification(transaction.getUser(), "You have successfully picked up '" + bookToBorrow.getTitle() + "'. Due date: " + transaction.getDueDate(), transaction);
                break;

            case RETURNED:
                if (transaction.getStatus() != TransactionStatus.BORROWED && transaction.getStatus() != TransactionStatus.OVERDUE) {
                    throw new InvalidTransactionStateException("Invalid return");
                }
                Book bookToReturn = transaction.getBook();
                bookToReturn.setAvailableCopies(bookToReturn.getAvailableCopies() + 1);
                bookRepository.save(bookToReturn);

                transaction.setStatus(TransactionStatus.RETURNED);
                transaction.setActualReturnDate(LocalDateTime.now());
                
                // Student Message
                notificationService.sendNotification(transaction.getUser(), "Return confirmed for '" + bookToReturn.getTitle() + "'. Thank you!", transaction);
                break;

            default: 
                throw new InvalidTransactionStateException("Invalid status transition");
        }

        return mapToTransactionDto(transactionRepository.save(transaction));
    }

    @Override
    public List<TransactionDto> getAllTransactions() {
        return getAllTransactions((TransactionStatus) null);
    }

    @Override
    public List<TransactionDto> getAllTransactions(TransactionStatus status) {
        return transactionRepository.searchTransactions(status, null, Sort.unsorted()).stream()
                .map(this::mapToTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<TransactionDto> getAllTransactions(Sort sort) {
        return getAllTransactions((TransactionStatus) null, sort);
    }

    @Override
    public List<TransactionDto> getAllTransactions(TransactionStatus status, Sort sort) {
        return transactionRepository.searchTransactions(status, null, sort).stream()
                .map(this::mapToTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<TransactionDto> getAllTransactions(Pageable pageable) {
        return getAllTransactions((TransactionStatus) null, pageable);
    }

    @Override
    public Page<TransactionDto> getAllTransactions(TransactionStatus status, Pageable pageable) {
        return transactionRepository.searchTransactions(status, null, pageable)
                .map(this::mapToTransactionDto);
    }

    @Override
    public List<TransactionDto> getMyHistory(String email) {
        return getMyHistory(email, (TransactionStatus) null);
    }

    @Override
    public List<TransactionDto> getMyHistory(String email, TransactionStatus status) {
        return transactionRepository.searchTransactions(status, email, Sort.unsorted()).stream()
                .map(this::mapToTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<TransactionDto> getMyHistory(String email, Sort sort) {
        return getMyHistory(email, (TransactionStatus) null, sort);
    }

    @Override
    public List<TransactionDto> getMyHistory(String email, TransactionStatus status, Sort sort) {
        return transactionRepository.searchTransactions(status, email, sort).stream()
                .map(this::mapToTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<TransactionDto> getMyHistory(String email, Pageable pageable) {
        return getMyHistory(email, (TransactionStatus) null, pageable);
    }

    @Override
    public Page<TransactionDto> getMyHistory(String email, TransactionStatus status, Pageable pageable) {
        return transactionRepository.searchTransactions(status, email, pageable)
                .map(this::mapToTransactionDto);
    }
}
