package com.jthomas.springamqp.SpringAMQP;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {
	
	@RabbitListener(queues = "${queue_name}")
	public void displayMessage(Message data) throws Exception{
		String body = new String(data.getBody());
		System.out.println("Received: " + body);
		Thread.sleep(20000);
		if(body.equalsIgnoreCase("Jobin5"))
			throw new Exception();
		if(body.equalsIgnoreCase("Jobin8"))
			throw new Exception();
		if(body.equalsIgnoreCase("Jobin10"))
			throw new Exception();
		if(body.equalsIgnoreCase("Jobin12"))
			throw new Exception();
		
	}
	
	public static void main(String[] args){
		ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
		context.getBean(MessageReceiver.class);
	}

}
