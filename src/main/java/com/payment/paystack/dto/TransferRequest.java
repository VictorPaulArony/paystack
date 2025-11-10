package com.payment.paystack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

// Transfer Request (B2C)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String source; // "balance"
    private BigDecimal amount; // in kobo
    private String recipient;
    private String reason;
    private String currency;
    private String reference;
}