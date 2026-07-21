package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, CustomBookRepository {

    @Override
    @EntityGraph(attributePaths = {"category"})
    Optional<Book> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"category"})
    List<Book> findAll();

    @Override
    @EntityGraph(attributePaths = {"category"})
    List<Book> findAll(Sort sort);

    // Overriding the pageable fetch to force an eager Eager Join fetch via EntityGraph
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT b FROM Book b")
    Page<Book> findAllPaginated(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT b FROM Book b WHERE " +
           "(:categoryId IS NULL OR b.category.categoryId = :categoryId) AND " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> searchBooks(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT b FROM Book b WHERE " +
           "(:categoryId IS NULL OR b.category.categoryId = :categoryId) AND " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Book> searchBooks(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Sort sort);

    @EntityGraph(attributePaths = {"category"})
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(String title, String author, String isbn);

    @EntityGraph(attributePaths = {"category"})
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(String title, String author, String isbn, Sort sort);

    @EntityGraph(attributePaths = {"category"})
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(String title, String author, String isbn, Pageable pageable);
}