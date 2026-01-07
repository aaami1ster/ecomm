package com.aaami.cqrs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command bus for dispatching commands to their respective handlers.
 * Implements the mediator pattern to decouple command senders from handlers.
 */
public class CommandBus {
    private final Map<Class<? extends Command<?>>, CommandHandler<?, ?>> handlers = new ConcurrentHashMap<>();

    /**
     * Registers a command handler for a specific command type.
     *
     * @param commandType The type of command
     * @param handler     The handler for this command type
     * @param <C>         The command type
     * @param <R>         The result type
     */
    @SuppressWarnings("unchecked")
    public <C extends Command<R>, R> void registerHandler(Class<C> commandType, CommandHandler<C, R> handler) {
        handlers.put(commandType, handler);
    }

    /**
     * Dispatches a command to its registered handler.
     *
     * @param command The command to dispatch
     * @param <C>     The command type
     * @param <R>     The result type
     * @return The result of command execution
     * @throws IllegalArgumentException if no handler is registered for the command type
     */
    @SuppressWarnings("unchecked")
    public <C extends Command<R>, R> R dispatch(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for command type: " + command.getClass().getName());
        }
        return handler.handle(command);
    }
}

