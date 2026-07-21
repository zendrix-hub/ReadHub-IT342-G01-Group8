package com.readhub.bookmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private Long transactionId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime actualReturnDate;
    private Long adminId;
    private String adminName;
}
