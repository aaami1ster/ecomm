package com.aaami.cqrs;

import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandBusTest {

    private CommandBus commandBus;
    private CommandHandler<CreateUserCommand, UserDto> handler;

    @BeforeEach
    void setUp() {
        commandBus = new CommandBus();
        handler = mock(CommandHandler.class);
    }

    @Test
    void registerHandler_ShouldRegisterHandler() {
        // When
        commandBus.registerHandler(CreateUserCommand.class, handler);

        // Then - No exception thrown
        assertDoesNotThrow(() -> commandBus.registerHandler(CreateUserCommand.class, handler));
    }

    @Test
    void dispatch_ShouldCallHandler_WhenHandlerIsRegistered() {
        // Given
        CreateUserCommand command = new CreateUserCommand();
        UserDto expectedResult = UserDto.builder().id(1L).build();
        
        when(handler.handle(command)).thenReturn(expectedResult);
        commandBus.registerHandler(CreateUserCommand.class, handler);

        // When
        UserDto result = commandBus.dispatch(command);

        // Then
        assertEquals(expectedResult, result);
        verify(handler).handle(command);
    }

    @Test
    void dispatch_ShouldThrowException_WhenHandlerNotRegistered() {
        // Given
        CreateUserCommand command = new CreateUserCommand();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commandBus.dispatch(command));
        assertTrue(exception.getMessage().contains("No handler registered"));
    }
}

