package com.jobrec.notification.exception;

import com.jobrec.notification.service.NotificationNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotificationNotFoundException.class) ResponseEntity<?> notFound(NotificationNotFoundException exception) { return error(404, exception.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> badRequest(IllegalArgumentException exception) { return error(400, exception.getMessage()); }
    private ResponseEntity<Map<String, Object>> error(int status, String message) { return ResponseEntity.status(status).body(Map.of("status", status, "error", HttpStatus.valueOf(status).getReasonPhrase(), "message", message)); }
}
