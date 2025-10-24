package com.sathya.conversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;  // ← Feign for service calls
import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // ← You might want to add this
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@SpringBootApplication
@EnableFeignClients  // ← This enables Feign clients to work with Eureka
// @EnableDiscoveryClient ← Optional (auto-enabled by spring-cloud-starter)
public class CurrencyConversionApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CurrencyConversionApplication.class, args);
    }
    
    @Bean
    @LoadBalanced  // ← This enables Eureka service discovery for RestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    @LoadBalanced  // ← This enables Eureka service discovery for WebClient
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}