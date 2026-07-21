package com.readhub.bookmanagement.exception;

import com.readhub.bookmanagement.dto.ApiFieldError;
import com.readhub.bookmanagement.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex, WebRequest request) {
        log.warn("Resource already exists: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex, WebRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileStorageException(FileStorageException ex, WebRequest request) {
        log.warn("File storage violation: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookCopyUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookCopyUnavailableException(BookCopyUnavailableException ex, WebRequest request) {
        log.warn("Book copy unavailable alert: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTransactionStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransactionStateException(InvalidTransactionStateException ex, WebRequest request) {
        log.warn("Invalid transaction transition flow: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DeleteViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeleteViolationException(DeleteViolationException ex, WebRequest request) {
        log.warn("Deletion violation check: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiResponse<Void> errorResponse = ApiResponse.error(
                "Access denied: " + ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials error for email login");
        ApiResponse<Void> errorResponse = ApiResponse.error(
                "Invalid email or password",
                HttpStatus.UNAUTHORIZED.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        List<ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiFieldError(err.getField(), err.getDefaultMessage(), err.getRejectedValue()))
                .collect(Collectors.toList());
        
        String summaryMessage = "Validation failed: " + fieldErrors.size() + " error(s)";
        log.warn("Validation failure: {}", summaryMessage);

        ApiResponse<Void> errorResponse = ApiResponse.error(
                summaryMessage,
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                fieldErrors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred during request handling", ex);
        ApiResponse<Void> errorResponse = ApiResponse.error(
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
