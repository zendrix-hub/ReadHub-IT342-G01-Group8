package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.BookDto;
import com.readhub.bookmanagement.dto.BookRequest;
import com.readhub.bookmanagement.exception.DeleteViolationException;
import com.readhub.bookmanagement.exception.ResourceAlreadyExistsException;
import com.readhub.bookmanagement.exception.ResourceNotFoundException;
import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Category;
import com.readhub.bookmanagement.repository.BookRepository;
import com.readhub.bookmanagement.repository.CategoryRepository;
import com.readhub.bookmanagement.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTests {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Category category;
    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    public void setUp() {
        category = new Category();
        category.setCategoryId(10L);
        category.setName("Fiction");

        book = Book.builder()
                .bookId(1L)
                .isbn("111-222-333")
                .title("Original Title")
                .author("Original Author")
                .publicationYear(2020)
                .totalCopies(10)
                .availableCopies(8)
                .category(category)
                .build();

        bookRequest = new BookRequest();
        bookRequest.setIsbn("111-222-333");
        bookRequest.setTitle("New Title");
        bookRequest.setAuthor("New Author");
        bookRequest.setPublicationYear(2022);
        bookRequest.setCategoryId(10L);
        bookRequest.setTotalCopies(15);
    }

    @Test
    public void testAddBook_Success() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setBookId(1L);
            return b;
        });

        BookDto result = bookService.addBook(bookRequest);

        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals("New Title", result.getTitle());
        assertEquals(15, result.getTotalCopies());
        assertEquals(15, result.getAvailableCopies()); // Initial copies assigned
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testAddBook_CategoryNotFound() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.addBook(bookRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testCreateCategory_Success() {
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category created = bookService.createCategory("Fiction");

        assertNotNull(created);
        assertEquals("Fiction", created.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void testCreateCategory_AlreadyExists() {
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.of(category));

        assertThrows(ResourceAlreadyExistsException.class, () -> bookService.createCategory("Fiction"));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    public void testUpdateBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookDto updated = bookService.updateBook(1L, bookRequest);

        assertNotNull(updated);
        assertEquals("New Title", updated.getTitle());
        assertEquals("New Author", updated.getAuthor());
        assertEquals(15, updated.getTotalCopies());
        // Difference: 15 - 10 = +5. availableCopies = 8 + 5 = 13
        assertEquals(13, updated.getAvailableCopies());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testUpdateBook_BookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(1L, bookRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testUpdateBook_CategoryNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(1L, bookRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testDeleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(transactionRepository.existsByBook(book)).thenReturn(false);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteBook_BookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(1L));
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteBook_LinkedToTransactions() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(transactionRepository.existsByBook(book)).thenReturn(true);

        assertThrows(DeleteViolationException.class, () -> bookService.deleteBook(1L));
        verify(bookRepository, never()).deleteById(anyLong());
    }
}
