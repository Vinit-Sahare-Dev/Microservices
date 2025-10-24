package com.sathya.conversion.model;

import java.math.BigDecimal;

public class CurrencyConversion {
    private Long id;
    private String from;
    private String to;
    private BigDecimal quantity;
    private BigDecimal exchangeRate;
    private BigDecimal totalCalculatedAmount;
    private String environment;
    private String clientUsed;

    // Constructors
    public CurrencyConversion() {}

    public CurrencyConversion(Long id, String from, String to, BigDecimal quantity, 
                             BigDecimal exchangeRate, BigDecimal totalCalculatedAmount, 
                             String environment, String clientUsed) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.quantity = quantity;
        this.exchangeRate = exchangeRate;
        this.totalCalculatedAmount = totalCalculatedAmount;
        this.environment = environment;
        this.clientUsed = clientUsed;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public BigDecimal getTotalCalculatedAmount() { return totalCalculatedAmount; }
    public void setTotalCalculatedAmount(BigDecimal totalCalculatedAmount) { this.totalCalculatedAmount = totalCalculatedAmount; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getClientUsed() { return clientUsed; }
    public void setClientUsed(String clientUsed) { this.clientUsed = clientUsed; }
}