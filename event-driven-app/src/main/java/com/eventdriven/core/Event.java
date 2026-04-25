package com.eventdriven.core;

import java.util.Map;

/**
 * Représente un événement transitant sur le bus d'événements.
 */
public record Event(
    String eventId,
    String topic,
    Map<String, Object> payload,
    String timestamp,
    long sequence
) {}
