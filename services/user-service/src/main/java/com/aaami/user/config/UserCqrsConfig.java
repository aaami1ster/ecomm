package com.aaami.user.config;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.DeleteUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.user.handler.*;
import com.aaami.user.query.GetAllUsersQuery;
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
    private final DeleteUserCommandHandler deleteUserCommandHandler;
    private final GetUserQueryHandler getUserQueryHandler;
    private final GetUserByEmailQueryHandler getUserByEmailQueryHandler;
    private final GetAllUsersQueryHandler getAllUsersQueryHandler;
    
    @PostConstruct
    public void registerHandlers() {
        // Register command handlers
        commandBus.registerHandler(CreateUserCommand.class, createUserCommandHandler);
        commandBus.registerHandler(UpdateUserCommand.class, updateUserCommandHandler);
        commandBus.registerHandler(DeleteUserCommand.class, deleteUserCommandHandler);
        
        // Register query handlers
        queryBus.registerHandler(GetUserQuery.class, getUserQueryHandler);
        queryBus.registerHandler(GetUserByEmailQuery.class, getUserByEmailQueryHandler);
        queryBus.registerHandler(GetAllUsersQuery.class, getAllUsersQueryHandler);
    }
}

