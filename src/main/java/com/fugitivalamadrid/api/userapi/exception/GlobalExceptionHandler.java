package com.fugitivalamadrid.api.userapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final String TIMESTAMP = LocalDateTime.now().toString();

    // Response field constants
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String STATUS_KEY = "status";
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    // HTTP Status codes
    private static final int NOT_FOUND_STATUS = 404;
    private static final int CONFLICT_STATUS = 409;
    private static final int TOO_MANY_REQUESTS_STATUS = 429;

    // Error messages
    private static final String NOT_FOUND_ERROR = "Not Found";
    private static final String CONFLICT_ERROR = "Conflict";
    private static final String TOO_MANY_REQUESTS_ERROR = "Too Many Requests";
    private static final String DUPLICATE_USER_MESSAGE = "A user with this email or username already exists";

    /**
     * Handle User Not Found Exception
     */
    @SuppressWarnings("unused")
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, TIMESTAMP,
                STATUS_KEY, NOT_FOUND_STATUS,
                ERROR_KEY, NOT_FOUND_ERROR,
                MESSAGE_KEY, ex.getMessage()
        ));
    }

    /**
     * Handle Duplicate Key Exception (e.g., email or username already exists)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateKey(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                TIMESTAMP_KEY, TIMESTAMP,
                STATUS_KEY, CONFLICT_STATUS,
                ERROR_KEY, CONFLICT_ERROR,
                MESSAGE_KEY, DUPLICATE_USER_MESSAGE
        ));
    }

    /**
     * Handle Rate Limit Exception
     */
    @SuppressWarnings("unused")
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                TIMESTAMP_KEY, TIMESTAMP,
                STATUS_KEY, TOO_MANY_REQUESTS_STATUS,
                ERROR_KEY, TOO_MANY_REQUESTS_ERROR,
                MESSAGE_KEY, ex.getMessage(),
                "maxRequests", ex.getMaxRequests(),
                "windowSizeMillis", ex.getWindowSizeMillis(),
                "timeUntilReset", ex.getTimeUntilReset()
        ));
    }
}
