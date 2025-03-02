package com.example.messagingstompwebsocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class GreetingSubsciptionHandler implements SubsciptionHandler{
    @Override
    public String getDestination() {
        return "/app/greetings";
    }

    @Override
    public Map<String, Object> supplyAttributes(String sessionId, String subscriptionId, Message<?> message) {

        StompHeaderAccessor assessor = StompHeaderAccessor.wrap(message);

        var names = assessor.getNativeHeader("subscriberName");

        if (names == null || names.isEmpty()) {
            return new HashMap<>();
        }
        System.out.println("subscriberName " + names.get(0));
        return Map.of("subscriberName", names.get(0));
    }

    @Override
    public boolean shouldRecieve(Map<String, Object> subAttributes, Message<?> message) {
        String subscriberName = (String) subAttributes.get("subscriberName");

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        String receiverName = headerAccessor.getNativeHeader("receiverName").get(0);

        System.out.println("subscriberName " + subscriberName + " receiverName " + receiverName);

        if (subscriberName != null && subscriberName.equals(receiverName)) {

            return true;
        }
        return false;
    }
}
