package com.fugitivalamadrid.api.userapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final String TIMESTAMP = LocalDateTime.now().toString();

    /**
     * Handle User Not Found Exception
     */
    @SuppressWarnings("unused")
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", TIMESTAMP,
                "status", 404,
                "error", "Not Found",
                "message", ex.getMessage()
        ));
    }

    /**
     * Handle Rate Limit Exception
     */
    @SuppressWarnings("unused")
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "timestamp", TIMESTAMP,
                "status", 429,
                "error", "Too Many Requests",
                "message", ex.getMessage(),
                "maxRequests", ex.getMaxRequests(),
                "windowSizeMillis", ex.getWindowSizeMillis(),
                "timeUntilReset", ex.getTimeUntilReset()
        ));
    }
}
