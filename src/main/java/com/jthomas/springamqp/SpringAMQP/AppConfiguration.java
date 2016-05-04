package com.jthomas.springamqp.SpringAMQP;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@ComponentScan(basePackages = "org.jthomas.springamqp")
@PropertySource(value = {"classpath:application.properties"})
@EnableRabbit
public class AppConfiguration {
	
	@Value("${queue_name}")
	private String queueName;
	
	@Value("${exchange_name}")
	private String exchangeName;
	
	@Value("${dead_letter_exchange_name}")
	private String deadLetterExchange;
	
	@Value("${routing_key}")
	private String routingKeyForBibs;
	
	@Value("${dead_letter_queue}")
	private String deadLetterQueue;
	
	@Value("${broker_host}")
	private String host;
	
	@Value("${broker_virtual_host}")
	private String virtualHost;
	
	@Value("${broker_username}")
	private String brokerUsername;
	
	@Value("${broker_password}")
	private String brokerPassword;
	
	@Bean
	public ConnectionFactory connectionFactory(){
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
		connectionFactory.setVirtualHost(virtualHost);
		connectionFactory.setUsername(brokerUsername);
		connectionFactory.setPassword(brokerPassword);
		connectionFactory.setChannelCacheSize(10);
		connectionFactory.setRequestedHeartBeat(10);
		return connectionFactory;
	}
	
	@Bean
	public AmqpTemplate rabbitTemplate(){
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		RetryTemplate retryTemplate = new RetryTemplate();
		ExponentialBackOffPolicy backOffpolicy = new ExponentialBackOffPolicy();
		backOffpolicy.setInitialInterval(500);
		backOffpolicy.setMaxInterval(10000);
		backOffpolicy.setMultiplier(10.0);
		retryTemplate.setBackOffPolicy(backOffpolicy);
		rabbitTemplate.setRetryTemplate(retryTemplate);
		return rabbitTemplate;
	}
	
	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(){
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		factory.setConcurrentConsumers(3);
		factory.setDefaultRequeueRejected(false);
		return factory;
	}
	
	@Bean
	public RabbitAdmin rabbitAdminForSierraUpdaterBibs(ConnectionFactory connectionFactory){
		return new RabbitAdmin(connectionFactory);
	}
	
	@Bean
	public Exchange sierraXChange(){
		return new DirectExchange(exchangeName, true, false);
	}
	
	@Bean
	public Queue sierraBibsUpdaterQueue(){
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("x-dead-letter-exchange", deadLetterExchange);
		arguments.put("x-dead-letter-routing-key", routingKeyForBibs);
		return new Queue(queueName, true, false, false, arguments);
	}
	
	@Bean
	public Queue sierraBibsDLQueue(){
		return new Queue(deadLetterQueue, true, false, false);
	}
	
    @Bean
    public List<Declarable> declaredDeadLetterResources() {
    	return Arrays.<Declarable>asList(
    			new DirectExchange(deadLetterExchange, true, false), sierraBibsDLQueue(),
    			new Binding(deadLetterQueue, DestinationType.QUEUE, deadLetterExchange, routingKeyForBibs, null)
    	);
    }
    
    @Bean
    public List<Declarable> SierraUpdaterExchangeAndQueues() {
    	return Arrays.<Declarable>asList(
    			sierraXChange(), sierraBibsUpdaterQueue(),
    			new Binding(queueName, DestinationType.QUEUE, exchangeName, routingKeyForBibs, null)
    	);
    }
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(){
		return new PropertySourcesPlaceholderConfigurer();
	}

}
