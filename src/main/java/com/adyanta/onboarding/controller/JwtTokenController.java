package com.adyanta.onboarding.controller;

import com.adyanta.onboarding.model.JwtToken;
import com.adyanta.onboarding.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT Token Management Controller
 * Provides endpoints for JWT token management and monitoring
 */
@RestController
@RequestMapping("/api/v1/jwt")
@Tag(name = "JWT Token Management", description = "JWT token caching, renewal, and monitoring")
public class JwtTokenController {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenController.class);

    @Autowired
    private JwtTokenService jwtTokenService;

    /**
     * Get JWT token for a service
     */
    @Operation(
            summary = "Get JWT Token",
            description = "Gets cached JWT token for a service or fetches new one if needed"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to get token")
    })
    @GetMapping("/token/{serviceName}")
    public ResponseEntity<Map<String, Object>> getToken(
            @Parameter(description = "Service name", example = "kyc")
            @PathVariable String serviceName) {
        
        logger.info("Getting JWT token for service: {}", serviceName);
        
        try {
            JwtToken token = jwtTokenService.getToken(serviceName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", serviceName);
            response.put("accessToken", token.getAccessToken());
            response.put("tokenType", token.getTokenType());
            response.put("expiresIn", token.getExpiresIn());
            response.put("scope", token.getScope());
            response.put("issuedAt", token.getIssuedAt());
            response.put("expiresAt", token.getExpiresAt());
            response.put("cacheExpiresAt", token.getCacheExpiresAt());
            response.put("secondsUntilExpiry", token.getSecondsUntilExpiry());
            response.put("secondsUntilCacheExpiry", token.getSecondsUntilCacheExpiry());
            response.put("needsRenewal", token.needsRenewal());
            response.put("isRenewing", token.isRenewing());
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Successfully retrieved token for service: {}", serviceName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get token for service: {}", serviceName, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("serviceName", serviceName);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get bearer token string for a service
     */
    @Operation(
            summary = "Get Bearer Token",
            description = "Gets bearer token string for HTTP headers"
    )
    @GetMapping("/bearer/{serviceName}")
    public ResponseEntity<Map<String, Object>> getBearerToken(
            @Parameter(description = "Service name", example = "kyc")
            @PathVariable String serviceName) {
        
        logger.info("Getting bearer token for service: {}", serviceName);
        
        try {
            String bearerToken = jwtTokenService.getBearerToken(serviceName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", serviceName);
            response.put("bearerToken", bearerToken);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Successfully retrieved bearer token for service: {}", serviceName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get bearer token for service: {}", serviceName, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("serviceName", serviceName);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Refresh JWT token for a service
     */
    @Operation(
            summary = "Refresh JWT Token",
            description = "Force refresh JWT token for a service"
    )
    @PostMapping("/refresh/{serviceName}")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Parameter(description = "Service name", example = "kyc")
            @PathVariable String serviceName) {
        
        logger.info("Refreshing JWT token for service: {}", serviceName);
        
        try {
            JwtToken token = jwtTokenService.refreshToken(serviceName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", serviceName);
            response.put("message", "Token refreshed successfully");
            response.put("accessToken", token.getAccessToken());
            response.put("tokenType", token.getTokenType());
            response.put("expiresIn", token.getExpiresIn());
            response.put("issuedAt", token.getIssuedAt());
            response.put("expiresAt", token.getExpiresAt());
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Successfully refreshed token for service: {}", serviceName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to refresh token for service: {}", serviceName, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("serviceName", serviceName);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Evict JWT token from cache
     */
    @Operation(
            summary = "Evict JWT Token",
            description = "Remove JWT token from cache"
    )
    @DeleteMapping("/evict/{serviceName}")
    public ResponseEntity<Map<String, Object>> evictToken(
            @Parameter(description = "Service name", example = "kyc")
            @PathVariable String serviceName) {
        
        logger.info("Evicting JWT token for service: {}", serviceName);
        
        jwtTokenService.evictToken(serviceName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", serviceName);
        response.put("message", "Token evicted from cache");
        response.put("timestamp", LocalDateTime.now());
        
        logger.info("Successfully evicted token for service: {}", serviceName);
        return ResponseEntity.ok(response);
    }

    /**
     * Get token status for a service
     */
    @Operation(
            summary = "Get Token Status",
            description = "Get detailed token status for a service"
    )
    @GetMapping("/status/{serviceName}")
    public ResponseEntity<Map<String, Object>> getTokenStatus(
            @Parameter(description = "Service name", example = "kyc")
            @PathVariable String serviceName) {
        
        logger.info("Getting token status for service: {}", serviceName);
        
        JwtToken token = jwtTokenService.getTokenStatus(serviceName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", serviceName);
        response.put("timestamp", LocalDateTime.now());
        
        if (token != null) {
            response.put("exists", true);
            response.put("tokenType", token.getTokenType());
            response.put("expiresIn", token.getExpiresIn());
            response.put("issuedAt", token.getIssuedAt());
            response.put("expiresAt", token.getExpiresAt());
            response.put("cacheExpiresAt", token.getCacheExpiresAt());
            response.put("secondsUntilExpiry", token.getSecondsUntilExpiry());
            response.put("secondsUntilCacheExpiry", token.getSecondsUntilCacheExpiry());
            response.put("isExpired", token.isExpired());
            response.put("isCacheExpired", token.isCacheExpired());
            response.put("needsRenewal", token.needsRenewal());
            response.put("isRenewing", token.isRenewing());
        } else {
            response.put("exists", false);
            response.put("message", "No token found in cache");
        }
        
        logger.info("Successfully retrieved token status for service: {}", serviceName);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all token statuses
     */
    @Operation(
            summary = "Get All Token Statuses",
            description = "Get status of all cached tokens"
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllTokenStatuses() {
        logger.info("Getting all token statuses");
        
        ConcurrentHashMap<String, JwtToken> allTokens = jwtTokenService.getAllTokenStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("totalTokens", allTokens.size());
        
        Map<String, Object> tokenStatuses = new HashMap<>();
        allTokens.forEach((serviceName, token) -> {
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("tokenType", token.getTokenType());
            tokenInfo.put("expiresIn", token.getExpiresIn());
            tokenInfo.put("issuedAt", token.getIssuedAt());
            tokenInfo.put("expiresAt", token.getExpiresAt());
            tokenInfo.put("cacheExpiresAt", token.getCacheExpiresAt());
            tokenInfo.put("secondsUntilExpiry", token.getSecondsUntilExpiry());
            tokenInfo.put("secondsUntilCacheExpiry", token.getSecondsUntilCacheExpiry());
            tokenInfo.put("isExpired", token.isExpired());
            tokenInfo.put("isCacheExpired", token.isCacheExpired());
            tokenInfo.put("needsRenewal", token.needsRenewal());
            tokenInfo.put("isRenewing", token.isRenewing());
            
            tokenStatuses.put(serviceName, tokenInfo);
        });
        
        response.put("tokens", tokenStatuses);
        
        logger.info("Successfully retrieved all token statuses");
        return ResponseEntity.ok(response);
    }

    /**
     * Get cache statistics
     */
    @Operation(
            summary = "Get Cache Statistics",
            description = "Get JWT token cache statistics"
    )
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        logger.info("Getting cache statistics");
        
        JwtTokenService.CacheStatistics stats = jwtTokenService.getCacheStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("totalTokens", stats.getTotalTokens());
        response.put("activeTokens", stats.getActiveTokens());
        response.put("expiredTokens", stats.getExpiredTokens());
        response.put("renewingTokens", stats.getRenewingTokens());
        
        logger.info("Successfully retrieved cache statistics");
        return ResponseEntity.ok(response);
    }

    /**
     * Check token service health
     */
    @Operation(
            summary = "Check Token Service Health",
            description = "Check if the JWT token service is healthy"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkTokenServiceHealth() {
        logger.info("Checking token service health");
        
        boolean isHealthy = jwtTokenService.isTokenServiceHealthy();
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("healthy", isHealthy);
        response.put("status", isHealthy ? "UP" : "DOWN");
        
        logger.info("Token service health check: {}", isHealthy ? "UP" : "DOWN");
        return ResponseEntity.ok(response);
    }
}
