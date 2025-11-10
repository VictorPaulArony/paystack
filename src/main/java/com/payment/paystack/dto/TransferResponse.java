package com.payment.paystack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

// Transfer Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private boolean status;
    private String message;
    private TransferData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferData {
        private Long id;
        @JsonProperty("integration")
        private Long integrationId;
        private String domain;
        private BigDecimal amount;
        private String currency;
        private String source;
        private String reason;
        private String recipient;
        private String status;
        @JsonProperty("transfer_code")
        private String transferCode;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
    }
}
