package com.sathya.exchangeservice.controller;

import com.sathya.exchangeservice.model.CurrencyExchange;
import com.sathya.exchangeservice.service.CurrencyExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CurrencyExchangeController {
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private CurrencyExchangeService currencyExchangeService;
    
    // GET - Get exchange rate by currencies
    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    public CurrencyExchange retrieveExchangeValue(
            @PathVariable String from,
            @PathVariable String to) {
        
        CurrencyExchange exchange = currencyExchangeService.getExchangeRate(from, to);
        String port = environment.getProperty("local.server.port");
        exchange.setEnvironment(port);
        return exchange;
    }
    
    // GET - All exchange rates
    @GetMapping("/currency-exchange/all")
    public List<CurrencyExchange> getAllExchangeRates() {
        List<CurrencyExchange> rates = currencyExchangeService.getAllExchangeRates();
        String port = environment.getProperty("local.server.port");
        rates.forEach(rate -> rate.setEnvironment(port));
        return rates;
    }
    
    // GET - Get exchange rate by ID
    @GetMapping("/currency-exchange/{id}")
    public ResponseEntity<CurrencyExchange> getExchangeRateById(@PathVariable Long id) {
        return currencyExchangeService.getExchangeRateById(id)
                .map(exchange -> {
                    String port = environment.getProperty("local.server.port");
                    exchange.setEnvironment(port);
                    return ResponseEntity.ok(exchange);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // POST - Create new exchange rate
    @PostMapping("/currency-exchange")
    public ResponseEntity<CurrencyExchange> createExchangeRate(@RequestBody CurrencyExchange exchange) {
        try {
            CurrencyExchange created = currencyExchangeService.createExchangeRate(exchange);
            String port = environment.getProperty("local.server.port");
            created.setEnvironment(port);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // PUT - Update exchange rate
    @PutMapping("/currency-exchange/{id}")
    public ResponseEntity<CurrencyExchange> updateExchangeRate(
            @PathVariable Long id,
            @RequestBody CurrencyExchange exchange) {
        
        try {
            CurrencyExchange updated = currencyExchangeService.updateExchangeRate(id, exchange);
            String port = environment.getProperty("local.server.port");
            updated.setEnvironment(port);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE - Delete exchange rate
    @DeleteMapping("/currency-exchange/{id}")
    public ResponseEntity<Void> deleteExchangeRate(@PathVariable Long id) {
        try {
            currencyExchangeService.deleteExchangeRate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String port = environment.getProperty("local.server.port");
        return ResponseEntity.ok("Currency Exchange Service is running on port: " + port);
    }
}