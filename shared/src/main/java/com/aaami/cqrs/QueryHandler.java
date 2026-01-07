package com.aaami.cqrs;

/**
 * Interface for handling queries in the CQRS pattern.
 * Each query handler is responsible for processing a specific query type.
 *
 * @param <Q> The type of query this handler processes
 * @param <R> The type of result returned by the query
 */
@FunctionalInterface
public interface QueryHandler<Q extends Query<R>, R> {
    /**
     * Handles the given query and returns a result.
     *
     * @param query The query to handle
     * @return The result of the query
     */
    R handle(Q query);
}

