package org.sid.gatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors for @Valid on request bodies
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", details);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            "Input validation failed. Please check the request data.",
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle validation errors for @Validated on path variables and request parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> details = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            details.put(propertyPath, message);
        }
        
        log.warn("Constraint violation: {}", details);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Constraint Violation",
            "Request constraints violated. Please check the request parameters.",
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle type mismatch errors (e.g., passing a string where a number is expected)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> details = new HashMap<>();
        details.put("parameter", ex.getName());
        details.put("invalidValue", String.valueOf(ex.getValue()));
        details.put("requiredType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        log.warn("Type mismatch: parameter '{}' with value '{}' cannot be converted to {}", 
                 ex.getName(), ex.getValue(), ex.getRequiredType());
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Type Mismatch",
            String.format("Parameter '%s' has an invalid type", ex.getName()),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle resource not found
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            "The requested resource was not found",
            null
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle access denied (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "You do not have permission to access this resource",
            null
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response structure
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, String> details;

        public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, Map<String, String> details) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.details = details;
        }

        // Getters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, String> getDetails() {
            return details;
        }
    }
}
