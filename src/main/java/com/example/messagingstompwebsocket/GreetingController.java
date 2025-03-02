package com.example.messagingstompwebsocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class GreetingController {

	@Autowired
	CustomSubscriptionRegistry customSubscriptionRegistry;

	@Autowired
	private SimpUserRegistry userRegistry;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;


	@SubscribeMapping("/greetings")
	public List subscribeGreeting(StompHeaderAccessor stompHeaderAccessor) {
		System.out.println("subscribe greeting " );
		String subId = stompHeaderAccessor.getSubscriptionId();
		String sessionId = stompHeaderAccessor.getSessionId();
		String subscriberName = stompHeaderAccessor.getNativeHeader("subscriberName").get(0);

		customSubscriptionRegistry.setSubscriptionAttribute(sessionId, subId, "subscriberName", subscriberName);

		return new ArrayList<>();
	}

	@MessageMapping("/hello")
//	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {

		Set<SimpUser> userList = userRegistry.getUsers();

		userList.forEach( u -> {

			System.out.println("---------------aa"+u.getName());

			u.getSessions().forEach(simpSession -> {

				simpSession.getSubscriptions().forEach(sub -> {

					System.out.println("send msg to " + u.getName() + "----" + sub.getDestination() + "-----" + simpSession.getId() + "-----" + sub.getId());

					Map<String, Object> headers = new HashMap<>();
					headers.put("simpSessionId", simpSession.getId());
					headers.put("simpSubscriptionId", sub.getId());


					messagingTemplate.convertAndSend( sub.getDestination(), new Greeting("Hello ---" + u.getName()),
							headers  );

				});
			});

			//messagingTemplate.convertAndSend( "/topic/greetings", new Greeting("Hello ---" + u.getName()));
		});

		Thread.sleep(1000); // simulated delay
		return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
	}

}
