package com.sathya.conversion.service;

import com.sathya.conversion.config.RabbitMQConfig;
import com.sathya.conversion.model.CurrencyConversionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void sendConversionMessage(CurrencyConversionMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.CONVERSION_ROUTING_KEY,
                message
            );
            logger.info("✅ Conversion message sent: {}", message);
            
            // Also send notification
            sendNotificationMessage(message);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send conversion message: {}", e.getMessage());
            throw new RuntimeException("RabbitMQ message sending failed: " + e.getMessage());
        }
    }
    
    private void sendNotificationMessage(CurrencyConversionMessage message) {
        try {
            message.setStatus("NOTIFICATION_SENT");
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                message
            );
            logger.info("📧 Notification message sent for conversion ID: {}", message.getConversionId());
        } catch (Exception e) {
            logger.error("❌ Failed to send notification message: {}", e.getMessage());
        }
    }
}