package com.sathya.conversion.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "currency-exchange";
    
    // Queues
    public static final String CONVERSION_QUEUE = "currency-conversion-queue";
    public static final String NOTIFICATION_QUEUE = "currency-notification-queue";
    public static final String DEAD_LETTER_QUEUE = "currency-dead-letter-queue";
    
    // Routing Keys
    public static final String CONVERSION_ROUTING_KEY = "conversion.key";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.key";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter.key";

    // Queue for currency conversion messages
    @Bean
    public Queue conversionQueue() {
        return QueueBuilder.durable(CONVERSION_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }
    
    // Queue for notifications
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }
    
    // Dead letter queue for failed messages
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }
    
    // Topic Exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    // Bindings
    @Bean
    public Binding conversionBinding(Queue conversionQueue, TopicExchange exchange) {
        return BindingBuilder.bind(conversionQueue)
                .to(exchange)
                .with(CONVERSION_ROUTING_KEY);
    }
    
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(exchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }
    
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(exchange)
                .with(DEAD_LETTER_ROUTING_KEY);
    }
    
    // Message converter for JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}