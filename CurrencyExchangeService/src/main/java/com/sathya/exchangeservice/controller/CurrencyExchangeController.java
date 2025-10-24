package com.sathya.exchangeservice.controller;

import com.sathya.exchangeservice.model.CurrencyExchange;
import com.sathya.exchangeservice.service.CurrencyExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currency-exchange")
public class CurrencyExchangeController {
    
    @Autowired
    private CurrencyExchangeService currencyExchangeService;
    
    @GetMapping("/from/{from}/to/{to}")
    public CurrencyExchange getExchangeValue(
            @PathVariable String from,
            @PathVariable String to) {
        
        return currencyExchangeService.getExchangeRate(from, to);
    }
    
    @GetMapping("/test")
    public String test() {
        return "Currency Exchange Service is working!";
    }
}