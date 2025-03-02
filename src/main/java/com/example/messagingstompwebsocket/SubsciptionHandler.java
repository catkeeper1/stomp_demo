package com.example.messagingstompwebsocket;

import org.springframework.messaging.Message;

import java.util.Map;

public interface SubsciptionHandler {

    String getDestination();

    Map<String, Object> supplyAttributes(String sessionId, String subscriptionId, Message<?> message);

    boolean shouldRecieve(Map<String, Object> attributes, Message<?> message) ;
}
