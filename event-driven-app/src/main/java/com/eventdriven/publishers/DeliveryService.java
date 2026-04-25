package com.eventdriven.publishers;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PUBLISHER & SUBSCRIBER — DeliveryService
 * S'abonne à ORDER_SHIPPED et livre après un délai.
 */
public class DeliveryService {
    private static final DeliveryService INSTANCE = new DeliveryService();
    private final EventBus bus = EventBus.getInstance();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private DeliveryService() {}

    public static DeliveryService getInstance() {
        return INSTANCE;
    }

    public void register() {
        bus.subscribe(Topics.ORDER_SHIPPED, this::onOrderShipped);
        System.out.println("[DeliveryService] ✅ Abonné à ORDER_SHIPPED");
    }

    private void onOrderShipped(Event event) {
        Map<String, Object> p = event.payload();
        String orderId = (String) p.get("orderId");
        String userId = (String) p.get("userId");

        System.out.println("[DeliveryService] Prise en charge de la livraison pour → " + orderId);

        // Simuler un délai de livraison de 5 secondes
        scheduler.schedule(() -> {
            System.out.println("[DeliveryService] Commande livrée → " + orderId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", orderId);
            payload.put("userId", userId);
            payload.put("deliveredAt", Instant.now().toString());

            bus.publish(Topics.DELIVERY_COMPLETED, payload);
        }, 5, TimeUnit.SECONDS);
    }
}
