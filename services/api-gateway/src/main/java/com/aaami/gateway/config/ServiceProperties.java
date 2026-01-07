package com.aaami.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {
    private String productServiceUrl = "http://localhost:8081";
    private String orderServiceUrl = "http://localhost:8082";
    private String userServiceUrl = "http://localhost:8083";
}

