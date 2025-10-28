package com.sathya.gateway.controller;

import com.sathya.gateway.model.AuthRequest;
import com.sathya.gateway.model.AuthResponse;
import com.sathya.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Simple in-memory user store for demo (Replace with database in production)
    private static final Map<String, String> USERS = new HashMap<>();
    
    static {
        USERS.put("admin", "admin123");
        USERS.put("user", "user123");
        USERS.put("test", "test123");
    }
    
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        logger.info("🔐 Login attempt for user: {}", authRequest.getUsername());
        
        // Validate credentials
        String storedPassword = USERS.get(authRequest.getUsername());
        
        if (storedPassword == null || !storedPassword.equals(authRequest.getPassword())) {
            logger.warn("❌ Invalid credentials for user: {}", authRequest.getUsername());
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid username or password", null)));
        }
        
        try {
            // Generate JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "USER"); // Add role or other claims as needed
            
            String token = jwtUtil.generateToken(authRequest.getUsername(), claims);
            
            logger.info("✅ Token generated successfully for user: {}", authRequest.getUsername());
            
            AuthResponse response = new AuthResponse(
                    token,
                    "Authentication successful",
                    authRequest.getUsername()
            );
            
            return Mono.just(ResponseEntity.ok(response));
            
        } catch (Exception e) {
            logger.error("❌ Token generation failed: {}", e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Token generation failed", null)));
        }
    }
    
    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, String>>> register(@Valid @RequestBody AuthRequest authRequest) {
        logger.info("📝 Registration attempt for user: {}", authRequest.getUsername());
        
        // Check if user already exists
        if (USERS.containsKey(authRequest.getUsername())) {
            logger.warn("❌ User already exists: {}", authRequest.getUsername());
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "User already exists")));
        }
        
        // Register new user
        USERS.put(authRequest.getUsername(), authRequest.getPassword());
        logger.info("✅ User registered successfully: {}", authRequest.getUsername());
        
        return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully")));
    }
    
    @GetMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("🔍 Token validation request");
        
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        
        if (token == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("valid", false, "message", "Invalid token format")));
        }
        
        boolean isValid = jwtUtil.validateToken(token);
        
        if (isValid) {
            String username = jwtUtil.extractUsername(token);
            logger.info("✅ Token valid for user: {}", username);
            return Mono.just(ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "message", "Token is valid"
            )));
        } else {
            logger.warn("❌ Invalid token");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token is invalid or expired")));
        }
    }
    
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Authentication Service"
        )));
    }
}