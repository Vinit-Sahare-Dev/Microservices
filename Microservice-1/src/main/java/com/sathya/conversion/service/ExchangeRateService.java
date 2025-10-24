package com.sathya.conversion.service;

import com.sathya.conversion.model.CurrencyExchange;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeRateService {
    
    private final Map<String, BigDecimal> exchangeRates = new HashMap<>();
    
    public ExchangeRateService() {
        // Initialize with sample exchange rates
        exchangeRates.put("USD-INR", new BigDecimal("83.25"));
        exchangeRates.put("EUR-INR", new BigDecimal("88.50"));
        exchangeRates.put("GBP-INR", new BigDecimal("105.75"));
        exchangeRates.put("USD-EUR", new BigDecimal("0.92"));
        exchangeRates.put("EUR-USD", new BigDecimal("1.09"));
        exchangeRates.put("USD-GBP", new BigDecimal("0.79"));
        exchangeRates.put("EUR-GBP", new BigDecimal("0.85"));
        exchangeRates.put("GBP-USD", new BigDecimal("1.27"));
    }
    
    public CurrencyExchange getExchangeRate(String from, String to) {
        String key = from + "-" + to;
        BigDecimal rate = exchangeRates.get(key);
        
        if (rate == null) {
            rate = new BigDecimal("1.0"); // Default rate
        }
        
        CurrencyExchange exchange = new CurrencyExchange();
        exchange.setId(1000L);
        exchange.setFrom(from);
        exchange.setTo(to);
        exchange.setExchangeRate(rate);
        exchange.setEnvironment("local-service");

        return exchange;
    }
}