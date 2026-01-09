package com.aaami.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret = "your-secret-key-change-this-in-production-use-a-long-random-string";
    private long expiration = 86400000; // 24 hours in milliseconds
}

