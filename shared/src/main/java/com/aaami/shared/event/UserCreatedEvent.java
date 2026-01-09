package com.aaami.shared.event;

import com.aaami.shared.dto.UserDto;

/**
 * Event published when a new user is created.
 */
public class UserCreatedEvent extends UserEvent {
    public UserCreatedEvent() {
        super();
    }

    public UserCreatedEvent(UserDto user) {
        super(user);
    }

    public UserCreatedEvent(String eventId, java.time.LocalDateTime timestamp, UserDto user) {
        super(eventId, timestamp, user);
    }
}

