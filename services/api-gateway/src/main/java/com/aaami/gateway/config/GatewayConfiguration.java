package com.aaami.gateway.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewayConfiguration {
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
    
    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configure Jackson message converter with the ObjectMapper
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(objectMapper);
        
        // Replace default Jackson converter with our configured one
        restTemplate.getMessageConverters().removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        restTemplate.getMessageConverters().add(jacksonConverter);
        
        return restTemplate;
    }
}

