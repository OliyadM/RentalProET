//package com.rentalpro.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now()));
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials", LocalDateTime.now()));
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
//        ex.printStackTrace();
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", LocalDateTime.now()));
//    }
//
////    @ExceptionHandler(Exception.class)
////    public ResponseEntity<?> handleException(Exception ex) {
////        ex.printStackTrace(); // 👈 ADD THIS
////        return ResponseEntity.status(500).body(ex.getMessage()); // 👈 show real error
////    }
//
//    public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
//}

package com.rentalpro.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Database Constraint Violations (Unique, Foreign Key, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Operation failed due to data constraint violation";
        // Walk the full cause chain — the useful constraint name is usually in the root cause
        String fullMessage = buildFullMessage(ex);

        if (fullMessage != null) {
            String lower = fullMessage.toLowerCase();
            if (lower.contains("email")) {
                message = "Email already registered";
            } else if (lower.contains("phone") || lower.contains("phone_number")) {
                message = "Phone number already registered";
            } else if (lower.contains("national_id") || lower.contains("nationalid")) {
                message = "National ID number already registered";
            } else if (lower.contains("tin")) {
                message = "TIN number already registered";
            } else if (lower.contains("unique") || lower.contains("duplicate") || lower.contains("already exists")) {
                message = "This record already exists";
            } else if (lower.contains("foreign key") || lower.contains("violates foreign key constraint")) {
                message = "Cannot delete this record because it is referenced by other data";
            } else if (lower.contains("not-null") || lower.contains("null value") || lower.contains("violates not-null")) {
                message = "Required field is missing";
            }
        }

        // Log the actual error for debugging
        System.err.println("DataIntegrityViolationException full chain: " + fullMessage);
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        LocalDateTime.now()
                ));
    }

    /** Walk the full Throwable cause chain and concatenate all messages. */
    private String buildFullMessage(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage()).append(" | ");
            }
            current = current.getCause();
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Handle Security Authorization Failures (403 Forbidden)
     * This triggers when @PreAuthorize fails.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Access Denied: You do not have the required permissions for this action.",
                        LocalDateTime.now()
                ));
    }

    /**
     * Handle Authentication Failures (401 Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid email or password",
                        LocalDateTime.now()
                ));
    }

    /**
     * Handle Invalid Path Variables (e.g., malformed UUID)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(), ex.getRequiredType().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, LocalDateTime.now()));
    }

    /**
     * Handle DTO Validation Failures (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle General Business/Runtime Logic Errors (400 Bad Request)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    /**
     * Handle IllegalStateException (400 Bad Request)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    /**
     * Catch-all for unexpected Server Errors (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the stack trace so you can debug the root cause in the console
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected internal server error occurred",
                        LocalDateTime.now()
                ));
    }

    public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
}