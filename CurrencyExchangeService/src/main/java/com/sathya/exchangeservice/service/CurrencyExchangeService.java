package com.sathya.exchangeservice.service;

import com.sathya.exchangeservice.model.CurrencyExchange;
import com.sathya.exchangeservice.repository.CurrencyExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CurrencyExchangeService {
    
    @Autowired
    private CurrencyExchangeRepository repository;
    
    public CurrencyExchange getExchangeRate(String from, String to) {
        CurrencyExchange exchangeValue = repository.findByFromAndTo(from, to);
        if (exchangeValue == null) {
            throw new RuntimeException("Exchange rate not found for " + from + " to " + to);
        }
        return exchangeValue;
    }
    
    public List<CurrencyExchange> getAllExchangeRates() {
        return repository.findAll();
    }
    
    public CurrencyExchange createExchangeRate(CurrencyExchange exchange) {
        // Check if already exists
        CurrencyExchange existing = repository.findByFromAndTo(exchange.getFrom(), exchange.getTo());
        if (existing != null) {
            throw new RuntimeException("Exchange rate already exists for " + exchange.getFrom() + " to " + exchange.getTo());
        }
        return repository.save(exchange);
    }
    
    public CurrencyExchange updateExchangeRate(Long id, CurrencyExchange exchange) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Exchange rate not found with id: " + id);
        }
        exchange.setId(id);
        return repository.save(exchange);
    }
    
    public void deleteExchangeRate(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Exchange rate not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    public Optional<CurrencyExchange> getExchangeRateById(Long id) {
        return repository.findById(id);
    }
    
    public boolean exchangeRateExists(String from, String to) {
        return repository.findByFromAndTo(from, to) != null;
    }
}