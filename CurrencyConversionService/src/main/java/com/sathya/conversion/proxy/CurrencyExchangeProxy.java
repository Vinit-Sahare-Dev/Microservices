package com.sathya.conversion.proxy;

import com.sathya.conversion.model.CurrencyConversion;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "currency-exchange-service")
public interface CurrencyExchangeProxy {

    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    CurrencyConversion retrieveExchangeValue(
        @PathVariable("from") String from,
        @PathVariable("to") String to
    );
    
    @GetMapping("/currency-exchange/health")
    String healthCheck();
}