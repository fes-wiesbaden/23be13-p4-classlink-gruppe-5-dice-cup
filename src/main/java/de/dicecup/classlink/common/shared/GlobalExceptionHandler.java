package de.dicecup.classlink.common.shared;

import de.dicecup.classlink.features.security.AccessTokenExpiredException;
import de.dicecup.classlink.features.security.refreshtoken.RefreshTokenException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT,
                "BUSINESS_RULE_VIOLATION",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (var error : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        }

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to perform this action"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = ex.getStatusCode() instanceof HttpStatus ? (HttpStatus) ex.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse body = ErrorResponse.of(
                status,
                ex.getStatusCode().is4xxClientError() ? "REQUEST_ERROR" : "SERVER_ERROR",
                ex.getReason() != null ? ex.getReason() : "Request failed"
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(AccessTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleAccessTokenExpired(AccessTokenExpiredException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                "ACCESS_TOKEN_EXPIRED",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenException(RefreshTokenException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse body = ErrorResponse.of(
                status,
                mapRefreshTokenCode(ex.getReason()),
                ex.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    private String mapRefreshTokenCode(RefreshTokenException.Reason reason) {
        return switch (reason) {
            case NOT_FOUND -> "REFRESH_TOKEN_NOT_FOUND";
            case INVALID -> "REFRESH_TOKEN_INVALID";
            case EXPIRED -> "REFRESH_TOKEN_EXPIRED";
            case REUSED -> "REFRESH_TOKEN_REUSED";
            case ROTATED -> "REFRESH_TOKEN_ALREADY_ROTATED";
            case MALFORMED -> "REFRESH_TOKEN_MALFORMED";
        };
    }
    

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String code,
            String message,
            Map<String, String> details
    ) {
        static ErrorResponse of(HttpStatus status, String code, String message) {
            return new ErrorResponse(
                    Instant.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    code,
                    message,
                    null
            );
        }

        static ErrorResponse of(HttpStatus status, String code, String message, Map<String, String> details) {
            return new ErrorResponse(
                    Instant.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    code,
                    message,
                    details
            );
        }
    }
}
