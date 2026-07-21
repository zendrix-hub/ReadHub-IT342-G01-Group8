package com.readhub.bookmanagement;

import com.readhub.bookmanagement.dto.BookDto;
import com.readhub.bookmanagement.dto.TransactionDto;
import com.readhub.bookmanagement.model.*;
import com.readhub.bookmanagement.service.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ModelMappingTests {

    private final BookService bookService = new BookServiceImpl(null, null, null);
    private final TransactionService transactionService = new TransactionServiceImpl(null, null, null, null);

    @Test
    public void testMapToBookDto_WithCategory() {
        Category category = new Category();
        category.setCategoryId(10L);
        category.setName("Fiction");

        Book book = Book.builder()
                .bookId(1L)
                .isbn("123456789")
                .title("Sample Title")
                .author("Author Name")
                .publicationYear(2021)
                .totalCopies(5)
                .availableCopies(3)
                .category(category)
                .build();

        BookDto dto = bookService.mapToBookDto(book);

        assertNotNull(dto);
        assertEquals(1L, dto.getBookId());
        assertEquals("123456789", dto.getIsbn());
        assertEquals("Sample Title", dto.getTitle());
        assertEquals("Author Name", dto.getAuthor());
        assertEquals(2021, dto.getPublicationYear());
        assertEquals(5, dto.getTotalCopies());
        assertEquals(3, dto.getAvailableCopies());
        assertEquals(10L, dto.getCategoryId());
        assertEquals("Fiction", dto.getCategoryName());
    }

    @Test
    public void testMapToBookDto_NullCategory() {
        Book book = Book.builder()
                .bookId(1L)
                .isbn("123456789")
                .title("Sample Title")
                .author("Author Name")
                .publicationYear(2021)
                .totalCopies(5)
                .availableCopies(3)
                .category(null)
                .build();

        BookDto dto = bookService.mapToBookDto(book);

        assertNotNull(dto);
        assertNull(dto.getCategoryId());
        assertNull(dto.getCategoryName());
    }

    @Test
    public void testMapToBookDto_NullBook() {
        BookDto dto = bookService.mapToBookDto(null);
        assertNull(dto);
    }

    @Test
    public void testMapToTransactionDto_Full() {
        User student = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        Book book = Book.builder()
                .bookId(2L)
                .title("Book Title")
                .author("Book Author")
                .build();

        User admin = User.builder()
                .userId(3L)
                .firstName("Admin")
                .lastName("User")
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId(100L)
                .user(student)
                .book(book)
                .admin(admin)
                .status(TransactionStatus.BORROWED)
                .requestDate(LocalDateTime.of(2026, 7, 20, 10, 0))
                .borrowDate(LocalDateTime.of(2026, 7, 20, 11, 0))
                .dueDate(LocalDate.of(2026, 7, 27))
                .actualReturnDate(LocalDateTime.of(2026, 7, 25, 12, 0))
                .build();

        TransactionDto dto = transactionService.mapToTransactionDto(transaction);

        assertNotNull(dto);
        assertEquals(100L, dto.getTransactionId());
        assertEquals(1L, dto.getStudentId());
        assertEquals("John Doe", dto.getStudentName());
        assertEquals("john.doe@example.com", dto.getStudentEmail());
        assertEquals(2L, dto.getBookId());
        assertEquals("Book Title", dto.getBookTitle());
        assertEquals("Book Author", dto.getBookAuthor());
        assertEquals("BORROWED", dto.getStatus());
        assertEquals(LocalDateTime.of(2026, 7, 20, 10, 0), dto.getRequestDate());
        assertEquals(LocalDateTime.of(2026, 7, 20, 11, 0), dto.getBorrowDate());
        assertEquals(LocalDate.of(2026, 7, 27), dto.getDueDate());
        assertEquals(LocalDateTime.of(2026, 7, 25, 12, 0), dto.getActualReturnDate());
        assertEquals(3L, dto.getAdminId());
        assertEquals("Admin User", dto.getAdminName());
    }

    @Test
    public void testMapToTransactionDto_NullAssociations() {
        Transaction transaction = Transaction.builder()
                .transactionId(100L)
                .user(null)
                .book(null)
                .admin(null)
                .status(null)
                .build();

        TransactionDto dto = transactionService.mapToTransactionDto(transaction);

        assertNotNull(dto);
        assertEquals(100L, dto.getTransactionId());
        assertNull(dto.getStudentId());
        assertNull(dto.getStudentName());
        assertNull(dto.getStudentEmail());
        assertNull(dto.getBookId());
        assertNull(dto.getBookTitle());
        assertNull(dto.getBookAuthor());
        assertNull(dto.getStatus());
        assertNull(dto.getAdminId());
        assertNull(dto.getAdminName());
    }

    @Test
    public void testMapToTransactionDto_NullTransaction() {
        TransactionDto dto = transactionService.mapToTransactionDto(null);
        assertNull(dto);
    }
}
