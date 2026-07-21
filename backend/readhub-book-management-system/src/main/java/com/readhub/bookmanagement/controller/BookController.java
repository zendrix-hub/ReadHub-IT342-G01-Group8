package com.readhub.bookmanagement.controller;

import com.readhub.bookmanagement.dto.ApiResponse;
import com.readhub.bookmanagement.dto.BookDto;
import com.readhub.bookmanagement.dto.BookRequest;
import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Category;
import com.readhub.bookmanagement.repository.CategoryRepository;
import com.readhub.bookmanagement.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Endpoints for retrieving, creating, updating, and deleting books and book categories")
public class BookController {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;

    // Public: Search or List all books
    @GetMapping
    @Operation(summary = "Search or list all books", description = "Retrieves all cataloged books with support for keyword search, category filtering, sorting, and pagination.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Books list retrieved successfully")
    public ResponseEntity<ApiResponse<?>> getAllBooks(
            @Parameter(description = "Keyword to filter by title, author, or ISBN") @RequestParam(required = false) String keyword,
            @Parameter(description = "Category ID to filter books by") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Page index for paginated output (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size for paginated output") @RequestParam(required = false) Integer size,
            @Parameter(description = "Database field name to sort search results by") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sorting order direction ('asc' or 'desc')") @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        if (page != null && size != null) {
            Page<BookDto> books = bookService.getAllBooks(keyword, categoryId, PageRequest.of(page, size, sort));
            return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
        }
        List<BookDto> books = bookService.getAllBooks(keyword, categoryId, sort);
        return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
    }

    // Admin Only: Add a new book
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new book", description = "Adds a new book record into the system catalog. Requires ADMIN permissions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Book added successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request fields supplied")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access attempt")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access forbidden for non-admin accounts")
    public ResponseEntity<ApiResponse<BookDto>> addBook(@Valid @RequestBody BookRequest request) {
        BookDto book = bookService.addBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(book, "Book added successfully"));
    }

    // --- NEW: Update Book ---
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing book record", description = "Modifies catalog parameters for a specific book ID. Requires ADMIN permissions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request fields supplied")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Target book record not found")
    public ResponseEntity<ApiResponse<BookDto>> updateBook(
            @Parameter(description = "ID of the target book to update") @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        BookDto book = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success(book, "Book updated successfully"));
    }

    // --- NEW: Delete Book ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book record", description = "Removes a specific book record from the system database. Requires ADMIN permissions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Target book record not found")
    public ResponseEntity<ApiResponse<String>> deleteBook(
            @Parameter(description = "ID of the target book to delete") @PathVariable Long id
    ) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully", "Book deleted successfully"));
    }

    // Admin Only: Create a Category
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new book category", description = "Inserts a new book categorization tag. Requires ADMIN permissions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid naming parameter supplied")
    public ResponseEntity<ApiResponse<Category>> addCategory(@RequestBody Map<String, String> payload) {
        Category category = bookService.createCategory(payload.get("name"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(category, "Category created successfully"));
    }

    // Temporary Fix Endpoint
    @GetMapping("/fix-categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk initialize categories seeder", description = "Saves base list category tags if none are registered. Requires ADMIN permissions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Initial category seed execution complete")
    public ResponseEntity<ApiResponse<String>> fixCategories() {
        if (categoryRepository.count() == 0) {
            Category c1 = new Category(); c1.setName("Technology");
            Category c2 = new Category(); c2.setName("Science");
            Category c3 = new Category(); c3.setName("Fiction");
            Category c4 = new Category(); c4.setName("Self-help");
            Category c5 = new Category(); c5.setName("Other");
            categoryRepository.saveAll(Arrays.asList(c1, c2, c3, c4, c5));
            return ResponseEntity.ok(ApiResponse.success("Fixed! Categories created.", "Categories fixed successfully"));
        }
        return ResponseEntity.ok(ApiResponse.success("Categories already exist.", "Categories already exist"));
    }
}