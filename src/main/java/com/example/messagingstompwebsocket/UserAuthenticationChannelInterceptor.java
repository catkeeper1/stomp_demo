package com.example.messagingstompwebsocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Collections;


public class UserAuthenticationChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = "username";//System.currentTimeMillis() + "";
            System.out.println("user id " + userId+" connected");

            accessor.setSessionAttributes(Collections.singletonMap("customizedAttribute", "1234"));
            var  principle = new CustomPrincipal(userId, "customized attribute");
            accessor.setUser(principle);
        }
        return message;
    }
}