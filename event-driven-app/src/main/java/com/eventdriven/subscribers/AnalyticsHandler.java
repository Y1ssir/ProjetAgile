package com.eventdriven.subscribers;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SUBSCRIBER — AnalyticsHandler
 */
public class AnalyticsHandler {
    private static final AnalyticsHandler INSTANCE = new AnalyticsHandler();
    private final EventBus bus = EventBus.getInstance();

    private final AtomicInteger totalUsers = new AtomicInteger(0);
    private final AtomicInteger totalOrders = new AtomicInteger(0);
    private final AtomicInteger totalShipped = new AtomicInteger(0);
    private final AtomicInteger failedPayments = new AtomicInteger(0);
    // Double requires custom synchronization if updated concurrently
    private final AtomicReference<Double> totalRevenue = new AtomicReference<>(0.0);

    private AnalyticsHandler() {}

    public static AnalyticsHandler getInstance() {
        return INSTANCE;
    }

    public void register() {
        bus.subscribe(Topics.USER_CREATED, this::onUserCreated);
        bus.subscribe(Topics.ORDER_PLACED, this::onOrderPlaced);
        bus.subscribe(Topics.ORDER_SHIPPED, this::onOrderShipped);
        bus.subscribe(Topics.PAYMENT_DONE, this::onPaymentDone);
        bus.subscribe(Topics.PAYMENT_FAILED, this::onPaymentFailed);

        System.out.println("[AnalyticsHandler] ✅ Abonné à 5 topics");
    }

    private void onUserCreated(Event event) {
        int users = totalUsers.incrementAndGet();
        System.out.println("[Analytics] 📊 Nouvel inscrit : " + event.payload().get("name") + " | Total users : " + users);
    }

    private void onOrderPlaced(Event event) {
        int orders = totalOrders.incrementAndGet();
        System.out.println("[Analytics] 📊 Commande enregistrée : " + event.payload().get("orderId") + " | Total commandes : " + orders);
    }

    private void onOrderShipped(Event event) {
        int shipped = totalShipped.incrementAndGet();
        System.out.println("[Analytics] 📊 Livraison suivie : " + event.payload().get("trackingCode") + " | Total expédiés : " + shipped);
    }

    private void onPaymentDone(Event event) {
        double amount = ((Number) event.payload().get("amount")).doubleValue();
        if (amount > 0) {
            double currentRevenue = totalRevenue.updateAndGet(v -> v + amount);
            System.out.println("[Analytics] 📊 Revenu +" + amount + " € | Chiffre d'affaires total : " + currentRevenue + " €");
        }
    }

    private void onPaymentFailed(Event event) {
        int fails = failedPayments.incrementAndGet();
        System.out.println("[Analytics] ⚠️  Paiement échoué : " + event.payload().get("reason") + " | Total échecs : " + fails);
    }

    public int getTotalUsers() { return totalUsers.get(); }
    public int getTotalOrders() { return totalOrders.get(); }
    public int getTotalShipped() { return totalShipped.get(); }
    public double getTotalRevenue() { return totalRevenue.get(); }
    public int getFailedPayments() { return failedPayments.get(); }

    public void printReport() {
        System.out.println("\n══════════════════════════════════");
        System.out.println("       RAPPORT ANALYTIQUE          ");
        System.out.println("══════════════════════════════════");
        System.out.println("👤 Utilisateurs inscrits : " + totalUsers.get());
        System.out.println("🛒 Commandes passées     : " + totalOrders.get());
        System.out.println("📦 Commandes expédiées   : " + totalShipped.get());
        System.out.println("💰 Chiffre d'affaires    : " + totalRevenue.get() + " €");
        System.out.println("❌ Paiements échoués     : " + failedPayments.get());
        System.out.println("══════════════════════════════════\n");
    }
}
