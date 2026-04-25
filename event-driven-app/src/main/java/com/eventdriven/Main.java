package com.eventdriven;

import com.eventdriven.subscribers.AnalyticsHandler;
import com.eventdriven.subscribers.EmailService;
import com.eventdriven.subscribers.NotifHandler;
import com.eventdriven.utils.EventLogger;
import com.eventdriven.web.ApiServer;

import java.io.IOException;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║                     MAIN.JAVA                        ║
 * ║         Point d'entrée de l'API Event-Driven         ║
 * ╚══════════════════════════════════════════════════════╝
 */
public class Main {
    public static void main(String[] args) throws IOException {
        EventLogger eventLogger = EventLogger.getInstance();
        EmailService emailService = EmailService.getInstance();
        AnalyticsHandler analyticsHandler = AnalyticsHandler.getInstance();
        NotifHandler notifHandler = NotifHandler.getInstance();

        System.out.println("🚀 Lancement du système event-driven...\n");
        System.out.println("── Enregistrement des subscribers ──────────────");

        eventLogger.register();       // Logger global
        emailService.register();      // Emails
        analyticsHandler.register();  // Statistiques
        notifHandler.register();      // Notifications
        
        // Nouveaux services autonomes
        com.eventdriven.publishers.InventoryService.getInstance().register();
        com.eventdriven.publishers.OrderService.getInstance().register();
        com.eventdriven.publishers.PaymentService.getInstance().register();
        com.eventdriven.publishers.DeliveryService.getInstance().register();

        System.out.println("────────────────────────────────────────────────\n");

        ApiServer.start();
    }
}
