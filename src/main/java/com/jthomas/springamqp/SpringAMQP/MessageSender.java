package com.jthomas.springamqp.SpringAMQP;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {
	
	@Value("${exchange_name}")
	private String exchangeName;
	
	@Value("${routing_key}")
	private String routingKey;
	
	public static void main(String[] args){
		ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
		AmqpTemplate template = context.getBean(AmqpTemplate.class);
		MessageSender sender = context.getBean(MessageSender.class);
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);;
		Message message = new Message(new String("Hello Jobin").getBytes(), messageProperties);
		template.send(sender.exchangeName, sender.routingKey, message);
	}

}
