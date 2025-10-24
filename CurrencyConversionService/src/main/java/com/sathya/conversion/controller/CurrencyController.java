package com.sathya.conversion.controller;

import com.sathya.conversion.model.CurrencyConversion;
import com.sathya.conversion.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/currency-conversion")
public class CurrencyController {
    
    @Autowired
    private CurrencyService currencyService;
    
    @GetMapping("/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion convertCurrency(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {
        
        return currencyService.convertCurrency(from, to, quantity);
    }
    
    @GetMapping("/test")
    public String test() {
        return "Currency Conversion Service is working!";
    }
}