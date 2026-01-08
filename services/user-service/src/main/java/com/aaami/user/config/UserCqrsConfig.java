package com.aaami.user.config;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.user.handler.*;
import com.aaami.user.query.GetUserByEmailQuery;
import com.aaami.user.query.GetUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class UserCqrsConfig {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateUserCommandHandler createUserCommandHandler;
    private final UpdateUserCommandHandler updateUserCommandHandler;
    private final GetUserQueryHandler getUserQueryHandler;
    private final GetUserByEmailQueryHandler getUserByEmailQueryHandler;
    
    @PostConstruct
    public void registerHandlers() {
        // Register command handlers
        commandBus.registerHandler(CreateUserCommand.class, createUserCommandHandler);
        commandBus.registerHandler(UpdateUserCommand.class, updateUserCommandHandler);
        
        // Register query handlers
        queryBus.registerHandler(GetUserQuery.class, getUserQueryHandler);
        queryBus.registerHandler(GetUserByEmailQuery.class, getUserByEmailQueryHandler);
    }
}

