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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public BookDto mapToBookDto(Book book) {
        if (book == null) {
            return null;
        }
        return BookDto.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publicationYear(book.getPublicationYear())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .categoryId(book.getCategory() != null ? book.getCategory().getCategoryId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .build();
    }

    @Override
    public List<BookDto> getAllBooks(String keyword) {
        return getAllBooks(keyword, (Long) null);
    }

    @Override
    public List<BookDto> getAllBooks(String keyword, Long categoryId) {
        return bookRepository.searchBooksCustom(categoryId, keyword, Sort.unsorted()).stream()
                .map(this::mapToBookDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<BookDto> getAllBooks(String keyword, Sort sort) {
        return getAllBooks(keyword, (Long) null, sort);
    }

    @Override
    public List<BookDto> getAllBooks(String keyword, Long categoryId, Sort sort) {
        return bookRepository.searchBooksCustom(categoryId, keyword, sort).stream()
                .map(this::mapToBookDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<BookDto> getAllBooks(String keyword, Pageable pageable) {
        return getAllBooks(keyword, (Long) null, pageable);
    }

    @Override
    public Page<BookDto> getAllBooks(String keyword, Long categoryId, Pageable pageable) {
        return bookRepository.searchBooksCustom(categoryId, keyword, pageable)
                .map(this::mapToBookDto);
    }

    @Override
    @Transactional
    public BookDto addBook(BookRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publicationYear(request.getPublicationYear())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies()) 
                .category(category)
                .build();

        return mapToBookDto(bookRepository.save(book));
    }
    
    @Override
    @Transactional
    public Category createCategory(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new ResourceAlreadyExistsException("Category already exists");
        }
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public BookDto updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setCategory(category);
        
        int difference = request.getTotalCopies() - book.getTotalCopies();
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(book.getAvailableCopies() + difference);

        return mapToBookDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (transactionRepository.existsByBook(book)) {
            throw new DeleteViolationException("Cannot delete: This book is linked to active or past transactions.");
        }

        bookRepository.deleteById(id);
    }
}
