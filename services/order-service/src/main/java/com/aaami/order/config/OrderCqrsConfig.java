package com.aaami.order.config;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.order.handler.CreateOrderCommandHandler;
import com.aaami.order.handler.GetOrderQueryHandler;
import com.aaami.order.handler.GetUserOrdersQueryHandler;
import com.aaami.order.query.GetOrderQuery;
import com.aaami.order.query.GetUserOrdersQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class OrderCqrsConfig {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateOrderCommandHandler createOrderCommandHandler;
    private final GetOrderQueryHandler getOrderQueryHandler;
    private final GetUserOrdersQueryHandler getUserOrdersQueryHandler;
    
    @PostConstruct
    public void registerHandlers() {
        // Register command handlers
        commandBus.registerHandler(CreateOrderCommand.class, createOrderCommandHandler);
        
        // Register query handlers
        queryBus.registerHandler(GetOrderQuery.class, getOrderQueryHandler);
        queryBus.registerHandler(GetUserOrdersQuery.class, getUserOrdersQueryHandler);
    }
}

