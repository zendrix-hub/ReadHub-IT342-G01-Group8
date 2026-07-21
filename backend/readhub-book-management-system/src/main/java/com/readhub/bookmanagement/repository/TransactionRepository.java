package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.model.TransactionStatus;
import com.readhub.bookmanagement.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    Optional<Transaction> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    List<Transaction> findAll(Sort sort);
    
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

    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    Page<Transaction> findAll(Pageable pageable);

    // Optional: get user-specific transactions sorted latest first
    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    List<Transaction> findByUserOrderByRequestDateDesc(User user);

    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    List<Transaction> findByUser(User user, Sort sort);

    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    Page<Transaction> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:userEmail IS NULL OR t.user.email = :userEmail)")
    Page<Transaction> searchTransactions(@Param("status") TransactionStatus status, @Param("userEmail") String userEmail, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book", "book.category", "admin"})
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:userEmail IS NULL OR t.user.email = :userEmail)")
    List<Transaction> searchTransactions(@Param("status") TransactionStatus status, @Param("userEmail") String userEmail, Sort sort);

    boolean existsByBook(Book book);
    
    // This is required for UserService to safely delete users
    boolean existsByUser(User user);
}