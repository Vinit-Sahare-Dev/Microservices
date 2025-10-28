package com.sathya.gateway.filter;

import com.sathya.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthenticationFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            logger.debug("🔍 Processing request: {} {}", request.getMethod(), request.getURI());
            
            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn("❌ Missing Authorization header for: {}", request.getURI());
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            // Extract token from header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            if (token == null) {
                logger.warn("❌ Invalid Authorization header format for: {}", request.getURI());
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }
            
            // Validate token
            try {
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("❌ Invalid JWT token for: {}", request.getURI());
                    return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
                }
                
                // Extract username and add to request headers
                String username = jwtUtil.extractUsername(token);
                logger.debug("✅ Token validated for user: {}", username);
                
                // Add username to request headers for downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", username)
                        .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (Exception e) {
                logger.error("❌ Token validation error: {}", e.getMessage());
                return onError(exchange, "JWT validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorJson = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
                                        status.getReasonPhrase(), 
                                        message);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));
    }
    
    public static class Config {
        // Configuration properties can be added here if needed
    }
}