package com.eventdriven.publishers;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PUBLISHER — OrderService
 */
public class OrderService {
    private static final OrderService INSTANCE = new OrderService();
    private final EventBus bus = EventBus.getInstance();

    private OrderService() {}

    public static OrderService getInstance() {
        return INSTANCE;
    }

    public void register() {
        bus.subscribe(Topics.PAYMENT_DONE, this::onPaymentDone);
        System.out.println("[OrderService] ✅ Abonné à PAYMENT_DONE (Expédition Auto)");
    }

    private void onPaymentDone(Event event) {
        Map<String, Object> p = event.payload();
        // Simulation temps d'emballage 3 secondes
        try { Thread.sleep(3000); } catch (InterruptedException e) {}
        shipOrder((String) p.get("orderId"), (String) p.get("userId"), "Deliveroo");
    }

    public Map<String, Object> placeOrder(String userId, List<Map<String, Object>> items) {
        String orderId = "ord_" + System.currentTimeMillis();
        
        double total = 0;
        for (Map<String, Object> item : items) {
            double price = ((Number) item.get("price")).doubleValue();
            int qty = ((Number) item.get("qty")).intValue();
            total += price * qty;
        }

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("items", items);
        order.put("total", total);
        order.put("placedAt", Instant.now().toString());
        order.put("status", "placed");

        System.out.println("[OrderService] Commande passée → " + orderId + " (" + total + " €)");
        bus.publish(Topics.ORDER_PLACED, order);
        return order;
    }

    public String shipOrder(String orderId, String userId, String carrier) {
        if (carrier == null) carrier = "Colissimo";
        String trackingCode = "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        System.out.println("[OrderService] Commande expédiée → " + orderId + " / tracking: " + trackingCode);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        payload.put("userId", userId);
        payload.put("trackingCode", trackingCode);
        payload.put("carrier", carrier);
        payload.put("shippedAt", Instant.now().toString());
        
        bus.publish(Topics.ORDER_SHIPPED, payload);

        return trackingCode;
    }

    public void cancelOrder(String orderId, String userId, String reason) {
        System.out.println("[OrderService] Commande annulée → " + orderId);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        payload.put("userId", userId);
        payload.put("reason", reason);
        payload.put("cancelledAt", Instant.now().toString());
        
        bus.publish(Topics.ORDER_CANCELLED, payload);
    }
}
