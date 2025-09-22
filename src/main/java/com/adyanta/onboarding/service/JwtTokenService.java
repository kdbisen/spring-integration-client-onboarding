package com.adyanta.onboarding.service;

import com.adyanta.onboarding.model.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JWT Token Service with caching and automatic renewal
 */
@Service
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    @Autowired
    @Qualifier("jwtRestTemplate")
    private RestTemplate restTemplate;

    @Value("${jwt.token.service.url:http://localhost:8080/oauth/token}")
    private String tokenServiceUrl;

    @Value("${jwt.token.client.id:your-client-id}")
    private String clientId;

    @Value("${jwt.token.client.secret:your-client-secret}")
    private String clientSecret;

    @Value("${jwt.token.scope:api.read api.write}")
    private String scope;

    @Value("${jwt.token.grant.type:client_credentials}")
    private String grantType;

    @Value("${jwt.token.cache.renewal.buffer:20}")
    private int renewalBufferSeconds;

    // In-memory cache for tokens
    private final ConcurrentHashMap<String, JwtToken> tokenCache = new ConcurrentHashMap<>();
    
    // Locks for thread-safe token renewal
    private final ConcurrentHashMap<String, ReentrantLock> renewalLocks = new ConcurrentHashMap<>();

    /**
     * Get cached JWT token or fetch new one if needed
     */
    @Cacheable(value = "jwtTokens", key = "#serviceName")
    public JwtToken getToken(String serviceName) {
        logger.info("Getting JWT token for service: {}", serviceName);
        
        JwtToken cachedToken = tokenCache.get(serviceName);
        
        if (cachedToken == null || cachedToken.isExpired()) {
            logger.info("No valid cached token found for service: {}, fetching new token", serviceName);
            return fetchNewToken(serviceName);
        }
        
        if (cachedToken.needsRenewal()) {
            logger.info("Token for service {} needs renewal, triggering async renewal", serviceName);
            renewTokenAsync(serviceName);
        }
        
        logger.info("Returning cached token for service: {}, expires in {} seconds", 
                   serviceName, cachedToken.getSecondsUntilExpiry());
        return cachedToken;
    }

    /**
     * Get bearer token string for HTTP headers
     */
    public String getBearerToken(String serviceName) {
        JwtToken token = getToken(serviceName);
        return token.getBearerToken();
    }

    /**
     * Fetch new token from token service
     */
    public JwtToken fetchNewToken(String serviceName) {
        logger.info("Fetching new JWT token for service: {}", serviceName);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", grantType);
            body.add("scope", scope);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<JwtToken> response = restTemplate.postForEntity(
                tokenServiceUrl, request, JwtToken.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JwtToken token = response.getBody();
                token.setServiceName(serviceName);
                token.setIssuedAt(LocalDateTime.now());
                
                // Calculate expiration times
                if (token.getExpiresIn() != null) {
                    token.setExpiresAt(LocalDateTime.now().plusSeconds(token.getExpiresIn()));
                    token.setCacheExpiresAt(token.getExpiresAt().minusSeconds(renewalBufferSeconds));
                }
                
                // Cache the token
                tokenCache.put(serviceName, token);
                
                logger.info("Successfully fetched and cached token for service: {}, expires in {} seconds", 
                           serviceName, token.getSecondsUntilExpiry());
                return token;
            } else {
                logger.error("Failed to fetch token for service: {}, status: {}", 
                            serviceName, response.getStatusCode());
                throw new RuntimeException("Failed to fetch JWT token");
            }
        } catch (Exception e) {
            logger.error("Error fetching JWT token for service: {}", serviceName, e);
            throw new RuntimeException("Error fetching JWT token", e);
        }
    }

    /**
     * Renew token asynchronously
     */
    @Async
    public void renewTokenAsync(String serviceName) {
        ReentrantLock lock = renewalLocks.computeIfAbsent(serviceName, k -> new ReentrantLock());
        
        if (lock.tryLock()) {
            try {
                logger.info("Starting async token renewal for service: {}", serviceName);
                
                JwtToken currentToken = tokenCache.get(serviceName);
                if (currentToken != null) {
                    currentToken.setRenewing(true);
                }
                
                JwtToken newToken = fetchNewToken(serviceName);
                logger.info("Successfully renewed token for service: {}", serviceName);
                
            } catch (Exception e) {
                logger.error("Failed to renew token for service: {}", serviceName, e);
            } finally {
                lock.unlock();
            }
        } else {
            logger.debug("Token renewal already in progress for service: {}", serviceName);
        }
    }

    /**
     * Force refresh token
     */
    public JwtToken refreshToken(String serviceName) {
        logger.info("Force refreshing token for service: {}", serviceName);
        tokenCache.remove(serviceName);
        return fetchNewToken(serviceName);
    }

    /**
     * Remove token from cache
     */
    public void evictToken(String serviceName) {
        logger.info("Evicting token from cache for service: {}", serviceName);
        tokenCache.remove(serviceName);
    }

    /**
     * Get token cache status
     */
    public JwtToken getTokenStatus(String serviceName) {
        return tokenCache.get(serviceName);
    }

    /**
     * Get all cached tokens status
     */
    public ConcurrentHashMap<String, JwtToken> getAllTokenStatus() {
        return new ConcurrentHashMap<>(tokenCache);
    }

    /**
     * Scheduled task to clean up expired tokens
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupExpiredTokens() {
        logger.debug("Running token cleanup task");
        
        tokenCache.entrySet().removeIf(entry -> {
            JwtToken token = entry.getValue();
            if (token.isExpired()) {
                logger.info("Removing expired token for service: {}", entry.getKey());
                return true;
            }
            return false;
        });
        
        // Clean up unused locks
        renewalLocks.entrySet().removeIf(entry -> 
            !tokenCache.containsKey(entry.getKey()) && !entry.getValue().isLocked());
    }

    /**
     * Scheduled task to proactively renew tokens
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void proactiveTokenRenewal() {
        logger.debug("Running proactive token renewal task");
        
        tokenCache.forEach((serviceName, token) -> {
            if (token.needsRenewal() && !token.isRenewing()) {
                logger.info("Proactively renewing token for service: {}", serviceName);
                renewTokenAsync(serviceName);
            }
        });
    }

    /**
     * Health check for token service
     */
    public boolean isTokenServiceHealthy() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                tokenServiceUrl, HttpMethod.HEAD, request, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("Token service health check failed", e);
            return false;
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        int totalTokens = tokenCache.size();
        int expiredTokens = (int) tokenCache.values().stream().filter(JwtToken::isExpired).count();
        int renewingTokens = (int) tokenCache.values().stream().filter(JwtToken::isRenewing).count();
        
        return new CacheStatistics(totalTokens, expiredTokens, renewingTokens);
    }

    /**
     * Cache statistics inner class
     */
    public static class CacheStatistics {
        private final int totalTokens;
        private final int expiredTokens;
        private final int renewingTokens;
        
        public CacheStatistics(int totalTokens, int expiredTokens, int renewingTokens) {
            this.totalTokens = totalTokens;
            this.expiredTokens = expiredTokens;
            this.renewingTokens = renewingTokens;
        }
        
        public int getTotalTokens() { return totalTokens; }
        public int getExpiredTokens() { return expiredTokens; }
        public int getRenewingTokens() { return renewingTokens; }
        public int getActiveTokens() { return totalTokens - expiredTokens - renewingTokens; }
    }
}
