package com.payment.paystack.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.payment.paystack.config.PaystackProperties;
import com.payment.paystack.dto.CreateRecipientRequest;
import com.payment.paystack.dto.CreateRecipientResponse;
import com.payment.paystack.dto.InitializeTransactionRequest;
import com.payment.paystack.dto.InitializeTransactionResponse;
import com.payment.paystack.dto.TransferRequest;
import com.payment.paystack.dto.TransferResponse;
import com.payment.paystack.dto.VerifyTransactionResponse;
import com.payment.paystack.exception.PaystackException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackService {
    
    private final RestTemplate paystackRestTemplate;
    private final PaystackProperties paystackProperties;
    
    /**
     * Initialize a transaction for customer payment (STK Push equivalent)
     */
    public InitializeTransactionResponse initializeTransaction(
            String email,
            BigDecimal amount,
            String currency,
            String[] channels) {
        
        String reference = generateReference();
        
        InitializeTransactionRequest request = InitializeTransactionRequest.builder()
                .email(email)
                .amount(convertToKobo(amount))
                .currency(currency != null ? currency : "NGN")
                .reference(reference)
                .callbackUrl(paystackProperties.getCallbackUrl())
                .channels(channels != null ? channels : new String[]{"mobile_money", "card", "bank"})
                .build();
        
        String url = paystackProperties.getActiveConfig().getBaseUrl() + "/transaction/initialize";
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<InitializeTransactionRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Initializing transaction for email: {} with reference: {} in {} environment",
                    email, reference, paystackProperties.getActiveEnv());
            
            ResponseEntity<InitializeTransactionResponse> response = paystackRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    InitializeTransactionResponse.class
            );
            
            if (response.getBody() != null && response.getBody().isStatus()) {
                log.info("Transaction initialized successfully: {}", reference);
                return response.getBody();
            } else {
                throw new PaystackException("Failed to initialize transaction: " + 
                        (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
            }
            
        } catch (Exception e) {
            log.error("Error initializing transaction: {}", e.getMessage(), e);
            throw new PaystackException("Error initializing transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify a transaction
     */
    public VerifyTransactionResponse verifyTransaction(String reference) {
        String url = paystackProperties.getActiveConfig().getBaseUrl() + "/transaction/verify/" + reference;
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            log.info("Verifying transaction with reference: {} in {} environment",
                    reference, paystackProperties.getActiveEnv());
            
            ResponseEntity<VerifyTransactionResponse> response = paystackRestTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    VerifyTransactionResponse.class
            );
            
            if (response.getBody() != null) {
                log.info("Transaction verification response - Status: {}, Message: {}",
                        response.getBody().getData().getStatus(),
                        response.getBody().getMessage());
                return response.getBody();
            } else {
                throw new PaystackException("Failed to verify transaction: No response body");
            }
            
        } catch (Exception e) {
            log.error("Error verifying transaction: {}", e.getMessage(), e);
            throw new PaystackException("Error verifying transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a transfer recipient (for B2C transfers)
     */
    public CreateRecipientResponse createTransferRecipient(
            String type,
            String name,
            String accountNumber,
            String bankCode,
            String currency) {
        
        CreateRecipientRequest request = CreateRecipientRequest.builder()
                .type(type) // "mobile_money" or "nuban"
                .name(name)
                .accountNumber(accountNumber)
                .bankCode(bankCode)
                .currency(currency != null ? currency : "NGN")
                .build();
        
        String url = paystackProperties.getActiveConfig().getBaseUrl() + "/transferrecipient";
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<CreateRecipientRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Creating transfer recipient: {} in {} environment",
                    name, paystackProperties.getActiveEnv());
            
            ResponseEntity<CreateRecipientResponse> response = paystackRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CreateRecipientResponse.class
            );
            
            if (response.getBody() != null && response.getBody().isStatus()) {
                log.info("Transfer recipient created successfully: {}", 
                        response.getBody().getData().getRecipientCode());
                return response.getBody();
            } else {
                throw new PaystackException("Failed to create recipient: " + 
                        (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
            }
            
        } catch (Exception e) {
            log.error("Error creating transfer recipient: {}", e.getMessage(), e);
            throw new PaystackException("Error creating transfer recipient: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initiate a B2C transfer
     */
    public TransferResponse initiateTransfer(
            String recipientCode,
            BigDecimal amount,
            String reason,
            String currency) {
        
        String reference = generateReference();
        
        TransferRequest request = TransferRequest.builder()
                .source("balance")
                .amount(convertToKobo(amount))
                .recipient(recipientCode)
                .reason(reason)
                .currency(currency != null ? currency : "NGN")
                .reference(reference)
                .build();
        
        String url = paystackProperties.getActiveConfig().getBaseUrl() + "/transfer";
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<TransferRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Initiating transfer to recipient: {} with reference: {} in {} environment",
                    recipientCode, reference, paystackProperties.getActiveEnv());
            
            ResponseEntity<TransferResponse> response = paystackRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TransferResponse.class
            );
            
            if (response.getBody() != null && response.getBody().isStatus()) {
                log.info("Transfer initiated successfully: {}", reference);
                return response.getBody();
            } else {
                throw new PaystackException("Failed to initiate transfer: " + 
                        (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
            }
            
        } catch (Exception e) {
            log.error("Error initiating transfer: {}", e.getMessage(), e);
            throw new PaystackException("Error initiating transfer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String computedSignature = computeHmacSha512(
                    payload,
                    paystackProperties.getWebhookSecret()
            );
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get current environment
     */
    public String getCurrentEnvironment() {
        return paystackProperties.getActiveEnv();
    }
    
    /**
     * Check if running in production
     */
    public boolean isProduction() {
        return paystackProperties.isProduction();
    }
    
    // Helper methods
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(paystackProperties.getActiveConfig().getSecretKey());
        return headers;
    }
    
    private String generateReference() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    private BigDecimal convertToKobo(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100"));
    }
    
    private String computeHmacSha512(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
        );
        mac.init(secretKeySpec);
        byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmac);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
