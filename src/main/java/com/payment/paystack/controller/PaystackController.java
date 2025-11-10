package com.payment.paystack.controller;

import com.payment.paystack.dto.CreateRecipientResponse;
import com.payment.paystack.dto.InitializeTransactionResponse;
import com.payment.paystack.dto.TransferResponse;
import com.payment.paystack.dto.VerifyTransactionResponse;
import com.payment.paystack.service.PaystackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/paystack")
@RequiredArgsConstructor
public class PaystackController {
    
    private final PaystackService paystackService;
    
    /**
     * Initialize a payment transaction (STK Push equivalent)
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializePayment(
            @RequestBody Map<String, Object> request) {
        
        try {
            String email = (String) request.get("email");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String currency = (String) request.getOrDefault("currency", "NGN");
            String[] channels = request.containsKey("channels") 
                    ? ((java.util.List<String>) request.get("channels")).toArray(new String[0])
                    : null;
            
            InitializeTransactionResponse response = paystackService.initializeTransaction(
                    email, amount, currency, channels
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Transaction initialized successfully");
            result.put("data", response.getData());
            result.put("environment", paystackService.getCurrentEnvironment());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error initializing payment: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to initialize payment: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Verify a payment transaction
     */
    @GetMapping("/verify/{reference}")
    public ResponseEntity<Map<String, Object>> verifyPayment(@PathVariable String reference) {
        
        try {
            VerifyTransactionResponse response = paystackService.verifyTransaction(reference);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isStatus());
            result.put("message", response.getMessage());
            result.put("data", response.getData());
            result.put("environment", paystackService.getCurrentEnvironment());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to verify payment: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Create a transfer recipient for B2C
     */
    @PostMapping("/recipient")
    public ResponseEntity<Map<String, Object>> createRecipient(
            @RequestBody Map<String, Object> request) {
        
        try {
            String type = (String) request.get("type"); // "mobile_money" or "nuban"
            String name = (String) request.get("name");
            String accountNumber = (String) request.get("account_number");
            String bankCode = (String) request.get("bank_code");
            String currency = (String) request.getOrDefault("currency", "NGN");
            
            CreateRecipientResponse response = paystackService.createTransferRecipient(
                    type, name, accountNumber, bankCode, currency
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Recipient created successfully");
            result.put("data", response.getData());
            result.put("environment", paystackService.getCurrentEnvironment());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error creating recipient: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to create recipient: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Initiate a B2C transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> initiateTransfer(
            @RequestBody Map<String, Object> request) {
        
        try {
            String recipientCode = (String) request.get("recipient_code");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String reason = (String) request.get("reason");
            String currency = (String) request.getOrDefault("currency", "NGN");
            
            TransferResponse response = paystackService.initiateTransfer(
                    recipientCode, amount, reason, currency
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Transfer initiated successfully");
            result.put("data", response.getData());
            result.put("environment", paystackService.getCurrentEnvironment());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error initiating transfer: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to initiate transfer: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Webhook endpoint for Paystack events
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {
        
        try {
            if (!paystackService.verifyWebhookSignature(payload, signature)) {
                log.warn("Invalid webhook signature received");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }
            
            // Parse the webhook event
            // You can use Jackson ObjectMapper to parse the payload
            log.info("Webhook received in {} environment: {}", 
                    paystackService.getCurrentEnvironment(), payload);
            
            // Process the webhook based on event type
            // Example events: charge.success, transfer.success, transfer.failed
            
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }
    
    /**
     * Callback endpoint after payment
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String trxref) {
        
        try {
            String ref = reference != null ? reference : trxref;
            
            if (ref == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "No reference provided");
                return ResponseEntity.badRequest().body(error);
            }
            
            VerifyTransactionResponse response = paystackService.verifyTransaction(ref);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isStatus());
            result.put("message", "Payment " + response.getData().getStatus());
            result.put("data", response.getData());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in callback: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to process callback: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get current environment status
     */
    @GetMapping("/environment")
    public ResponseEntity<Map<String, Object>> getEnvironment() {
        Map<String, Object> result = new HashMap<>();
        result.put("environment", paystackService.getCurrentEnvironment());
        result.put("is_production", paystackService.isProduction());
        
        return ResponseEntity.ok(result);
    }
}
