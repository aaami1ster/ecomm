package com.aaami.product.config;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateProductCommand;
import com.aaami.shared.command.DeleteProductCommand;
import com.aaami.shared.command.UpdateProductCommand;
import com.aaami.product.handler.*;
import com.aaami.product.query.GetProductQuery;
import com.aaami.product.query.SearchProductsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class ProductCqrsConfig {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateProductCommandHandler createProductCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;
    private final DeleteProductCommandHandler deleteProductCommandHandler;
    private final GetProductQueryHandler getProductQueryHandler;
    private final SearchProductsQueryHandler searchProductsQueryHandler;
    
    @PostConstruct
    public void registerHandlers() {
        // Register command handlers
        commandBus.registerHandler(CreateProductCommand.class, createProductCommandHandler);
        commandBus.registerHandler(UpdateProductCommand.class, updateProductCommandHandler);
        commandBus.registerHandler(DeleteProductCommand.class, deleteProductCommandHandler);
        
        // Register query handlers
        queryBus.registerHandler(GetProductQuery.class, getProductQueryHandler);
        queryBus.registerHandler(SearchProductsQuery.class, searchProductsQueryHandler);
    }
}

