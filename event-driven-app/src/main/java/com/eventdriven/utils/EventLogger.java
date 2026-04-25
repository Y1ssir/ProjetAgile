package com.eventdriven.utils;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UTILS — EventLogger
 */
public class EventLogger {
    private static final EventLogger INSTANCE = new EventLogger();
    private final EventBus bus = EventBus.getInstance();
    private final List<Event> log = Collections.synchronizedList(new ArrayList<>());

    private EventLogger() {}

    public static EventLogger getInstance() {
        return INSTANCE;
    }

    public void register() {
        String[] allTopics = {
            Topics.USER_CREATED, Topics.USER_DELETED, Topics.USER_UPDATED,
            Topics.ORDER_PLACED, Topics.ORDER_OUT_OF_STOCK, Topics.INVENTORY_RESERVED,
            Topics.PAYMENT_SUBMITTED, Topics.PAYMENT_DONE, Topics.PAYMENT_FAILED, 
            Topics.ORDER_SHIPPED, Topics.ORDER_CANCELLED,
            Topics.DELIVERY_COMPLETED
        };

        for (String topic : allTopics) {
            bus.subscribe(topic, this::logEvent);
        }

        System.out.println("[EventLogger] ✅ Journal actif sur " + allTopics.length + " topics\n");
    }

    private void logEvent(Event event) {
        log.add(event);
        // On simplifie l'affichage de l'heure
        System.out.printf("[EventLogger] 📝 [#%d] → %s%n", event.sequence(), event.topic());
    }

    public List<Event> getLog() {
        return new ArrayList<>(log);
    }

    public void printHistory() {
        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("          JOURNAL COMPLET DES ÉVÉNEMENTS       ");
        System.out.println("══════════════════════════════════════════════");

        if (log.isEmpty()) {
            System.out.println("  Aucun événement enregistré.");
        } else {
            int i = 1;
            for (Event e : log) {
                System.out.printf("  %02d. %-20s ID: %s%n", i++, e.topic(), e.eventId());
            }
        }

        System.out.println("══════════════════════════════════════════════");
        System.out.println("  Total : " + log.size() + " événement(s) traité(s)\n");
    }
}
