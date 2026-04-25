package com.eventdriven;

import com.eventdriven.publishers.OrderService;
import com.eventdriven.publishers.PaymentService;
import com.eventdriven.publishers.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioRunner {

    public static void runScenario1() {
        UserService userService = UserService.getInstance();
        OrderService orderService = OrderService.getInstance();
        PaymentService paymentService = PaymentService.getInstance();

        Map<String, Object> user = userService.createUser("Faress Benali", "faress@example.com");

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "Clavier mécanique"); item1.put("price", 120.0); item1.put("qty", 1);
        items.add(item1);
        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "Souris gaming"); item2.put("price", 80.0); item2.put("qty", 2);
        items.add(item2);

        Map<String, Object> order = orderService.placeOrder((String) user.get("userId"), items);
        paymentService.processPayment((String) order.get("orderId"), (String) user.get("userId"), (Double) order.get("total"), "carte_bancaire");
        orderService.shipOrder((String) order.get("orderId"), (String) user.get("userId"), "Colissimo");
    }

    public static void runScenario2() {
        UserService userService = UserService.getInstance();
        OrderService orderService = OrderService.getInstance();
        PaymentService paymentService = PaymentService.getInstance();

        Map<String, Object> user2 = userService.createUser("Sara Moussaoui", "sara@example.com");

        List<Map<String, Object>> items2 = new ArrayList<>();
        Map<String, Object> item3 = new HashMap<>();
        item3.put("name", "Laptop pro"); item3.put("price", 1500.0); item3.put("qty", 1);
        items2.add(item3);

        Map<String, Object> order2 = orderService.placeOrder((String) user2.get("userId"), items2);
        paymentService.processPayment((String) order2.get("orderId"), (String) user2.get("userId"), (Double) order2.get("total"), "carte_bancaire");
    }

    public static void runScenario3() {
        UserService userService = UserService.getInstance();
        OrderService orderService = OrderService.getInstance();

        Map<String, Object> user = userService.createUser("Faress Benali", "faress@example.com");

        List<Map<String, Object>> items3 = new ArrayList<>();
        Map<String, Object> item4 = new HashMap<>();
        item4.put("name", "Webcam HD"); item4.put("price", 60.0); item4.put("qty", 1);
        items3.add(item4);

        Map<String, Object> order3 = orderService.placeOrder((String) user.get("userId"), items3);
        orderService.cancelOrder((String) order3.get("orderId"), (String) user.get("userId"), "Client a changé d'avis");
    }
}
