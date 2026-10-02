package com.jobrec.application.exception;
import com.jobrec.application.job.JobServiceUnavailableException; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestControllerAdvice public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e) { Map<String,String> details=new HashMap<>(); e.getBindingResult().getFieldErrors().forEach(x -> details.put(x.getField(), x.getDefaultMessage())); return error(400,"Validation failed",details); }
    @ExceptionHandler(DuplicateApplicationException.class) ResponseEntity<?> duplicate(DuplicateApplicationException e){return error(409,e.getMessage(),null);}
    @ExceptionHandler(OwnershipException.class) ResponseEntity<?> ownership(OwnershipException e){return error(403,e.getMessage(),null);}
    @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<?> missing(ResourceNotFoundException e){return error(404,e.getMessage(),null);}
    @ExceptionHandler(JobServiceUnavailableException.class) ResponseEntity<?> unavailable(JobServiceUnavailableException e){return error(503,e.getMessage(),null);}
    @ExceptionHandler({IllegalArgumentException.class, DataIntegrityViolationException.class}) ResponseEntity<?> bad(Exception e){return error(400,e instanceof DataIntegrityViolationException ? "Data integrity violation" : e.getMessage(),null);}
    private ResponseEntity<Map<String,Object>> error(int status,String message,Object details){Map<String,Object> body=new LinkedHashMap<>(); body.put("status",status); body.put("error",HttpStatus.valueOf(status).getReasonPhrase()); body.put("message",message); if(details!=null)body.put("details",details); return ResponseEntity.status(status).body(body);}
}
