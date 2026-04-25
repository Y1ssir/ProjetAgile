package com.eventdriven.subscribers;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.util.Map;

/**
 * SUBSCRIBER — NotifHandler
 */
public class NotifHandler {
    private static final NotifHandler INSTANCE = new NotifHandler();
    private final EventBus bus = EventBus.getInstance();

    private NotifHandler() {}

    public static NotifHandler getInstance() {
        return INSTANCE;
    }

    public void register() {
        bus.subscribe(Topics.USER_CREATED, this::onUserCreated);
        bus.subscribe(Topics.ORDER_SHIPPED, this::onOrderShipped);
        bus.subscribe(Topics.PAYMENT_DONE, this::onPaymentDone);
        bus.subscribe(Topics.PAYMENT_FAILED, this::onPaymentFailed);
        bus.subscribe(Topics.ORDER_CANCELLED, this::onOrderCancelled);

        System.out.println("[NotifHandler] ✅ Abonné à 5 topics");
    }

    private void onUserCreated(Event event) {
        Map<String, Object> p = event.payload();
        send((String) p.get("userId"), "🎉 Bienvenue !", "Bonjour " + p.get("name") + ", votre compte est créé avec succès.");
    }

    private void onOrderShipped(Event event) {
        Map<String, Object> p = event.payload();
        send((String) p.get("userId"),
             "📦 Votre colis est en route !",
             "Commande " + p.get("orderId") + " expédiée via " + p.get("carrier") + ". Tracking : " + p.get("trackingCode"));
    }

    private void onPaymentDone(Event event) {
        Map<String, Object> p = event.payload();
        double amount = ((Number) p.get("amount")).doubleValue();
        if (amount > 0) {
            send((String) p.get("userId"),
                 "✅ Paiement confirmé",
                 "Votre paiement de " + amount + " € pour la commande " + p.get("orderId") + " a été accepté.");
        } else {
            send((String) p.get("userId"),
                 "💸 Remboursement effectué",
                 "Votre remboursement de " + Math.abs(amount) + " € est en cours.");
        }
    }

    private void onPaymentFailed(Event event) {
        Map<String, Object> p = event.payload();
        send((String) p.get("userId"),
             "❌ Paiement refusé",
             "Votre paiement de " + p.get("amount") + " € a été refusé : " + p.get("reason") + ". Veuillez réessayer.");
    }

    private void onOrderCancelled(Event event) {
        Map<String, Object> p = event.payload();
        send((String) p.get("userId"),
             "🚫 Commande annulée",
             "Votre commande " + p.get("orderId") + " a été annulée. Raison : " + p.get("reason"));
    }

    private void send(String userId, String title, String message) {
        System.out.println("[NotifHandler] 🔔 Push → user:" + userId);
        System.out.println("               Titre   : " + title);
        System.out.println("               Message : " + message);
    }
}
