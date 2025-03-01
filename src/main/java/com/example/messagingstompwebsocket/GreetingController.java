package com.example.messagingstompwebsocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
public class GreetingController {



	@Autowired
	private SimpUserRegistry userRegistry;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;


	@MessageMapping("/hello")
//	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {

		Set<SimpUser> userList = userRegistry.getUsers();

		userList.forEach( u -> {

			System.out.println("---------------"+u.getName());

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
