package com.payment.paystack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;   

// Create Transfer Recipient Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipientResponse {
    private boolean status;
    private String message;
    private RecipientData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientData {
        private Long id;
        private String domain;
        private String type;
        private String currency;
        private String name;
        @JsonProperty("details")
        private RecipientDetails details;
        @JsonProperty("recipient_code")
        private String recipientCode;
        private boolean active;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RecipientDetails {
            @JsonProperty("account_number")
            private String accountNumber;
            @JsonProperty("account_name")
            private String accountName;
            @JsonProperty("bank_code")
            private String bankCode;
            @JsonProperty("bank_name")
            private String bankName;
        }
    }
}