package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface CustomBookRepository {
    List<Book> searchBooksCustom(Long categoryId, String keyword, Sort sort);
    Page<Book> searchBooksCustom(Long categoryId, String keyword, Pageable pageable);
}
