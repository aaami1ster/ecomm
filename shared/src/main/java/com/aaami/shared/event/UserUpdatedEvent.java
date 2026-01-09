package com.aaami.shared.event;

import com.aaami.shared.dto.UserDto;

/**
 * Event published when a user is updated.
 */
public class UserUpdatedEvent extends UserEvent {
    public UserUpdatedEvent() {
        super();
    }

    public UserUpdatedEvent(UserDto user) {
        super(user);
    }

    public UserUpdatedEvent(String eventId, java.time.LocalDateTime timestamp, UserDto user) {
        super(eventId, timestamp, user);
    }
}

