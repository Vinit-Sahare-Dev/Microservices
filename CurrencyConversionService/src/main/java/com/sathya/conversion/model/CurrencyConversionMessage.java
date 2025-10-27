package com.sathya.conversion.model;

import java.math.BigDecimal;

public class CurrencyConversionMessage {
    private Long conversionId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private String status;
    
    // Constructors
    public CurrencyConversionMessage() {}
    
    public CurrencyConversionMessage(Long conversionId, String fromCurrency, String toCurrency, 
                                   BigDecimal amount, BigDecimal convertedAmount, 
                                   BigDecimal exchangeRate, String status) {
        this.conversionId = conversionId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.exchangeRate = exchangeRate;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getConversionId() { return conversionId; }
    public void setConversionId(Long conversionId) { this.conversionId = conversionId; }
    
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }
    
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "CurrencyConversionMessage{" +
                "conversionId=" + conversionId +
                ", fromCurrency='" + fromCurrency + '\'' +
                ", toCurrency='" + toCurrency + '\'' +
                ", amount=" + amount +
                ", convertedAmount=" + convertedAmount +
                ", exchangeRate=" + exchangeRate +
                ", status='" + status + '\'' +
                '}';
    }
}