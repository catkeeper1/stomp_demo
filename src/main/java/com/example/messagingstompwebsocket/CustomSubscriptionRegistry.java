package com.example.messagingstompwebsocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.simp.broker.DefaultSubscriptionRegistry;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomSubscriptionRegistry extends DefaultSubscriptionRegistry {

    private Map<String, Map<String, Map<String, Object>>> customizedSubscriptionCache = new ConcurrentHashMap<>();


    public Map<String, Object> getSubscriptionAttributes(String sessionId , String subscriptionId) {
        var session = customizedSubscriptionCache.get(sessionId);

        if (session == null) {
            return null;
        }

        var attributes = session.get(subscriptionId);

        if (attributes == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.putAll(attributes);

        return result;
    }

    public void setSubscriptionAttribute(String sessionId , String subscriptionId, String name, Object value) {
        var attributes = customizedSubscriptionCache
                .get(sessionId)
                .get(subscriptionId);

        attributes.put(name, value);
    }

    @Override
    protected void addSubscriptionInternal(String sessionId, String subscriptionId, String destination, Message<?> message) {
        super.addSubscriptionInternal(sessionId, subscriptionId, destination, message);
        System.out.println("addSubscriptionInternal");

        var session = customizedSubscriptionCache.computeIfAbsent(sessionId, (s) -> new HashMap<>());

        session.computeIfAbsent(subscriptionId, (s) -> new HashMap<>());

    }

    @Override
    protected void removeSubscriptionInternal(String sessionId, String subscriptionId, Message<?> message) {
        super.removeSubscriptionInternal(sessionId, subscriptionId, message);
        System.out.println("removeSubscriptionInternal");

        var session = customizedSubscriptionCache.get(sessionId );

        if (session == null) {
            return;
        }

        session.remove(subscriptionId);

    }

    @Override
    public void unregisterAllSubscriptions(String sessionId) {
        super.unregisterAllSubscriptions(sessionId);
        System.out.println("unregisterAllSubscriptions");

        customizedSubscriptionCache.remove(sessionId);
    }

    @Override
    protected MultiValueMap<String, String> findSubscriptionsInternal(String destination, Message<?> message) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) message.getHeaders().get("nativeHeaders");

        if (nativeHeaders == null) {
            return super.findSubscriptionsInternal(destination, message);
        }

        result.put(nativeHeaders.get("simpSessionId").get(0), nativeHeaders.get("simpSubscriptionId"));
        return result;
    }

    @Configuration
    public static class ConfigSubscriptionRegistoryConfig {
        @Autowired
        AbstractBrokerMessageHandler simpleBrokerMessageHandler;

        @Bean
        public CustomSubscriptionRegistry customSubscriptionRegistry () {
            var result = new CustomSubscriptionRegistry();
            SimpleBrokerMessageHandler handler = (SimpleBrokerMessageHandler) this.simpleBrokerMessageHandler;

            handler.setSubscriptionRegistry(result);
            return result;
        }
    }


}
