package com.aaami.shared.event;

import com.aaami.shared.dto.UserDto;

/**
 * Event published when a user is deleted (soft delete).
 */
public class UserDeletedEvent extends UserEvent {
    public UserDeletedEvent() {
        super();
    }

    public UserDeletedEvent(UserDto user) {
        super(user);
    }

    public UserDeletedEvent(String eventId, java.time.LocalDateTime timestamp, UserDto user) {
        super(eventId, timestamp, user);
    }
}

