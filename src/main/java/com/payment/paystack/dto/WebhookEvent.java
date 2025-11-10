package com.payment.paystack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

// Webhook Event
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {
    private String event;
    private Map<String, Object> data;
}
