package com.jobrec.recommendation.exception;

import com.jobrec.recommendation.client.DownstreamServiceException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class) void accessDenied(AccessDeniedException ex) { throw ex; }
    @ExceptionHandler(DownstreamServiceException.class) ResponseEntity<?> unavailable(DownstreamServiceException ex) { return error(503, ex.getMessage()); }
    @ExceptionHandler(RecruiterOwnershipException.class) ResponseEntity<?> ownership(RecruiterOwnershipException ex) { return error(403, ex.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> badRequest(IllegalArgumentException ex) { return error(400, ex.getMessage()); }
    @ExceptionHandler(Exception.class) ResponseEntity<?> generic(Exception ex) { return error(500, "An internal error occurred"); }
    private ResponseEntity<Map<String,Object>> error(int status, String message) { Map<String,Object> body=new LinkedHashMap<>(); body.put("status",status); body.put("error",HttpStatus.valueOf(status).getReasonPhrase()); body.put("message",message); return ResponseEntity.status(status).body(body); }
}
