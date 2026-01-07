package com.aaami.cqrs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Query bus for dispatching queries to their respective handlers.
 * Implements the mediator pattern to decouple query senders from handlers.
 */
public class QueryBus {
    private final Map<Class<? extends Query<?>>, QueryHandler<?, ?>> handlers = new ConcurrentHashMap<>();

    /**
     * Registers a query handler for a specific query type.
     *
     * @param queryType The type of query
     * @param handler   The handler for this query type
     * @param <Q>       The query type
     * @param <R>       The result type
     */
    @SuppressWarnings("unchecked")
    public <Q extends Query<R>, R> void registerHandler(Class<Q> queryType, QueryHandler<Q, R> handler) {
        handlers.put(queryType, handler);
    }

    /**
     * Dispatches a query to its registered handler.
     *
     * @param query The query to dispatch
     * @param <Q>   The query type
     * @param <R>   The result type
     * @return The result of the query
     * @throws IllegalArgumentException if no handler is registered for the query type
     */
    @SuppressWarnings("unchecked")
    public <Q extends Query<R>, R> R dispatch(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) handlers.get(query.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for query type: " + query.getClass().getName());
        }
        return handler.handle(query);
    }
}

