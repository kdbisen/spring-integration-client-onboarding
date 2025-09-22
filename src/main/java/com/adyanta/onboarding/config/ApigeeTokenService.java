package com.adyanta.onboarding.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Apigee Token Service
 * Handles OAuth2 token management for Apigee integration
 */
@Service
public class ApigeeTokenService {

    private static final Logger logger = LoggerFactory.getLogger(ApigeeTokenService.class);

    private final String apigeeBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final RestTemplate restTemplate;
    
    private String accessToken;
    private LocalDateTime tokenExpiry;
    private final Object tokenLock = new Object();

    public ApigeeTokenService(String apigeeBaseUrl, String clientId, String clientSecret) {
        this.apigeeBaseUrl = apigeeBaseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get valid access token, refreshing if necessary
     */
    public String getValidAccessToken() {
        synchronized (tokenLock) {
            if (accessToken == null || isTokenExpired()) {
                refreshAccessToken();
            }
            return accessToken;
        }
    }

    /**
     * Refresh the access token
     */
    private void refreshAccessToken() {
        try {
            logger.info("Refreshing Apigee access token");
            
            String tokenUrl = apigeeBaseUrl + "/oauth2/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("scope", "read write");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> tokenResponse = response.getBody();
                accessToken = (String) tokenResponse.get("access_token");
                
                // Calculate token expiry (default to 1 hour if not provided)
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                if (expiresIn != null) {
                    tokenExpiry = LocalDateTime.now().plusSeconds(expiresIn - 60); // Refresh 1 minute early
                } else {
                    tokenExpiry = LocalDateTime.now().plusHours(1);
                }
                
                logger.info("Apigee access token refreshed successfully, expires at: {}", tokenExpiry);
                
            } else {
                logger.error("Failed to refresh Apigee access token: {}", response.getStatusCode());
                throw new RuntimeException("Failed to refresh Apigee access token");
            }
            
        } catch (Exception e) {
            logger.error("Error refreshing Apigee access token", e);
            throw new RuntimeException("Error refreshing Apigee access token", e);
        }
    }

    /**
     * Check if current token is expired
     */
    private boolean isTokenExpired() {
        return tokenExpiry == null || LocalDateTime.now().isAfter(tokenExpiry);
    }

    /**
     * Validate token with Apigee
     */
    public boolean validateToken(String token) {
        try {
            String validateUrl = apigeeBaseUrl + "/oauth2/tokeninfo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                validateUrl,
                HttpMethod.GET,
                request,
                Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            logger.error("Error validating Apigee token", e);
            return false;
        }
    }

    /**
     * Revoke current token
     */
    public void revokeToken() {
        synchronized (tokenLock) {
            if (accessToken != null) {
                try {
                    String revokeUrl = apigeeBaseUrl + "/oauth2/revoke";
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                    body.add("token", accessToken);
                    body.add("client_id", clientId);
                    body.add("client_secret", clientSecret);
                    
                    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
                    
                    restTemplate.exchange(
                        revokeUrl,
                        HttpMethod.POST,
                        request,
                        Map.class
                    );
                    
                    logger.info("Apigee access token revoked successfully");
                    
                } catch (Exception e) {
                    logger.error("Error revoking Apigee token", e);
                } finally {
                    accessToken = null;
                    tokenExpiry = null;
                }
            }
        }
    }
}
