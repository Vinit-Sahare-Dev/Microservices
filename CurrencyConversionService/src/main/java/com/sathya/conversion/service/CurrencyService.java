package com.sathya.conversion.service;

import com.sathya.conversion.model.CurrencyConversion;
import com.sathya.conversion.proxy.CurrencyExchangeProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CurrencyService {
    
    @Autowired
    private CurrencyExchangeProxy currencyExchangeProxy;
    
    public CurrencyConversion convertCurrency(String from, String to, BigDecimal quantity) {
        // Validate input parameters
        validateCurrencyParams(from, to, quantity);
        
        // Get exchange rate from exchange service
        CurrencyConversion exchangeRate = currencyExchangeProxy.retrieveExchangeValue(from, to);
        
        // Calculate converted amount
        BigDecimal totalAmount = quantity.multiply(exchangeRate.getConversionMultiple());
        
        // Create and return conversion result
        return new CurrencyConversion(
            exchangeRate.getId(),
            from,
            to,
            exchangeRate.getConversionMultiple(),
            quantity,
            totalAmount,
            "conversion-service"
        );
    }
    
    public CurrencyConversion createExchangeRate(CurrencyConversion exchangeRate) {
        // Validate input
        if (exchangeRate.getFrom() == null || exchangeRate.getTo() == null) {
            throw new RuntimeException("From and To currencies are required");
        }
        
        if (exchangeRate.getConversionMultiple() == null || 
            exchangeRate.getConversionMultiple().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Conversion multiple must be greater than zero");
        }
        
        // Call exchange service to create new rate
        return currencyExchangeProxy.createExchangeRate(exchangeRate);
    }
    
    public List<CurrencyConversion> getAllExchangeRates() {
        return currencyExchangeProxy.getAllExchangeRates();
    }
    
    public List<CurrencyConversion> bulkConvert(String from, BigDecimal amount, List<String> targetCurrencies) {
        validateCurrencyParams(from, "USD", amount); // Using USD as dummy for validation
        
        return targetCurrencies.stream()
            .map(targetCurrency -> {
                CurrencyConversion rate = currencyExchangeProxy.retrieveExchangeValue(from, targetCurrency);
                BigDecimal totalAmount = amount.multiply(rate.getConversionMultiple());
                
                return new CurrencyConversion(
                    rate.getId(),
                    from,
                    targetCurrency,
                    rate.getConversionMultiple(),
                    amount,
                    totalAmount,
                    "bulk-conversion"
                );
            })
            .toList();
    }
    
    public CurrencyConversion updateExchangeRate(Long id, CurrencyConversion exchangeRate) {
        return currencyExchangeProxy.updateExchangeRate(id, exchangeRate);
    }
    
    public void deleteExchangeRate(Long id) {
        currencyExchangeProxy.deleteExchangeRate(id);
    }
    
    private void validateCurrencyParams(String from, String to, BigDecimal quantity) {
        if (from == null || from.trim().isEmpty()) {
            throw new RuntimeException("From currency cannot be empty");
        }
        
        if (to == null || to.trim().isEmpty()) {
            throw new RuntimeException("To currency cannot be empty");
        }
        
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }
        
        // Validate currency codes (basic validation)
        if (from.length() != 3 || to.length() != 3) {
            throw new RuntimeException("Currency codes must be 3 characters long");
        }
    }
    
    // Additional business logic methods
    public BigDecimal calculateFee(CurrencyConversion conversion, BigDecimal feePercentage) {
        BigDecimal amount = conversion.getTotalCalculatedAmount();
        BigDecimal fee = amount.multiply(feePercentage).divide(BigDecimal.valueOf(100));
        return amount.subtract(fee);
    }
    
    public boolean validateConversion(CurrencyConversion conversion) {
        return conversion != null &&
               conversion.getConversionMultiple() != null &&
               conversion.getConversionMultiple().compareTo(BigDecimal.ZERO) > 0 &&
               conversion.getTotalCalculatedAmount() != null &&
               conversion.getTotalCalculatedAmount().compareTo(BigDecimal.ZERO) >= 0;
    }
}