package com.sathya.conversion.controller;

import com.sathya.conversion.model.CurrencyConversion;
import com.sathya.conversion.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private CurrencyService currencyService;
    
    // ==================== GET ENDPOINTS ====================
    
    @GetMapping("/convert/from/{from}/to/{to}/quantity/{quantity}")
    public ResponseEntity<CurrencyConversion> convertCurrency(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {
        
        try {
            CurrencyConversion conversion = currencyService.convertCurrency(from, to, quantity);
            String port = environment.getProperty("local.server.port");
            conversion.setEnvironment(port + " (service layer)");
            
            return ResponseEntity.ok(conversion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/exchange-rates")
    public ResponseEntity<List<CurrencyConversion>> getAllExchangeRates() {
        try {
            List<CurrencyConversion> rates = currencyService.getAllExchangeRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== POST ENDPOINTS ====================
    
    @PostMapping("/exchange-rate")
    public ResponseEntity<CurrencyConversion> createExchangeRate(@RequestBody CurrencyConversion exchangeRate) {
        try {
            CurrencyConversion created = currencyService.createExchangeRate(exchangeRate);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/bulk-convert")
    public ResponseEntity<List<CurrencyConversion>> bulkCurrencyConversion(
            @RequestBody BulkConversionRequest request) {
        
        try {
            List<CurrencyConversion> results = currencyService.bulkConvert(
                request.getFrom(), 
                request.getAmount(), 
                request.getTargetCurrencies()
            );
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/convert-with-fee/from/{from}/to/{to}/quantity/{quantity}/fee/{feePercentage}")
    public ResponseEntity<CurrencyConversion> convertWithFee(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity,
            @PathVariable BigDecimal feePercentage) {
        
        try {
            // First convert currency
            CurrencyConversion conversion = currencyService.convertCurrency(from, to, quantity);
            
            // Calculate amount after fee
            BigDecimal amountAfterFee = currencyService.calculateFee(conversion, feePercentage);
            
            // Create new response with fee information
            CurrencyConversion conversionWithFee = new CurrencyConversion(
                conversion.getId(),
                from,
                to,
                conversion.getConversionMultiple(),
                quantity,
                amountAfterFee,
                conversion.getEnvironment() + " (with " + feePercentage + "% fee)"
            );
            
            return ResponseEntity.ok(conversionWithFee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // ==================== PUT ENDPOINT ====================
    
    @PutMapping("/exchange-rate/{id}")
    public ResponseEntity<CurrencyConversion> updateExchangeRate(
            @PathVariable Long id,
            @RequestBody CurrencyConversion exchangeRate) {
        
        try {
            CurrencyConversion updated = currencyService.updateExchangeRate(id, exchangeRate);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ==================== DELETE ENDPOINT ====================
    
    @DeleteMapping("/exchange-rate/{id}")
    public ResponseEntity<Void> deleteExchangeRate(@PathVariable Long id) {
        try {
            currencyService.deleteExchangeRate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ==================== HEALTH CHECK ====================
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String port = environment.getProperty("local.server.port");
        return ResponseEntity.ok("Currency Conversion Service is running on port: " + port);
    }
}

// Supporting class for bulk conversion
class BulkConversionRequest {
    private String from;
    private BigDecimal amount;
    private List<String> targetCurrencies;
    
    // Constructors
    public BulkConversionRequest() {}
    
    public BulkConversionRequest(String from, BigDecimal amount, List<String> targetCurrencies) {
        this.from = from;
        this.amount = amount;
        this.targetCurrencies = targetCurrencies;
    }
    
    // Getters and setters
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public List<String> getTargetCurrencies() { return targetCurrencies; }
    public void setTargetCurrencies(List<String> targetCurrencies) { this.targetCurrencies = targetCurrencies; }
}