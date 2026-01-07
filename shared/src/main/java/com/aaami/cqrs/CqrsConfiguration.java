package com.aaami.cqrs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for CQRS infrastructure.
 * Provides beans for command and query buses.
 */
@Configuration
public class CqrsConfiguration {
    
    @Bean
    public CommandBus commandBus() {
        return new CommandBus();
    }
    
    @Bean
    public QueryBus queryBus() {
        return new QueryBus();
    }
}

