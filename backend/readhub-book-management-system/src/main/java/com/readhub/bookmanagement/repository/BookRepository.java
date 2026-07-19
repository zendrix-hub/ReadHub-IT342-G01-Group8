package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(attributePaths = {"category"})
    List<Book> findAll();

    // Overriding the pageable fetch to force an eager Eager Join fetch via EntityGraph
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT b FROM Book b")
    Page<Book> findAllPaginated(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(String title, String author, String isbn);
}