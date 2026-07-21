package com.readhub.bookmanagement.controller;

import com.readhub.bookmanagement.dto.ApiResponse;
import com.readhub.bookmanagement.dto.TransactionDto;
import com.readhub.bookmanagement.dto.TransactionRequest;
import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.model.TransactionStatus;
import com.readhub.bookmanagement.service.TransactionService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for borrowing books, returning books, and tracking borrow histories")
public class TransactionController {

    private final TransactionService transactionService;

    // Student: Request to borrow a book
    @PostMapping("/borrow")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')") 
    @Operation(summary = "Submit a borrow request", description = "Submits a new book borrow request for the authenticated user account.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Book borrowing requested successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or book copies are unavailable")
    public ResponseEntity<ApiResponse<TransactionDto>> borrowBook(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName(); 
        TransactionDto transaction = transactionService.requestBook(request.getBookId(), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(transaction, "Book borrowing requested successfully"));
    }

    // Admin: Update status (Approve, Reject, Confirm Borrow, Confirm Return)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')") 
    @Operation(summary = "Update transaction status", description = "Modifies the state of a specific transaction (e.g., APPROVED, REJECTED, BORROWED, RETURNED). Requires ADMIN permissions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction status updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid transaction transition state flow requested")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Target transaction record not found")
    public ResponseEntity<ApiResponse<TransactionDto>> updateStatus(
            @Parameter(description = "ID of the target transaction to update") @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        TransactionStatus status = TransactionStatus.valueOf(payload.get("status"));
        TransactionDto transaction = transactionService.updateStatus(id, status, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Transaction status updated successfully"));
    }

    // Admin: View all transactions
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all system transactions", description = "Retrieves a list of all user borrow transactions with optional status filter, sorting, and pagination. Requires ADMIN permissions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<ApiResponse<?>> getAllTransactions(
            @Parameter(description = "Transaction state status code to filter by") @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Page index for paginated output (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size for paginated output") @RequestParam(required = false) Integer size,
            @Parameter(description = "Database field name to sort transaction results by") @RequestParam(defaultValue = "requestDate") String sortBy,
            @Parameter(description = "Sorting order direction ('asc' or 'desc')") @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        if (page != null && size != null) {
            Page<TransactionDto> transactions = transactionService.getAllTransactions(status, PageRequest.of(page, size, sort));
            return ResponseEntity.ok(ApiResponse.success(transactions, "All transactions retrieved successfully"));
        }
        List<TransactionDto> transactions = transactionService.getAllTransactions(status, sort);
        return ResponseEntity.ok(ApiResponse.success(transactions, "All transactions retrieved successfully"));
    }

    // Student: View my history
    @GetMapping("/my-history")
    @Operation(summary = "Retrieve current user's transaction history", description = "Retrieves transaction history records for the currently authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User transaction history retrieved successfully")
    public ResponseEntity<ApiResponse<?>> getMyHistory(
            Authentication authentication,
            @Parameter(description = "Transaction state status code to filter by") @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Page index for paginated output (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size for paginated output") @RequestParam(required = false) Integer size,
            @Parameter(description = "Database field name to sort transaction history by") @RequestParam(defaultValue = "requestDate") String sortBy,
            @Parameter(description = "Sorting order direction ('asc' or 'desc')") @RequestParam(defaultValue = "desc") String direction
    ) {
        String email = authentication.getName();
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        if (page != null && size != null) {
            Page<TransactionDto> history = transactionService.getMyHistory(email, status, PageRequest.of(page, size, sort));
            return ResponseEntity.ok(ApiResponse.success(history, "Transaction history retrieved successfully"));
        }
        List<TransactionDto> history = transactionService.getMyHistory(email, status, sort);
        return ResponseEntity.ok(ApiResponse.success(history, "Transaction history retrieved successfully"));
    }
}