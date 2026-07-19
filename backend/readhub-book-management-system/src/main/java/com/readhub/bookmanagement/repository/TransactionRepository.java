package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.model.TransactionStatus;
import com.readhub.bookmanagement.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    List<Transaction> findByUser(User user);

    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    List<Transaction> findByStatus(TransactionStatus status);
    
    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    @Query("SELECT t FROM Transaction t WHERE t.status = 'BORROWED' AND t.dueDate < CURRENT_DATE")
    List<Transaction> findOverdueTransactions();

    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    @Query("SELECT t FROM Transaction t WHERE t.status = 'BORROWED' AND t.dueDate = :targetDate")
    List<Transaction> findByDueDate(@Param("targetDate") LocalDate targetDate);

     // Get all transactions sorted by requestDate descending
    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    List<Transaction> findAllByOrderByRequestDateDesc();

    // Optional: get user-specific transactions sorted latest first
    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    List<Transaction> findByUserOrderByRequestDateDesc(User user);

    boolean existsByBook(Book book);
    
    // This is required for UserService to safely delete users
    boolean existsByUser(User user);
}