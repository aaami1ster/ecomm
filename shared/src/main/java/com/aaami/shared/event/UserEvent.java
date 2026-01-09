package com.aaami.shared.event;

import com.aaami.shared.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Base class for user-related events.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class UserEvent extends BaseEvent {
    private UserDto user;

    protected UserEvent(UserDto user) {
        super();
        this.user = user;
    }

    protected UserEvent(String eventId, java.time.LocalDateTime timestamp, UserDto user) {
        super(eventId, timestamp);
        this.user = user;
    }
}

