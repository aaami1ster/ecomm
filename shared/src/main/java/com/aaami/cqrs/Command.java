package com.aaami.cqrs;

/**
 * Marker interface for all commands in the CQRS pattern.
 * Commands represent write operations that change the state of the system.
 *
 * @param <R> The type of result returned after command execution
 */
public interface Command<R> {
}

