package com.sathya.conversion.service;

import com.sathya.conversion.client.CurrencyExchangeClient;
import com.sathya.conversion.model.CurrencyConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyService {
    
    @Autowired
    private CurrencyExchangeClient exchangeClient;
    
    @Autowired
    private Environment environment;
    
    public CurrencyConversion convertCurrency(String from, String to, BigDecimal quantity) {
        try {
            // Call currency-exchange service via Feign client
            CurrencyConversion exchange = exchangeClient.getExchangeValue(from, to);
            
            // Calculate total amount
            BigDecimal totalAmount = quantity.multiply(exchange.getConversionMultiple());
            
            // Create response object
            CurrencyConversion response = new CurrencyConversion();
            response.setId(exchange.getId());
            response.setFrom(from);
            response.setTo(to);
            response.setConversionMultiple(exchange.getConversionMultiple());
            response.setQuantity(quantity);
            response.setTotalCalculatedAmount(totalAmount);
            response.setEnvironment(environment.getProperty("local.server.port"));
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("Error converting currency: " + e.getMessage(), e);
        }
    }
}