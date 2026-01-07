package com.aaami.cqrs;

/**
 * Marker interface for all queries in the CQRS pattern.
 * Queries represent read operations that do not change the state of the system.
 *
 * @param <R> The type of result returned by the query
 */
public interface Query<R> {
}

