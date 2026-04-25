package com.eventdriven.publishers;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * PUBLISHER — PaymentService
 */
public class PaymentService {
    private static final PaymentService INSTANCE = new PaymentService();
    private final EventBus bus = EventBus.getInstance();

    private final Map<String, Map<String, Object>> pendingPayments = new HashMap<>();

    private PaymentService() {}

    public static PaymentService getInstance() {
        return INSTANCE;
    }

    public void register() {
        bus.subscribe(Topics.INVENTORY_RESERVED, this::onInventoryReserved);
        bus.subscribe(Topics.PAYMENT_SUBMITTED, this::onPaymentSubmitted);
        System.out.println("[PaymentService] ✅ Abonné à INVENTORY_RESERVED et PAYMENT_SUBMITTED (Paiement Manuel)");
    }

    private void onInventoryReserved(Event event) {
        Map<String, Object> p = event.payload();
        String orderId = (String) p.get("orderId");
        
        System.out.println("[PaymentService] Commande " + orderId + " en attente de validation de paiement...");
        pendingPayments.put(orderId, p);
    }

    private void onPaymentSubmitted(Event event) {
        Map<String, Object> submission = event.payload();
        String orderId = (String) submission.get("orderId");
        String action = (String) submission.get("action");
        
        Map<String, Object> p = pendingPayments.get(orderId);
        if (p == null) {
            System.out.println("[PaymentService] Erreur: Commande " + orderId + " introuvable pour le paiement.");
            return;
        }
        
        pendingPayments.remove(orderId);

        String userId = (String) p.get("userId");
        double amount = ((Number) p.get("total")).doubleValue();
        
        // Simulation traitement bancaire 1 sec
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        if ("approve".equals(action)) {
            processPayment(orderId, userId, amount, "carte_bancaire");
        } else {
            // Rejet manuel
            String paymentId = "pay_" + System.currentTimeMillis();
            Map<String, Object> payload = new HashMap<>();
            payload.put("paymentId", paymentId);
            payload.put("orderId", orderId);
            payload.put("userId", userId);
            payload.put("amount", amount);
            payload.put("reason", "Paiement annulé par l'utilisateur");
            payload.put("failedAt", Instant.now().toString());
            System.out.println("[PaymentService] ❌ Paiement refusé manuellement → " + paymentId);
            bus.publish(Topics.PAYMENT_FAILED, payload);
        }
    }


    public Map<String, Object> processPayment(String orderId, String userId, double amount, String method) {
        if (method == null) method = "carte_bancaire";
        String paymentId = "pay_" + System.currentTimeMillis();

        System.out.println("[PaymentService] Traitement paiement → " + paymentId + " (" + amount + " €)");

        boolean success = amount <= 1000;
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();

        payload.put("paymentId", paymentId);
        payload.put("orderId", orderId);
        payload.put("userId", userId);
        payload.put("amount", amount);
        payload.put("method", method);

        if (success) {
            payload.put("paidAt", Instant.now().toString());
            bus.publish(Topics.PAYMENT_DONE, payload);

            result.put("success", true);
            result.put("paymentId", paymentId);
        } else {
            String reason = "Montant dépasse la limite autorisée";
            payload.put("reason", reason);
            payload.put("failedAt", Instant.now().toString());
            bus.publish(Topics.PAYMENT_FAILED, payload);

            result.put("success", false);
            result.put("paymentId", paymentId);
            result.put("reason", reason);
        }
        return result;
    }

    public void refundPayment(String paymentId, String orderId, String userId, double amount) {
        System.out.println("[PaymentService] Remboursement → " + paymentId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", "ref_" + paymentId);
        payload.put("orderId", orderId);
        payload.put("userId", userId);
        payload.put("amount", -amount);
        payload.put("method", "remboursement");
        payload.put("paidAt", Instant.now().toString());

        bus.publish(Topics.PAYMENT_DONE, payload);
    }
}
