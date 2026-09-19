package com.temi.banking_backend.exception;

import com.temi.banking_backend.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                                                        String error,
                                                        String message) {
        ErrorResponse response = new ErrorResponse(status.value(), error, message);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields are invalid",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(
            {
                CurrencyMismatchException.class,
                InvalidTransferRecipientException.class,
                InvalidAccountStatusTransitionException.class,
                AccountNotActiveException.class
            }
    )
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            {
                InvalidCredentialsException.class,
                InvalidOtpException.class,
                InvalidPinException.class
            }
    )
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            UnauthorizedAccountAccessException.class
    )
    public ResponseEntity<ErrorResponse> handleForbidden(UnauthorizedAccountAccessException ex) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            {
                AccountNotFoundException.class,
                UserNotFoundException.class,
                TransactionNotFoundException.class
            }
    )
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            {
                EmailAlreadyExistsException.class,
                PhoneNumberAlreadyExistsException.class,
                InsufficientFundsException.class,
                TransactionNotReversibleException.class
            }
    )
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            PinLockedException.class
    )
    public ResponseEntity<ErrorResponse> handleTooManyRequests(PinLockedException ex) {
        return buildResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            EmailDeliveryException.class
    )
    public ResponseEntity<ErrorResponse> handleEmailDelivery(EmailDeliveryException ex) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                "Failed to send email. Please try again shortly."
        );
    }

    @ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred"
        );
    }
}