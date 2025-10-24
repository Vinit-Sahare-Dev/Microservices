package com.sathya.conversion.service;

import com.sathya.conversion.model.CurrencyConversion;
import com.sathya.conversion.model.CurrencyExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ExchangeRateService exchangeRateService;

    // APPROACH 1: Using RestTemplate in Service Layer (ACTIVE)
    public CurrencyConversion calculateConversionUsingRestTemplate(String from, String to, BigDecimal quantity) {
        try {
            // Try external service first
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("from", from);
            uriVariables.put("to", to);

            ResponseEntity<CurrencyExchange> responseEntity = restTemplate.getForEntity(
                "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                CurrencyExchange.class,
                uriVariables
            );

            CurrencyExchange exchange = responseEntity.getBody();
            return createConversion(from, to, quantity, exchange, "rest-template");
            
        } catch (Exception e) {
            // Fallback to local exchange rates
            CurrencyExchange exchange = exchangeRateService.getExchangeRate(from, to);
            return createConversion(from, to, quantity, exchange, "rest-template-with-local-fallback");
        }
    }

    /*
    // APPROACH 2: Direct Implementation in Service (COMMENTED)
    public CurrencyConversion calculateConversionDirect(String from, String to, BigDecimal quantity) {
        try {
            // Direct RestTemplate call
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("from", from);
            uriVariables.put("to", to);

            ResponseEntity<CurrencyExchange> responseEntity = restTemplate.getForEntity(
                "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                CurrencyExchange.class,
                uriVariables
            );

            CurrencyExchange exchange = responseEntity.getBody();
            return createConversion(from, to, quantity, exchange, "direct-resttemplate");
            
        } catch (Exception e) {
            CurrencyExchange exchange = exchangeRateService.getExchangeRate(from, to);
            return createConversion(from, to, quantity, exchange, "direct-resttemplate-with-local");
        }
    }
    */

    /*
    // APPROACH 3: Using Feign Client (COMMENTED)
    @Autowired
    private CurrencyExchangeServiceProxy feignClient;

    public CurrencyConversion calculateConversionUsingFeign(String from, String to, BigDecimal quantity) {
        try {
            CurrencyConversion conversion = feignClient.retrieveExchangeValue(from, to);
            BigDecimal total = quantity.multiply(conversion.getExchangeRate());
            conversion.setQuantity(quantity);
            conversion.setTotalCalculatedAmount(total);
            conversion.setClientUsed("feign");
            return conversion;
        } catch (Exception e) {
            CurrencyExchange exchange = exchangeRateService.getExchangeRate(from, to);
            return createConversion(from, to, quantity, exchange, "feign-with-local");
        }
    }
    */

    // Helper method to create conversion response
    private CurrencyConversion createConversion(String from, String to, BigDecimal quantity, 
                                              CurrencyExchange exchange, String clientUsed) {
        BigDecimal total = quantity.multiply(exchange.getExchangeRate());
        
        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setId(exchange.getId());
        conversion.setFrom(from);
        conversion.setTo(to);
        conversion.setQuantity(quantity);
        conversion.setExchangeRate(exchange.getExchangeRate());
        conversion.setTotalCalculatedAmount(total);
        conversion.setEnvironment(exchange.getEnvironment());
        conversion.setClientUsed(clientUsed);
        
        return conversion;
    }
}