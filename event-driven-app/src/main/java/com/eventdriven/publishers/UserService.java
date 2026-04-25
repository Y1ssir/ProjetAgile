package com.eventdriven.publishers;

import com.eventdriven.core.EventBus;
import com.eventdriven.core.Topics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * PUBLISHER — UserService
 */
public class UserService {
    private static final UserService INSTANCE = new UserService();
    private final EventBus bus = EventBus.getInstance();

    private UserService() {}

    public static UserService getInstance() {
        return INSTANCE;
    }

    public Map<String, Object> createUser(String name, String email) {
        String userId = "usr_" + System.currentTimeMillis();
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", Instant.now().toString());

        System.out.println("[UserService] Création → " + userId);
        bus.publish(Topics.USER_CREATED, user);
        return user;
    }
}
