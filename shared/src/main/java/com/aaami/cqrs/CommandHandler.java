package com.aaami.cqrs;

/**
 * Interface for handling commands in the CQRS pattern.
 * Each command handler is responsible for processing a specific command type.
 *
 * @param <C> The type of command this handler processes
 * @param <R> The type of result returned after command execution
 */
@FunctionalInterface
public interface CommandHandler<C extends Command<R>, R> {
    /**
     * Handles the given command and returns a result.
     *
     * @param command The command to handle
     * @return The result of command execution
     */
    R handle(C command);
}

