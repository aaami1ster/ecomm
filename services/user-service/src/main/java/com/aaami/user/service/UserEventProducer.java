package com.aaami.user.service;

import com.aaami.shared.dto.UserDto;
import com.aaami.shared.event.UserCreatedEvent;
import com.aaami.shared.event.UserDeletedEvent;
import com.aaami.shared.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing user-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.user-created:user-created}")
    private String userCreatedTopic;

    @Value("${kafka.topics.user-updated:user-updated}")
    private String userUpdatedTopic;

    @Value("${kafka.topics.user-deleted:user-deleted}")
    private String userDeletedTopic;

    public void publishUserCreated(UserDto user) {
        UserCreatedEvent event = new UserCreatedEvent(user);
        sendEvent(userCreatedTopic, user.getId().toString(), event);
        log.info("Published UserCreatedEvent for user ID: {}", user.getId());
    }

    public void publishUserUpdated(UserDto user) {
        UserUpdatedEvent event = new UserUpdatedEvent(user);
        sendEvent(userUpdatedTopic, user.getId().toString(), event);
        log.info("Published UserUpdatedEvent for user ID: {}", user.getId());
    }

    public void publishUserDeleted(UserDto user) {
        UserDeletedEvent event = new UserDeletedEvent(user);
        sendEvent(userDeletedTopic, user.getId().toString(), event);
        log.info("Published UserDeletedEvent for user ID: {}", user.getId());
    }

    private void sendEvent(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Successfully sent event to topic {} with offset {}", topic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send event to topic {}", topic, ex);
            }
        });
    }
}

