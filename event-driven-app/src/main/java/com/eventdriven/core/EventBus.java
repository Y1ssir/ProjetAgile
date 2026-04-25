package com.eventdriven.core;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║              EVENT BUS — Cœur de l'EDA              ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * Le bus est le seul composant partagé entre publishers et subscribers.
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    private final Map<String, List<Consumer<Event>>> subscribers = new ConcurrentHashMap<>();
    private final List<Event> history = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong count = new AtomicLong(0);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private EventBus() {}

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void subscribe(String topic, Consumer<Event> handler) {
        if (handler == null) throw new IllegalArgumentException("Handler must not be null");
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public void unsubscribe(String topic, Consumer<Event> handler) {
        List<Consumer<Event>> handlers = subscribers.get(topic);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    public Event publish(String topic, Map<String, Object> payload) {
        long sequence = count.incrementAndGet();
        String eventId = "evt_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 5);
        String timestamp = Instant.now().toString();

        Event event = new Event(eventId, topic, payload != null ? payload : new HashMap<>(), timestamp, sequence);
        history.add(event);

        List<Consumer<Event>> handlers = subscribers.getOrDefault(topic, Collections.emptyList());
        for (Consumer<Event> handler : handlers) {
            executor.submit(() -> {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    System.err.println("Err: " + topic + " - " + e.getMessage());
                }
            });
        }
        return event;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
