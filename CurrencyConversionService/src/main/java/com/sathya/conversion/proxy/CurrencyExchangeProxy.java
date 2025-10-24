package com.sathya.conversion.proxy;

import com.sathya.conversion.model.CurrencyConversion;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "currency-exchange-service")
public interface CurrencyExchangeProxy {
    
    // GET - Retrieve exchange rate
    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    CurrencyConversion retrieveExchangeValue(
        @PathVariable String from,
        @PathVariable String to);
    
    // GET - All exchange rates
    @GetMapping("/currency-exchange/all")
    List<CurrencyConversion> getAllExchangeRates();
    
    // POST - Create new exchange rate
    @PostMapping("/currency-exchange")
    CurrencyConversion createExchangeRate(@RequestBody CurrencyConversion request);
    
    // PUT - Update exchange rate (FIXED)
    @PutMapping("/currency-exchange/{id}")
    CurrencyConversion updateExchangeRate(
        @PathVariable("id") Long id,
        @RequestBody CurrencyConversion request);
    
    // DELETE - Remove exchange rate (FIXED)
    @DeleteMapping("/currency-exchange/{id}")
    ResponseEntity<Void> deleteExchangeRate(@PathVariable("id") Long id);
    
    // GET - By ID (Optional)
    @GetMapping("/currency-exchange/{id}")
    ResponseEntity<CurrencyConversion> getExchangeRateById(@PathVariable Long id);
}