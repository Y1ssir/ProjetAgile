package com.eventdriven.publishers;

import com.eventdriven.core.Event;
import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PUBLISHER & SUBSCRIBER — InventoryService
 * Vérifie le stock avant de valider la commande vers le paiement.
 */
public class InventoryService {
    private static final InventoryService INSTANCE = new InventoryService();
    private final EventBus bus = EventBus.getInstance();
    
    // Le Plat du Jour n'a que 2 unités en stock !
    private final AtomicInteger platDuJourStock = new AtomicInteger(2);

    private InventoryService() {}

    public static InventoryService getInstance() {
        return INSTANCE;
    }

    public void register() {
        bus.subscribe(Topics.ORDER_PLACED, this::onOrderPlaced);
        System.out.println("[InventoryService] ✅ Abonné à ORDER_PLACED (Vérification Stock)");
    }

    private void onOrderPlaced(Event event) {
        Map<String, Object> p = event.payload();
        List<Map<String, Object>> items = (List<Map<String, Object>>) p.get("items");
        
        boolean hasPlatDuJour = false;
        int qtyRequested = 0;
        
        if (items != null) {
            for (Map<String, Object> item : items) {
                if ("Plat du Jour".equals(item.get("name"))) {
                    hasPlatDuJour = true;
                    qtyRequested += ((Number) item.get("qty")).intValue();
                }
            }
        }

        if (hasPlatDuJour) {
            // Simulation temps de vérification stock
            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            int currentStock = platDuJourStock.get();
            if (currentStock >= qtyRequested) {
                platDuJourStock.addAndGet(-qtyRequested);
                System.out.println("[InventoryService] Stock OK. Reste : " + platDuJourStock.get());
                bus.publish(Topics.INVENTORY_RESERVED, p);
            } else {
                System.out.println("[InventoryService] ⚠️ RUPTURE DE STOCK ! Commande annulée.");
                p.put("reason", "Rupture de stock pour le Plat du Jour");
                bus.publish(Topics.ORDER_OUT_OF_STOCK, p);
            }
        } else {
            // Simulation temps de vérification stock
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            // Pas de plat du jour, on valide directement l'inventaire
            bus.publish(Topics.INVENTORY_RESERVED, p);
        }
    }
}
