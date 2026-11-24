package edu.campus.controller;

import edu.campus.service.Business;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class Errors {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String,String>> business(ResponseStatusException error) {
    return ResponseEntity.status(error.getStatusCode()).body(Map.of("error",error.getReason()==null?"请求失败":error.getReason()));
  }
  @ExceptionHandler(Business.BadCodeException.class)
  public ResponseEntity<Map<String,String>> code(Business.BadCodeException error) {
    return ResponseEntity.status(409).body(Map.of("error",error.getMessage()));
  }
}
