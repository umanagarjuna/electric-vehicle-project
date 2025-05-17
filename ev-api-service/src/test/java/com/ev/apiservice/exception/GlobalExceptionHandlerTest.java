package com.ev.apiservice.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        // No setup needed as the exception handler doesn't have dependencies
    }

    @Test
    void handleEntityNotFoundException_ShouldReturnNotFoundStatus() {
        // Arrange
        String errorMessage = "Vehicle not found with VIN: TEST123";
        EntityNotFoundException ex = new EntityNotFoundException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleEntityNotFoundException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getFieldErrors()).isNull();
        assertThat(response.getBody().getGlobalErrors()).isNull();
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequestStatus() {
        // Arrange
        String errorMessage = "Invalid VIN format";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getFieldErrors()).isNull();
        assertThat(response.getBody().getGlobalErrors()).isNull();
    }

    @Test
    void handleValidationExceptions_WithFieldErrors_ShouldReturnBadRequestWithFieldErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("electricVehicleDTO", "vin", "VIN cannot be blank"));
        fieldErrors.add(new FieldError("electricVehicleDTO", "make", "Make cannot be blank"));

        List<ObjectError> globalErrors = new ArrayList<>();
        globalErrors.add(new ObjectError("electricVehicleDTO", "Vehicle data is invalid"));

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(bindingResult.getGlobalErrors()).thenReturn(globalErrors);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getMessage()).isEqualTo("Validation Failed");

        // Check field errors
        assertThat(response.getBody().getFieldErrors()).isNotNull();
        assertThat(response.getBody().getFieldErrors()).hasSize(2);
        assertThat(response.getBody().getFieldErrors()).containsKey("vin");
        assertThat(response.getBody().getFieldErrors()).containsKey("make");

        // Check global errors
        assertThat(response.getBody().getGlobalErrors()).isNotNull();
        assertThat(response.getBody().getGlobalErrors()).hasSize(1);
        assertThat(response.getBody().getGlobalErrors().get(0)).isEqualTo("Vehicle data is invalid");
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorStatus() {
        // Arrange
        String errorMessage = "An unexpected database error occurred";
        Exception ex = new Exception(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        // The error message should be a generic one, not the actual exception message
        assertThat(response.getBody().getMessage()).contains("unexpected internal server error");
        assertThat(response.getBody().getFieldErrors()).isNull();
        assertThat(response.getBody().getGlobalErrors()).isNull();
    }

    @Test
    void handleValidationExceptions_WithEmptyErrors_ShouldHandleGracefully() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
        when(bindingResult.getGlobalErrors()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getMessage()).isEqualTo("Validation Failed");
        // Both errors should be null since they were empty collections
        assertThat(response.getBody().getFieldErrors()).isNull();
        assertThat(response.getBody().getGlobalErrors()).isNull();
    }

    @Test
    void errorResponse_GettersTest() {
        // This test ensures the ErrorResponse inner class getters are covered

        // Arrange - Create an error response with all fields populated
        int status = 400;
        String message = "Test Error Message";
        var fieldErrors = Collections.singletonMap("field", "defaultMessage");
        var globalErrors = Collections.singletonList("Global error message");

        // Act
        GlobalExceptionHandler.ErrorResponse response = new GlobalExceptionHandler.ErrorResponse(
                status, message, fieldErrors, globalErrors
        );

        // Assert - Test all getters
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getFieldErrors()).isEqualTo(fieldErrors);
        assertThat(response.getGlobalErrors()).isEqualTo(globalErrors);
    }

    @Test
    void handleGenericException_CustomException_ShouldReturnInternalServerError() {
        // Arrange
        class CustomException extends Exception {
            public CustomException(String message) {
                super(message);
            }
        }

        CustomException ex = new CustomException("A custom exception occurred");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().getMessage()).contains("internal server error");
    }
}