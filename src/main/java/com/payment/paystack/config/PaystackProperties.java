package com.payment.paystack.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "paystack")
public class PaystackProperties {
    
    private EnvironmentConfig test;
    private EnvironmentConfig production;
    private String activeEnv;
    private String webhookSecret;
    private String callbackUrl;
    
    @Data
    public static class EnvironmentConfig {
        private String secretKey;
        private String publicKey;
        private String baseUrl;
    }
    
    public EnvironmentConfig getActiveConfig() {
        return "production".equalsIgnoreCase(activeEnv) ? production : test;
    }
    
    public boolean isProduction() {
        return "production".equalsIgnoreCase(activeEnv);
    }
}
