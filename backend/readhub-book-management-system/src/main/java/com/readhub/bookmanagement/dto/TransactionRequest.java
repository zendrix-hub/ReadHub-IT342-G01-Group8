package com.readhub.bookmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequest {
    @NotNull(message = "Book ID is required")
    private Long bookId;
}