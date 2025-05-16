package com.ev.apiservice.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the EV API service.
 * Catches specified exceptions thrown by controllers or services and returns a standardized error response.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles {@link EntityNotFoundException}, typically thrown when a resource is not found.
     * @param ex The caught EntityNotFoundException.
     * @return A ResponseEntity with HTTP 404 (Not Found) status and an error message.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage()); // Log with less severity
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link IllegalArgumentException}, often used for invalid input parameters not caught by bean validation.
     * @param ex The caught IllegalArgumentException.
     * @return A ResponseEntity with HTTP 400 (Bad Request) status and an error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument provided: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link MethodArgumentNotValidException}, thrown when Spring's @Valid validation fails on a request body.
     * @param ex The caught MethodArgumentNotValidException.
     * @return A ResponseEntity with HTTP 400 (Bad Request) status and detailed validation error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Collect all field errors and their default messages
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));

        // Collect all global errors (non-field specific)
        List<String> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        // Combine field and global errors for a comprehensive message
        StringBuilder errorMessageBuilder = new StringBuilder("Validation failed. ");
        if (!fieldErrors.isEmpty()) {
            errorMessageBuilder.append("Field errors: ").append(fieldErrors).append(". ");
        }
        if (!globalErrors.isEmpty()) {
            errorMessageBuilder.append("Global errors: ").append(String.join(", ", globalErrors)).append(".");
        }
        String detailedMessage = errorMessageBuilder.toString().trim();

        log.warn("Validation error: {}", detailedMessage, ex); // Log the detailed validation errors
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed", fieldErrors, globalErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles all other unhandled exceptions as a catch-all.
     * @param ex The caught Exception.
     * @return A ResponseEntity with HTTP 500 (Internal Server Error) status and a generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex); // Log with stack trace for unexpected errors
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected internal server error occurred. Please contact support or check server logs.",
                null);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Inner class (or record) for a standardized error response structure.
     */
    public static class ErrorResponse {
        private final int status;
        private final String message;
        private final Map<String, String> fieldErrors; // For MethodArgumentNotValidException
        private final List<String> globalErrors; // For MethodArgumentNotValidException

        public ErrorResponse(int status, String message, Map<String, String> fieldErrors, List<String> globalErrors) {
            this.status = status;
            this.message = message;
            this.fieldErrors = (fieldErrors == null || fieldErrors.isEmpty()) ? null : fieldErrors;
            this.globalErrors = (globalErrors == null || globalErrors.isEmpty()) ? null : globalErrors;
        }
        public ErrorResponse(int status, String message, List<String> validationErrors) {
            this.status = status;
            this.message = message;
            this.fieldErrors = null;
            this.globalErrors = validationErrors;
        }

        // Getters are needed for Jackson serialization
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public List<String> getGlobalErrors() { return globalErrors; }
    }
}