package com.aaami.shared.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events.
 * Provides common fields like event ID, timestamp, and event type.
 */
public abstract class BaseEvent {
    private String eventId;
    private LocalDateTime timestamp;
    private String eventType;

    protected BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    protected BaseEvent(String eventId, LocalDateTime timestamp) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}

