package com.eventdriven.core;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║           ÉVÉNEMENTS DU DOMAINE (Catalogue)          ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * Ce fichier est le CONTRAT de l'architecture.
 * Il définit tous les topics possibles.
 */
public final class Topics {
    private Topics() {} // Prevent instantiation

    // Domaine : Utilisateurs
    public static final String USER_CREATED = "user.created";
    public static final String USER_DELETED = "user.deleted";
    public static final String USER_UPDATED = "user.updated";

    // Domaine : Commandes
    public static final String ORDER_PLACED = "order.placed";
    public static final String ORDER_SHIPPED = "order.shipped";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String ORDER_OUT_OF_STOCK = "order.out_of_stock";

    // Domaine : Inventaire
    public static final String INVENTORY_RESERVED = "inventory.reserved";

    // Domaine : Paiements
    public static final String PAYMENT_SUBMITTED = "payment.submitted";
    public static final String PAYMENT_DONE = "payment.done";
    public static final String PAYMENT_FAILED = "payment.failed";

    // Domaine : Livraison
    public static final String DELIVERY_COMPLETED = "delivery.completed";
}
