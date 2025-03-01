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

import java.util.List;
import java.util.Map;

public class CustomSubscriptionRegistry extends DefaultSubscriptionRegistry {

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
    public static class ConfigSubscriptionRegistory {
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
