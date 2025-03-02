package com.example.messagingstompwebsocket;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.simp.broker.DefaultSubscriptionRegistry;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomSubscriptionRegistry extends DefaultSubscriptionRegistry implements ApplicationContextAware {

    private Map<String, Map<String, Map<String, Object>>> customizedSubscriptionCache = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;


    @Autowired
    private SimpUserRegistry userRegistry;


    private Map<String, SubsciptionHandler> getSubscriptionHandlers() {
        return applicationContext.getBeansOfType(SubsciptionHandler.class);
    }

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
        System.out.println("setSubscriptionAttribute"  + sessionId + " " +subscriptionId + " thread name" + Thread.currentThread().getName());

        var attributes = customizedSubscriptionCache
                .get(sessionId)
                .get(subscriptionId);

        attributes.put(name, value);
    }

    @Override
    protected void addSubscriptionInternal(String sessionId, String subscriptionId, String destination, Message<?> message) {
        super.addSubscriptionInternal(sessionId, subscriptionId, destination, message);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("addSubscriptionInternal" + sessionId + " " +subscriptionId + " thread name" + Thread.currentThread().getName());

        var subHandlers = getSubscriptionHandlers().values();

        for (SubsciptionHandler subHandler: subHandlers) {

            if (subHandler.getDestination().equals(destination)) {
                var attribute = subHandler.supplyAttributes(sessionId, subscriptionId, message);

                var session = customizedSubscriptionCache.computeIfAbsent(sessionId, (s) -> new HashMap<>());

                session.computeIfAbsent(subscriptionId, (s) -> new HashMap<>());

                session.get(subscriptionId).putAll(attribute);

                break;
            }

        }

    }

    @Override
    protected void removeSubscriptionInternal(String sessionId, String subscriptionId, Message<?> message) {
        super.removeSubscriptionInternal(sessionId, subscriptionId, message);
        System.out.println("removeSubscriptionInternal" + sessionId + " " +subscriptionId);

        var session = customizedSubscriptionCache.get(sessionId );

        if (session == null) {
            return;
        }

        session.remove(subscriptionId);

    }

    @Override
    public void unregisterAllSubscriptions(String sessionId) {
        super.unregisterAllSubscriptions(sessionId);
        System.out.println("unregisterAllSubscriptions "+ sessionId + " thread name" + Thread.currentThread().getName());

        customizedSubscriptionCache.remove(sessionId);
    }

    @Override
    protected MultiValueMap<String, String> findSubscriptionsInternal(String destination, Message<?> message) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

        var userList = this.userRegistry.getUsers();
        userList.forEach( u -> {

			System.out.println("---------------"+u.getName());

			u.getSessions().forEach(simpSession -> {

				simpSession.getSubscriptions().forEach(sub -> {

                    Collection<SubsciptionHandler> subHandlers = getSubscriptionHandlers().values();

                    for (SubsciptionHandler subHandler: subHandlers) {

                        if (subHandler.getDestination().equals(destination)) {
                            System.out.println("destination matched");

                            var subAttributes = this.getSubscriptionAttributes(simpSession.getId(), sub.getId());

                            if (subHandler.shouldRecieve(subAttributes, message)) {
                                result.putIfAbsent(simpSession.getId(), new ArrayList<>());

                                result.get(simpSession.getId()).add(sub.getId());
                            }


                        }
                    }


                });
            });
        });


        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
