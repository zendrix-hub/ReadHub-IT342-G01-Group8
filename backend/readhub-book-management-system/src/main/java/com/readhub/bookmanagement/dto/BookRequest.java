package com.readhub.bookmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequest {
    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotNull(message = "Publication year is required")
    private Integer publicationYear;

    @Min(value = 0, message = "Total copies cannot be negative")
    private int totalCopies;

    @NotNull(message = "Category is required")
    private Long categoryId;
}