package com.sathya.exchangeservice.service;

import com.sathya.exchangeservice.model.CurrencyExchange;
import com.sathya.exchangeservice.repository.CurrencyExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class CurrencyExchangeService {
    
    @Autowired
    private CurrencyExchangeRepository repository;
    
    @Autowired
    private Environment environment;
    
    public CurrencyExchange getExchangeRate(String from, String to) {
        CurrencyExchange exchange = repository.findByFromCurrencyAndToCurrency(from, to);
        
        if (exchange == null) {
            throw new RuntimeException("Unable to find exchange rate from " + from + " to " + to);
        }
        
        // Set the port to identify which instance handled the request
        String port = environment.getProperty("local.server.port");
        exchange.setEnvironment(port);
        
        return exchange;
    }
}