package com.aaami.config;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.DeleteOrderCommand;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.order.handler.CreateOrderCommandHandler;
import com.aaami.order.handler.DeleteOrderCommandHandler;
import com.aaami.order.handler.GetAllOrdersQueryHandler;
import com.aaami.order.handler.GetOrderQueryHandler;
import com.aaami.order.handler.GetUserOrdersQueryHandler;
import com.aaami.order.handler.UpdateOrderCommandHandler;
import com.aaami.order.query.GetAllOrdersQuery;
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
    private final UpdateOrderCommandHandler updateOrderCommandHandler;
    private final DeleteOrderCommandHandler deleteOrderCommandHandler;
    private final GetOrderQueryHandler getOrderQueryHandler;
    private final GetUserOrdersQueryHandler getUserOrdersQueryHandler;
    private final GetAllOrdersQueryHandler getAllOrdersQueryHandler;
    
    @PostConstruct
    public void registerHandlers() {
        // Register command handlers
        commandBus.registerHandler(CreateOrderCommand.class, createOrderCommandHandler);
        commandBus.registerHandler(UpdateOrderCommand.class, updateOrderCommandHandler);
        commandBus.registerHandler(DeleteOrderCommand.class, deleteOrderCommandHandler);
        
        // Register query handlers
        queryBus.registerHandler(GetOrderQuery.class, getOrderQueryHandler);
        queryBus.registerHandler(GetUserOrdersQuery.class, getUserOrdersQueryHandler);
        queryBus.registerHandler(GetAllOrdersQuery.class, getAllOrdersQueryHandler);
    }
}

