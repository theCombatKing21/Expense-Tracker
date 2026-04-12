package com.SpringBootProject.ExpenseTracker.exception;

// ResourceNotFoundException and DuplicateResourceException are in their own
// separate files in this same package — no import needed within the same package.

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// ============================================================
// CUSTOM EXCEPTION
// ============================================================
// We define this as an inner class here for convenience.
// In a larger project, this lives in its own file: ResourceNotFoundException.java


// ============================================================
// GLOBAL EXCEPTION HANDLER
// ============================================================
// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// It is a single class that intercepts exceptions thrown ANYWHERE in your
// controllers/services and converts them into clean, structured JSON responses.
//
// Without this, Spring Boot returns its own ugly default error JSON (with a
// "timestamp", "status", "error", "path" structure that's hard to work with).
// With this, YOU control the error format — professional and consistent.
@RestControllerAdvice
class GlobalExceptionHandler {

    // This inner class is the structure of ALL our error responses.
    // Every error, whether 404 or 400, will return JSON in this shape:
    // { "timestamp": "...", "status": 404, "error": "Not Found", "message": "..." }
    record ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {}

    // -----------------------------------------------------------------------
    // Handles: ResourceNotFoundException thrown in any service
    // Returns: 404 Not Found
    // -----------------------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // -----------------------------------------------------------------------
    // Handles: DuplicateResourceException (e.g., email already exists)
    // Returns: 409 Conflict
    // -----------------------------------------------------------------------
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // -----------------------------------------------------------------------
    // Handles: @Valid validation failures on request DTOs
    // Returns: 400 Bad Request with a map of field → error message
    // -----------------------------------------------------------------------
    // When a request body fails @NotBlank, @Positive etc., Spring throws
    // MethodArgumentNotValidException. We catch it here and build a
    // friendly map like: { "amount": "Amount must be positive", "email": "..." }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        // ex.getBindingResult().getAllErrors() gives you all validation failures.
        // We cast each to FieldError to get the specific field name.
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 400);
        response.put("error", "Validation Failed");
        response.put("fieldErrors", fieldErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // -----------------------------------------------------------------------
    // Catch-all: anything unexpected
    // Returns: 500 Internal Server Error
    // -----------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
                // Note: we DON'T expose ex.getMessage() here in production — it could
                // leak internal implementation details to clients. Log it instead.
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}