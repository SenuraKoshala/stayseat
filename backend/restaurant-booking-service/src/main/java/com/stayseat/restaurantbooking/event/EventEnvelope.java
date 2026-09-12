package com.stayseat.restaurantbooking.event;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class EventEnvelope<T> {
    private UUID eventId;
    private String eventType;
    private Instant occurredAt;
    private T payload;
}
