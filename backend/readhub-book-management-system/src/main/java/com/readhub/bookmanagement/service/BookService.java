package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.BookDto;
import com.readhub.bookmanagement.dto.BookRequest;
import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface BookService {
    BookDto mapToBookDto(Book book);
    List<BookDto> getAllBooks(String keyword);
    List<BookDto> getAllBooks(String keyword, Long categoryId);
    List<BookDto> getAllBooks(String keyword, Sort sort);
    List<BookDto> getAllBooks(String keyword, Long categoryId, Sort sort);
    Page<BookDto> getAllBooks(String keyword, Pageable pageable);
    Page<BookDto> getAllBooks(String keyword, Long categoryId, Pageable pageable);
    BookDto addBook(BookRequest request);
    Category createCategory(String name);
    BookDto updateBook(Long id, BookRequest request);
    void deleteBook(Long id);
}