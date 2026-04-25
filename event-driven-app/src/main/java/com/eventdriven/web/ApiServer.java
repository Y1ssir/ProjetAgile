package com.eventdriven.web;

import com.eventdriven.ScenarioRunner;
import com.eventdriven.core.Event;
import com.eventdriven.subscribers.AnalyticsHandler;
import com.eventdriven.utils.EventLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.eventdriven.publishers.OrderService;
import com.eventdriven.publishers.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiServer {
    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/events", new EventsHandler());
        server.createContext("/api/scenario/1", new ScenarioHandler(1));
        server.createContext("/api/scenario/2", new ScenarioHandler(2));
        server.createContext("/api/scenario/3", new ScenarioHandler(3));
        server.createContext("/api/checkout", new CheckoutHandler());
        server.createContext("/api/payment", new PaymentHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("🌍 Serveur API démarré sur http://localhost:8080");
    }

    private static void sendCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            AnalyticsHandler analytics = AnalyticsHandler.getInstance();
            String json = String.format(
                "{\"users\": %d, \"orders\": %d, \"shipped\": %d, \"revenue\": %.2f, \"failed\": %d}",
                analytics.getTotalUsers(),
                analytics.getTotalOrders(),
                analytics.getTotalShipped(),
                analytics.getTotalRevenue(),
                analytics.getFailedPayments()
            );

            byte[] response = json.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    static class EventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Event> log = EventLogger.getInstance().getLog();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < log.size(); i++) {
                Event e = log.get(i);
                String orderId = (e.payload().containsKey("orderId")) ? (String) e.payload().get("orderId") : "";
                
                String itemsJson = "[]";
                if (Topics.ORDER_PLACED.equals(e.topic()) && e.payload().containsKey("items")) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) e.payload().get("items");
                    StringBuilder itemsBuilder = new StringBuilder("[");
                    for (int j = 0; j < items.size(); j++) {
                        Map<String, Object> item = items.get(j);
                        itemsBuilder.append(String.format("{\"name\":\"%s\",\"qty\":%d,\"price\":%s}", 
                                item.get("name").toString().replace("\"", "\\\""), 
                                item.get("qty"), 
                                item.get("price")));
                        if (j < items.size() - 1) itemsBuilder.append(",");
                    }
                    itemsBuilder.append("]");
                    itemsJson = itemsBuilder.toString();
                }
                
                sb.append(String.format("{\"id\": \"%s\", \"topic\": \"%s\", \"timestamp\": \"%s\", \"sequence\": %d, \"orderId\": \"%s\", \"items\": %s}",
                        e.eventId(), e.topic(), e.timestamp(), e.sequence(), orderId, itemsJson));
                if (i < log.size() - 1) sb.append(",");
            }
            sb.append("]");

            byte[] response = sb.toString().getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    static class ScenarioHandler implements HttpHandler {
        private final int scenario;
        public ScenarioHandler(int scenario) { this.scenario = scenario; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                if (scenario == 1) ScenarioRunner.runScenario1();
                else if (scenario == 2) ScenarioRunner.runScenario2();
                else if (scenario == 3) ScenarioRunner.runScenario3();

                String response = "{\"status\": \"ok\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class CheckoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                double total = 25.0;
                int platDuJourQty = 0;
                if (query != null) {
                    try {
                        if (query.contains("total=")) {
                            String totalStr = query.split("total=")[1].split("&")[0];
                            total = Double.parseDouble(totalStr);
                        }
                        if (query.contains("platDuJourQty=")) {
                            String pStr = query.split("platDuJourQty=")[1].split("&")[0];
                            platDuJourQty = Integer.parseInt(pStr);
                        }
                    } catch (Exception e) {}
                }

                UserService userService = UserService.getInstance();
                OrderService orderService = OrderService.getInstance();
                
                Map<String, Object> user = userService.createUser("Client Web", "client@web.com");
                
                List<Map<String, Object>> items = new ArrayList<>();
                if (platDuJourQty > 0) {
                    Map<String, Object> itemPlat = new HashMap<>();
                    itemPlat.put("name", "Plat du Jour"); 
                    itemPlat.put("price", 18.0); 
                    itemPlat.put("qty", platDuJourQty);
                    items.add(itemPlat);
                }
                
                Map<String, Object> item1 = new HashMap<>();
                item1.put("name", "Commande standard"); 
                item1.put("price", total); 
                item1.put("qty", 1);
                items.add(item1);
                
                orderService.placeOrder((String) user.get("userId"), items);

                String response = "{\"status\": \"ok\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class PaymentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String orderId = "";
                String action = "";
                if (query != null) {
                    try {
                        if (query.contains("orderId=")) {
                            orderId = query.split("orderId=")[1].split("&")[0];
                        }
                        if (query.contains("action=")) {
                            action = query.split("action=")[1].split("&")[0];
                        }
                    } catch (Exception e) {}
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("orderId", orderId);
                payload.put("action", action);
                com.eventdriven.core.EventBus.getInstance().publish(com.eventdriven.core.Topics.PAYMENT_SUBMITTED, payload);

                String response = "{\"status\": \"submitted\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}
