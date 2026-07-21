package com.readhub.bookmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {
    private Long bookId;
    private String isbn;
    private String title;
    private String author;
    private Integer publicationYear;
    private int totalCopies;
    private int availableCopies;
    private Long categoryId;
    private String categoryName;
}
