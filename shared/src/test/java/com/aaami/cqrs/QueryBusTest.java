package com.aaami.cqrs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QueryBusTest {

    private QueryBus queryBus;
    
    // Create a test query class
    static class TestQuery implements Query<String> {
        private final Long id;
        
        public TestQuery(Long id) {
            this.id = id;
        }
        
        public Long getId() {
            return id;
        }
    }

    @BeforeEach
    void setUp() {
        queryBus = new QueryBus();
    }

    @Test
    void registerHandler_ShouldRegisterHandler() {
        // Given
        QueryHandler<TestQuery, String> handler = mock(QueryHandler.class);

        // When
        queryBus.registerHandler(TestQuery.class, handler);

        // Then - No exception thrown
        assertDoesNotThrow(() -> queryBus.registerHandler(TestQuery.class, handler));
    }

    @Test
    void dispatch_ShouldCallHandler_WhenHandlerIsRegistered() {
        // Given
        TestQuery query = new TestQuery(1L);
        String expectedResult = "test result";
        QueryHandler<TestQuery, String> handler = mock(QueryHandler.class);
        
        when(handler.handle(query)).thenReturn(expectedResult);
        queryBus.registerHandler(TestQuery.class, handler);

        // When
        String result = queryBus.dispatch(query);

        // Then
        assertEquals(expectedResult, result);
        verify(handler).handle(query);
    }

    @Test
    void dispatch_ShouldThrowException_WhenHandlerNotRegistered() {
        // Given
        TestQuery query = new TestQuery(1L);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> queryBus.dispatch(query));
        assertTrue(exception.getMessage().contains("No handler registered"));
    }
}

