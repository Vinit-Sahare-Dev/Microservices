package com.sathya.conversion.controller;

import com.sathya.conversion.model.CurrencyConversion;
import com.sathya.conversion.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    // APPROACH 1: Using Service Layer with RestTemplate (ACTIVE)
    @GetMapping("/currency-converter/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion convertCurrency(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {
        
        return currencyService.calculateConversionUsingRestTemplate(from, to, quantity);
    }

    /*
    // APPROACH 2: Direct Implementation in Controller (COMMENTED)
    @GetMapping("/currency-converter-direct/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion convertCurrencyDirect(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {
        
        // Direct implementation without service layer
        // Create parameter map for REST call
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);

        // Call currency exchange service
        ResponseEntity<CurrencyExchange> responseEntity = restTemplate.getForEntity(
            "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
            CurrencyExchange.class,
            uriVariables
        );

        CurrencyExchange exchange = responseEntity.getBody();

        // Calculate total amount
        BigDecimal total = quantity.multiply(exchange.getExchangeRate());

        // Create and populate conversion object
        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setId(exchange.getId());
        conversion.setFrom(from);
        conversion.setTo(to);
        conversion.setQuantity(quantity);
        conversion.setExchangeRate(exchange.getExchangeRate());
        conversion.setTotalCalculatedAmount(total);
        conversion.setEnvironment(exchange.getEnvironment());
        conversion.setClientUsed("rest-template-direct");

        return conversion;
    }
    */

    /*
    // APPROACH 3: Using Feign Client (COMMENTED)
    @GetMapping("/currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion convertCurrencyFeign(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {
        
        return currencyService.calculateConversionUsingFeign(from, to, quantity);
    }
    */
    
    // Health check endpoint
    @GetMapping("/health")
    public String healthCheck() {
        return "Currency Conversion Service is running!";
    }
    
    // Test endpoint with default values
    @GetMapping("/currency-converter/test")
    public CurrencyConversion testConversion() {
        return currencyService.calculateConversionUsingRestTemplate("USD", "INR", new BigDecimal(100));
    }

    // Mock data test endpoint
    @GetMapping("/test-mock")
    public CurrencyConversion testMockConversion() {
        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setId(1000L);
        conversion.setFrom("USD");
        conversion.setTo("INR");
        conversion.setQuantity(new BigDecimal(100));
        conversion.setExchangeRate(new BigDecimal("83.25"));
        conversion.setTotalCalculatedAmount(new BigDecimal("8325.00"));
        conversion.setEnvironment("mock");
        conversion.setClientUsed("test");
        return conversion;
    }
}