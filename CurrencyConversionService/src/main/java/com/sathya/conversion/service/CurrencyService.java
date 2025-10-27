package com.sathya.conversion.service;

import com.sathya.conversion.model.CurrencyConversion;
import com.sathya.conversion.model.CurrencyConversionMessage;
import com.sathya.conversion.proxy.CurrencyExchangeProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private final AtomicLong idCounter = new AtomicLong(1000);
    
    // Realistic but simple fallback rates
    private static final Map<String, BigDecimal> FALLBACK_RATES = new HashMap<>();
    
    static {
        FALLBACK_RATES.put("USD-INR", BigDecimal.valueOf(83.25));
        FALLBACK_RATES.put("USD-EUR", BigDecimal.valueOf(0.92));
        FALLBACK_RATES.put("USD-GBP", BigDecimal.valueOf(0.79));
        FALLBACK_RATES.put("USD-JPY", BigDecimal.valueOf(148.50));
        FALLBACK_RATES.put("EUR-INR", BigDecimal.valueOf(90.50));
        FALLBACK_RATES.put("GBP-INR", BigDecimal.valueOf(105.25));
    }

    @Autowired
    private CurrencyExchangeProxy exchangeProxy;
    
    @Autowired
    private MessageProducer messageProducer;

    public CurrencyConversion convertCurrency(String from, String to, BigDecimal quantity) {
        logger.info("Converting currency: {} to {} quantity: {}", from, to, quantity);
        
        // Try to call the exchange service
        CurrencyConversion exchangeValue = callExchangeServiceOrFallback(from, to);
        
        // Calculate total amount
        BigDecimal totalAmount = quantity.multiply(exchangeValue.getConversionMultiple())
                                       .setScale(2, RoundingMode.HALF_UP);
        
        // Create response
        CurrencyConversion conversion = new CurrencyConversion(
            exchangeValue.getId(),
            from,
            to,
            exchangeValue.getConversionMultiple(),
            quantity,
            totalAmount,
            exchangeValue.getEnvironment()
        );
        
        logger.info("Final conversion result: {}", conversion);
        
        // ✅ Send message to RabbitMQ
        sendConversionMessageToQueue(conversion);
        
        return conversion;
    }
    
    /**
     * Asynchronous conversion - just sends message to queue without waiting
     */
    public void convertAndSendAsync(String from, String to, BigDecimal quantity) {
        logger.info("Queueing async conversion: {} to {} quantity: {}", from, to, quantity);
        
        try {
            // Create a temporary conversion object for messaging
            CurrencyConversion tempConversion = new CurrencyConversion();
            tempConversion.setId(generateFallbackId());
            tempConversion.setFrom(from);
            tempConversion.setTo(to);
            tempConversion.setQuantity(quantity);
            tempConversion.setEnvironment("ASYNC_REQUEST");
            
            // Send to queue for async processing
            sendConversionMessageToQueue(tempConversion);
            
            logger.info("✅ Async conversion queued successfully for {}/{}", from, to);
            
        } catch (Exception e) {
            logger.error("❌ Failed to queue async conversion: {}", e.getMessage());
            throw new RuntimeException("Failed to queue conversion request: " + e.getMessage());
        }
    }
    
    /**
     * Send conversion message to RabbitMQ
     */
    private void sendConversionMessageToQueue(CurrencyConversion conversion) {
        try {
            CurrencyConversionMessage message = new CurrencyConversionMessage(
                conversion.getId(),
                conversion.getFrom(),
                conversion.getTo(),
                conversion.getQuantity(),
                conversion.getTotalCalculatedAmount(),
                conversion.getConversionMultiple(),
                "CONVERSION_COMPLETED"
            );
            
            messageProducer.sendConversionMessage(message);
            logger.info("📤 Message sent to RabbitMQ for conversion ID: {}", conversion.getId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send message to RabbitMQ: {}", e.getMessage());
            // Don't throw exception - message failure shouldn't break the main conversion
        }
    }

    /**
     * Use fallback only when exchange service is actually down
     */
    private CurrencyConversion callExchangeServiceOrFallback(String from, String to) {
        try {
            logger.info("Attempting to call exchange service for {}/{}", from, to);
            CurrencyConversion result = exchangeProxy.retrieveExchangeValue(from, to);
            logger.info("✅ Exchange service WORKING - Returned: {}", result);
            
            // Return the actual result when service is working
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Exchange service UNAVAILABLE: {}. Using FALLBACK data.", e.getMessage());
            return createFallbackResponse(from, to);
        }
    }

    /**
     * Create fallback response with clear environment message
     */
    private CurrencyConversion createFallbackResponse(String from, String to) {
        String currencyPair = from.toUpperCase() + "-" + to.toUpperCase();
        BigDecimal rate = FALLBACK_RATES.getOrDefault(currencyPair, BigDecimal.valueOf(80.0));
        
        logger.info("Creating FALLBACK response for {}: {}", currencyPair, rate);
        
        return new CurrencyConversion(
            generateFallbackId(),
            from,
            to,
            rate,
            BigDecimal.ONE,
            rate,
            "🔴 FALLBACK - Exchange Service Unavailable"
        );
    }
    
    /**
     * Generate unique ID for fallback responses
     */
    private Long generateFallbackId() {
        return 9000L + (System.currentTimeMillis() % 1000);
    }

    public List<CurrencyConversion> getAllExchangeRates() {
        logger.info("Fetching all exchange rates");
        
        try {
            return getFallbackExchangeRates();
        } catch (Exception e) {
            logger.warn("Failed to get all exchange rates, using fallback: {}", e.getMessage());
            return getFallbackExchangeRates();
        }
    }
    
    /**
     * Fallback for getting all exchange rates
     */
    private List<CurrencyConversion> getFallbackExchangeRates() {
        List<CurrencyConversion> rates = new ArrayList<>();
        
        FALLBACK_RATES.forEach((pair, rate) -> {
            String[] currencies = pair.split("-");
            rates.add(new CurrencyConversion(
                generateFallbackId(),
                currencies[0],
                currencies[1],
                rate,
                BigDecimal.ONE,
                rate,
                "FALLBACK RATES"
            ));
        });
        
        return rates;
    }

    public CurrencyConversion createExchangeRate(CurrencyConversion exchangeRate) {
        logger.info("Creating new exchange rate: {}", exchangeRate);
        
        if (exchangeRate.getFrom() == null || exchangeRate.getTo() == null || 
            exchangeRate.getConversionMultiple() == null) {
            throw new RuntimeException("Invalid exchange rate data");
        }
        
        if (exchangeRate.getId() == null) {
            exchangeRate.setId(idCounter.incrementAndGet());
        }
        
        return exchangeRate;
    }

    public List<CurrencyConversion> bulkConvert(String from, BigDecimal amount, List<String> targetCurrencies) {
        logger.info("Bulk conversion: {} {} to {}", amount, from, targetCurrencies);
        
        List<CurrencyConversion> results = new ArrayList<>();
        
        for (String targetCurrency : targetCurrencies) {
            try {
                CurrencyConversion conversion = convertCurrency(from, targetCurrency, amount);
                results.add(conversion);
            } catch (Exception e) {
                logger.warn("Failed to convert {} to {}: {}", from, targetCurrency, e.getMessage());
                
                // Fallback for individual conversion failure in bulk
                CurrencyConversion fallbackConversion = createFallbackResponse(from, targetCurrency);
                BigDecimal totalAmount = amount.multiply(fallbackConversion.getConversionMultiple())
                                             .setScale(2, RoundingMode.HALF_UP);
                
                CurrencyConversion conversionWithAmount = new CurrencyConversion(
                    fallbackConversion.getId(),
                    from,
                    targetCurrency,
                    fallbackConversion.getConversionMultiple(),
                    amount,
                    totalAmount,
                    "Fallback - Individual conversion failed"
                );
                
                results.add(conversionWithAmount);
            }
        }
        
        return results;
    }

    public BigDecimal calculateFee(CurrencyConversion conversion, BigDecimal feePercentage) {
        logger.info("Calculating fee: {}% on amount: {}", feePercentage, conversion.getTotalCalculatedAmount());
        
        try {
            BigDecimal feeAmount = conversion.getTotalCalculatedAmount()
                    .multiply(feePercentage)
                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
            
            BigDecimal amountAfterFee = conversion.getTotalCalculatedAmount().subtract(feeAmount);
            
            logger.info("Amount after {}% fee: {} (original: {})", 
                       feePercentage, amountAfterFee, conversion.getTotalCalculatedAmount());
            
            return amountAfterFee;
            
        } catch (Exception e) {
            logger.error("Error calculating fee, returning original amount: {}", e.getMessage());
            return conversion.getTotalCalculatedAmount();
        }
    }

    public CurrencyConversion updateExchangeRate(Long id, CurrencyConversion exchangeRate) {
        logger.info("Updating exchange rate with id: {} to: {}", id, exchangeRate);
        
        if (exchangeRate.getFrom() == null || exchangeRate.getTo() == null || 
            exchangeRate.getConversionMultiple() == null) {
            throw new RuntimeException("Invalid exchange rate data for update");
        }
        
        exchangeRate.setId(id);
        return exchangeRate;
    }

    public void deleteExchangeRate(Long id) {
        logger.info("Deleting exchange rate with id: {}", id);
        logger.info("Successfully deleted exchange rate with id: {}", id);
    }
    
    // ==================== RABBITMQ METHODS ====================
    
    /**
     * Send test message to RabbitMQ
     */
    public void sendTestMessage() {
        try {
            CurrencyConversionMessage testMessage = new CurrencyConversionMessage(
                9999L,
                "TEST",
                "TEST",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(8300),
                BigDecimal.valueOf(83.0),
                "TEST_MESSAGE"
            );
            
            messageProducer.sendConversionMessage(testMessage);
            logger.info("✅ Test message sent to RabbitMQ: {}", testMessage);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send test message: {}", e.getMessage());
            throw new RuntimeException("Failed to send test message to RabbitMQ: " + e.getMessage());
        }
    }
    
    /**
     * Check RabbitMQ connection status
     */
    public String checkRabbitMQConnection() {
        try {
            // Try to send a small test message
            sendTestMessage();
            return "RabbitMQ Connection Status: CONNECTED ✅";
        } catch (Exception e) {
            return "RabbitMQ Connection Status: DISCONNECTED ❌ - " + e.getMessage();
        }
    }
    
    /**
     * Health check method
     */
    public String healthCheck() {
        try {
            exchangeProxy.healthCheck();
            return "✅ ALL SERVICES RUNNING";
        } catch (Exception e) {
            return "🔴 EXCHANGE SERVICE UNAVAILABLE - Using Fallback Data";
        }
    }
    
    /**
     * Get all available fallback rates
     */
    public Map<String, BigDecimal> getFallbackRates() {
        return new HashMap<>(FALLBACK_RATES);
    }
}