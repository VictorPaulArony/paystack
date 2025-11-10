package com.payment.paystack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

// Create Transfer Recipient Request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipientRequest {
    private String type; // "mobile_money" or "nuban"
    private String name;
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("bank_code")
    private String bankCode;
    private String currency;
}
