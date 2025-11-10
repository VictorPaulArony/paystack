package com.payment.paystack.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

// Initialize Transaction Request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitializeTransactionRequest {
    private String email;
    private BigDecimal amount; // in kobo (smallest currency unit)
    private String currency;
    private String reference;
    @JsonProperty("callback_url")
    private String callbackUrl;
    private Map<String, Object> metadata;
    private String[] channels; // ["mobile_money", "card", "bank"]
}

