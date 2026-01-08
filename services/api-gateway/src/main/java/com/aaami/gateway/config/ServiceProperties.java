package com.aaami.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {
    private String productServiceUrl;
    private String orderServiceUrl;
    private String userServiceUrl;
    
    public String getProductServiceUrl() {
        return productServiceUrl;
    }
    
    public void setProductServiceUrl(String productServiceUrl) {
        this.productServiceUrl = productServiceUrl;
    }
    
    public String getOrderServiceUrl() {
        return orderServiceUrl;
    }
    
    public void setOrderServiceUrl(String orderServiceUrl) {
        this.orderServiceUrl = orderServiceUrl;
    }
    
    public String getUserServiceUrl() {
        return userServiceUrl;
    }
    
    public void setUserServiceUrl(String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
    }
}

