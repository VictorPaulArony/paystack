package com.payment.paystack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

// Initialize Transaction Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitializeTransactionResponse {
    private boolean status;
    private String message;
    private TransactionData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionData {
        @JsonProperty("authorization_url")
        private String authorizationUrl;
        @JsonProperty("access_code")
        private String accessCode;
        private String reference;
    }
}
