package com.payment.paystack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

// Verify Transaction Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTransactionResponse {
    private boolean status;
    private String message;
    private TransactionVerificationData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionVerificationData {
        private Long id;
        private String domain;
        private String status; // success, failed, abandoned
        private String reference;
        private BigDecimal amount;
        private String message;
        @JsonProperty("gateway_response")
        private String gatewayResponse;
        @JsonProperty("paid_at")
        private String paidAt;
        @JsonProperty("created_at")
        private String createdAt;
        private String channel;
        private String currency;
        @JsonProperty("ip_address")
        private String ipAddress;
        private Map<String, Object> metadata;
        private CustomerData customer;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CustomerData {
            private Long id;
            @JsonProperty("first_name")
            private String firstName;
            @JsonProperty("last_name")
            private String lastName;
            private String email;
            @JsonProperty("customer_code")
            private String customerCode;
            private String phone;
        }
    }
}

