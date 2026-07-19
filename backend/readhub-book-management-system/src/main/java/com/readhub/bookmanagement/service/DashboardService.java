package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.AdminDashboardStatsDto;
import com.readhub.bookmanagement.dto.StudentDashboardStatsDto;
import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Role;
import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.model.TransactionStatus;
import com.readhub.bookmanagement.model.User;
import com.readhub.bookmanagement.repository.BookRepository;
import com.readhub.bookmanagement.repository.TransactionRepository;
import com.readhub.bookmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;

    public AdminDashboardStatsDto getAdminStats() {
        long totalBooks = bookRepository.count();
        long totalStudents = userRepository.countByRole(Role.STUDENT);

        List<Transaction> allTransactions = transactionRepository.findAll();

        long activeLoans = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.BORROWED || t.getStatus() == TransactionStatus.OVERDUE)
                .count();

        long pendingApprovals = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PENDING)
                .count();

        // Book category distribution
        List<Book> allBooks = bookRepository.findAll();
        Map<String, Long> categoryDistribution = allBooks.stream()
                .filter(b -> b.getCategory() != null)
                .collect(Collectors.groupingBy(b -> b.getCategory().getName(), Collectors.counting()));

        // Prepopulate last 6 months in chronological order
        Map<String, Long> borrowingTrends = getPrepopulatedLast6Months();

        // Populate actual values from transactions
        allTransactions.stream()
                .filter(t -> t.getBorrowDate() != null)
                .forEach(t -> {
                    String monthName = t.getBorrowDate().format(DateTimeFormatter.ofPattern("MMM"));
                    if (borrowingTrends.containsKey(monthName)) {
                        borrowingTrends.put(monthName, borrowingTrends.get(monthName) + 1);
                    }
                });

        return AdminDashboardStatsDto.builder()
                .totalBooks(totalBooks)
                .totalStudents(totalStudents)
                .activeLoans(activeLoans)
                .pendingApprovals(pendingApprovals)
                .categoryDistribution(categoryDistribution)
                .borrowingTrends(borrowingTrends)
                .build();
    }

    public StudentDashboardStatsDto getStudentStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        List<Transaction> myTransactions = transactionRepository.findByUser(user);

        long totalBorrows = myTransactions.size();
        long activeLoans = myTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.BORROWED || t.getStatus() == TransactionStatus.OVERDUE)
                .count();
        long pendingRequests = myTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PENDING)
                .count();

        // Find favorite category
        String favoriteCategory = myTransactions.stream()
                .filter(t -> t.getBook() != null && t.getBook().getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getBook().getCategory().getName(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // Prepopulate last 6 months
        Map<String, Long> borrowingTrends = getPrepopulatedLast6Months();

        // Populate actual values
        myTransactions.stream()
                .filter(t -> t.getBorrowDate() != null)
                .forEach(t -> {
                    String monthName = t.getBorrowDate().format(DateTimeFormatter.ofPattern("MMM"));
                    if (borrowingTrends.containsKey(monthName)) {
                        borrowingTrends.put(monthName, borrowingTrends.get(monthName) + 1);
                    }
                });

        return StudentDashboardStatsDto.builder()
                .totalBorrows(totalBorrows)
                .activeLoans(activeLoans)
                .pendingRequests(pendingRequests)
                .favoriteCategory(favoriteCategory)
                .borrowingTrends(borrowingTrends)
                .build();
    }

    private Map<String, Long> getPrepopulatedLast6Months() {
        Map<String, Long> trends = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            String monthName = now.minusMonths(i).format(DateTimeFormatter.ofPattern("MMM"));
            trends.put(monthName, 0L);
        }
        return trends;
    }
}
