package com.sathya.conversion.client;  // Correct package

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sathya.conversion.model.CurrencyExchange;

@FeignClient(name = "currency-exchange-service", url = "${exchange.service.url}")
public interface CurrencyExchangeClient {
    
    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    CurrencyExchange getExchangeRate(
        @PathVariable("from") String from,
        @PathVariable("to") String to
    );
}