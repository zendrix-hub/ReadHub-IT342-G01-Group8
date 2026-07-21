package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.TransactionDto;
import com.readhub.bookmanagement.exception.BookCopyUnavailableException;
import com.readhub.bookmanagement.exception.InvalidTransactionStateException;
import com.readhub.bookmanagement.exception.ResourceNotFoundException;
import com.readhub.bookmanagement.model.*;
import com.readhub.bookmanagement.repository.BookRepository;
import com.readhub.bookmanagement.repository.TransactionRepository;
import com.readhub.bookmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User student;
    private User admin;
    private Book book;
    private Transaction transaction;

    @BeforeEach
    public void setUp() {
        student = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("student@readhub.com")
                .role(Role.STUDENT)
                .build();

        admin = User.builder()
                .userId(2L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@readhub.com")
                .role(Role.ADMIN)
                .build();

        book = Book.builder()
                .bookId(10L)
                .title("Sample Book")
                .author("Author")
                .availableCopies(3)
                .totalCopies(5)
                .build();

        transaction = Transaction.builder()
                .transactionId(100L)
                .user(student)
                .book(book)
                .status(TransactionStatus.PENDING)
                .build();
    }

    @Test
    public void testRequestBook_Success() {
        when(userRepository.findByEmail("student@readhub.com")).thenReturn(Optional.of(student));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setTransactionId(100L);
            return tx;
        });
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(Collections.singletonList(admin));

        TransactionDto dto = transactionService.requestBook(10L, "student@readhub.com");

        assertNotNull(dto);
        assertEquals(100L, dto.getTransactionId());
        assertEquals("PENDING", dto.getStatus());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(notificationService, times(1)).sendNotification(eq(admin), anyString(), any(Transaction.class));
    }

    @Test
    public void testRequestBook_UserNotFound() {
        when(userRepository.findByEmail("student@readhub.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.requestBook(10L, "student@readhub.com"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testRequestBook_BookNotFound() {
        when(userRepository.findByEmail("student@readhub.com")).thenReturn(Optional.of(student));
        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.requestBook(10L, "student@readhub.com"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testRequestBook_BookCopyUnavailable() {
        book.setAvailableCopies(0);
        when(userRepository.findByEmail("student@readhub.com")).thenReturn(Optional.of(student));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        assertThrows(BookCopyUnavailableException.class, () -> transactionService.requestBook(10L, "student@readhub.com"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testUpdateStatus_ApproveSuccess() {
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail("admin@readhub.com")).thenReturn(Optional.of(admin));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto dto = transactionService.updateStatus(100L, TransactionStatus.APPROVED, "admin@readhub.com");

        assertNotNull(dto);
        assertEquals("APPROVED", dto.getStatus());
        assertEquals(2L, dto.getAdminId());
        verify(notificationService, times(1)).sendNotification(eq(student), anyString(), eq(transaction));
    }

    @Test
    public void testUpdateStatus_ApproveInvalidState() {
        transaction.setStatus(TransactionStatus.APPROVED);
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail("admin@readhub.com")).thenReturn(Optional.of(admin));

        assertThrows(InvalidTransactionStateException.class, () -> transactionService.updateStatus(100L, TransactionStatus.APPROVED, "admin@readhub.com"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testUpdateStatus_RejectSuccess() {
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail("admin@readhub.com")).thenReturn(Optional.of(admin));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto dto = transactionService.updateStatus(100L, TransactionStatus.REJECTED, "admin@readhub.com");

        assertNotNull(dto);
        assertEquals("REJECTED", dto.getStatus());
        verify(notificationService, times(1)).sendNotification(eq(student), anyString(), eq(transaction));
    }

    @Test
    public void testUpdateStatus_BorrowSuccess() {
        transaction.setStatus(TransactionStatus.APPROVED);
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail("admin@readhub.com")).thenReturn(Optional.of(admin));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto dto = transactionService.updateStatus(100L, TransactionStatus.BORROWED, "admin@readhub.com");

        assertNotNull(dto);
        assertEquals("BORROWED", dto.getStatus());
        assertEquals(2, book.getAvailableCopies()); // Decremented
        assertNotNull(dto.getBorrowDate());
        assertEquals(LocalDate.now().plusDays(7), dto.getDueDate());
        verify(notificationService, times(1)).sendNotification(eq(student), anyString(), eq(transaction));
    }

    @Test
    public void testUpdateStatus_BorrowNoCopies() {
        transaction.setStatus(TransactionStatus.APPROVED);
        book.setAvailableCopies(0);
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail("admin@readhub.com")).thenReturn(Optional.of(admin));

        assertThrows(BookCopyUnavailableException.class, () -> transactionService.updateStatus(100L, TransactionStatus.BORROWED, "admin@readhub.com"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testUpdateStatus_ReturnSuccess() {
        transaction.setStatus(TransactionStatus.BORROWED);
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail("admin@readhub.com")).thenReturn(Optional.of(admin));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto dto = transactionService.updateStatus(100L, TransactionStatus.RETURNED, "admin@readhub.com");

        assertNotNull(dto);
        assertEquals("RETURNED", dto.getStatus());
        assertEquals(4, book.getAvailableCopies()); // Incremented from 3
        assertNotNull(dto.getActualReturnDate());
        verify(notificationService, times(1)).sendNotification(eq(student), anyString(), eq(transaction));
    }

    @Test
    public void testUpdateStatus_ReturnInvalidState() {
        transaction.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail("admin@readhub.com")).thenReturn(Optional.of(admin));

        assertThrows(InvalidTransactionStateException.class, () -> transactionService.updateStatus(100L, TransactionStatus.RETURNED, "admin@readhub.com"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
