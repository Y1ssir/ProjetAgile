package com.eventdriven.subscribers;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.util.Map;

/**
 * SUBSCRIBER — EmailService
 */
public class EmailService {
    private static final EmailService INSTANCE = new EmailService();
    private final EventBus bus = EventBus.getInstance();

    private EmailService() {}

    public static EmailService getInstance() {
        return INSTANCE;
    }

    public void register() {
        bus.subscribe(Topics.USER_CREATED, this::onUserCreated);
        bus.subscribe(Topics.USER_DELETED, this::onUserDeleted);
        bus.subscribe(Topics.ORDER_PLACED, this::onOrderPlaced);
        bus.subscribe(Topics.ORDER_SHIPPED, this::onOrderShipped);
        bus.subscribe(Topics.ORDER_CANCELLED, this::onOrderCancelled);
        bus.subscribe(Topics.PAYMENT_DONE, this::onPaymentDone);
        bus.subscribe(Topics.PAYMENT_FAILED, this::onPaymentFailed);

        System.out.println("[EmailService] ✅ Abonné à 7 topics");
    }

    private void onUserCreated(Event event) {
        Map<String, Object> p = event.payload();
        send((String) p.get("email"), "Bienvenue !", "Bonjour " + p.get("name") + ", bienvenue sur notre plateforme !");
    }

    private void onUserDeleted(Event event) {
        Map<String, Object> p = event.payload();
        send((String) p.get("email"), "Compte supprimé", "Votre compte a été supprimé. Raison : " + p.get("reason"));
    }

    private void onOrderPlaced(Event event) {
        Map<String, Object> p = event.payload();
        send("user_" + p.get("userId") + "@shop.com",
             "Confirmation commande " + p.get("orderId"),
             "Votre commande de " + p.get("total") + " € a bien été reçue. Merci !");
    }

    private void onOrderShipped(Event event) {
        Map<String, Object> p = event.payload();
        send("user_" + p.get("userId") + "@shop.com",
             "📦 Votre commande " + p.get("orderId") + " est expédiée",
             "Transporteur : " + p.get("carrier") + " | Numéro de suivi : " + p.get("trackingCode"));
    }

    private void onOrderCancelled(Event event) {
        Map<String, Object> p = event.payload();
        send("user_" + p.get("userId") + "@shop.com",
             "Commande " + p.get("orderId") + " annulée",
             "Votre commande a été annulée. Raison : " + p.get("reason"));
    }

    private void onPaymentDone(Event event) {
        Map<String, Object> p = event.payload();
        double amount = ((Number) p.get("amount")).doubleValue();
        if (amount > 0) {
            send("user_" + p.get("userId") + "@shop.com",
                 "Reçu de paiement",
                 "Votre paiement de " + amount + " € (" + p.get("method") + ") a été accepté. Merci !");
        }
    }

    private void onPaymentFailed(Event event) {
        Map<String, Object> p = event.payload();
        send("user_" + p.get("userId") + "@shop.com",
             "⚠️ Paiement refusé",
             "Votre paiement de " + p.get("amount") + " € a échoué : " + p.get("reason") + ". Veuillez réessayer.");
    }

    private void send(String to, String subject, String body) {
        System.out.println("[Email Service] 📧 → " + to);
        System.out.println("               Sujet : " + subject);
        System.out.println("               Corps : " + body);
    }
}
