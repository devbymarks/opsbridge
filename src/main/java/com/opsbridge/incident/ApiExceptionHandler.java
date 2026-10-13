package com.opsbridge.incident;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IncidentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,Object> notFound(IncidentNotFoundException ex) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now()); body.put("status",404);
        body.put("error","Not Found"); body.put("message",ex.getMessage()); return body;
    }

    @ExceptionHandler(CheckNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,Object> checkNotFound(CheckNotFoundException ex) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now()); body.put("status",404);
        body.put("error","Not Found"); body.put("message",ex.getMessage()); return body;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> validation(MethodArgumentNotValidException ex) {
        Map<String,Object> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp",LocalDateTime.now()); body.put("status",400);
        body.put("error","Validation Failed"); body.put("fields",fields); return body;
    }
}
