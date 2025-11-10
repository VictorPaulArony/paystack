package com.payment.paystack.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PaystackException.class)
    public ResponseEntity<Map<String, Object>> handlePaystackException(PaystackException ex) {
        log.error("Paystack error: {}", ex.getMessage(), ex);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("error_type", "PAYSTACK_ERROR");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientError(HttpClientErrorException ex) {
        log.error("HTTP client error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Payment service error: " + ex.getMessage());
        error.put("error_type", "HTTP_CLIENT_ERROR");
        error.put("status_code", ex.getStatusCode().value());
        
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }
    
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpServerError(HttpServerErrorException ex) {
        log.error("HTTP server error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Payment service is temporarily unavailable");
        error.put("error_type", "HTTP_SERVER_ERROR");
        error.put("status_code", ex.getStatusCode().value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException ex) {
        log.error("Resource access error: {}", ex.getMessage(), ex);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Unable to reach payment service. Please try again later.");
        error.put("error_type", "CONNECTION_ERROR");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "An unexpected error occurred. Please try again later.");
        error.put("error_type", "INTERNAL_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}