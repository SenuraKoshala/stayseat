package com.stayseat.paymentservice.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Wire format from API_CONTRACT.md §3.9:
 * { "eventId", "eventType", "occurredAt", "payload" }
 * This is the exact shape notification-service's EventEnvelope&lt;T&gt; expects
 * to deserialize, so field names/types must stay in sync with that class.
 */
public record DomainEvent(UUID eventId, String eventType, OffsetDateTime occurredAt, Object payload) {

    public static DomainEvent of(String eventType, Object payload) {
        return new DomainEvent(UUID.randomUUID(), eventType, OffsetDateTime.now(), payload);
    }
}
