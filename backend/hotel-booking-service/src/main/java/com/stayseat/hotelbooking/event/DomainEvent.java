package com.stayseat.hotelbooking.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DomainEvent(String eventId, String eventType, OffsetDateTime occurredAt, Object payload) {

    public static DomainEvent of(String eventType, Object payload) {
        return new DomainEvent(UUID.randomUUID().toString(), eventType, OffsetDateTime.now(), payload);
    }
}
