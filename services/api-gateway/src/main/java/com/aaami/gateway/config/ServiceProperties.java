package com.aaami.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {
    private String productServiceUrl;
    private String orderServiceUrl;
    private String userServiceUrl;
}

