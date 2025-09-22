package com.adyanta.onboarding.service;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * KYC (Know Your Customer) Service
 * Integrates with external KYC verification service
 */
@Service
public class KycService {

    private static final Logger logger = LoggerFactory.getLogger(KycService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${kyc.service.url:http://localhost:8081/kyc/verify}")
    private String kycServiceUrl;
    
    @Value("${kyc.service.timeout:30000}")
    private int kycServiceTimeout;

    public KycService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Verifies client information with KYC service
     */
    public Map<String, Object> verifyClient(ClientOnboardingRequest request) {
        logger.info("Starting KYC verification for client: {}", request.getClientId());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Prepare KYC request
            Map<String, Object> kycRequest = prepareKycRequest(request);
            
            // Call external KYC service
            ResponseEntity<Map> response = callKycService(kycRequest);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> kycResponse = response.getBody();
                
                // Process KYC response
                result = processKycResponse(kycResponse, request);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("KYC verification completed for client: {} in {}ms", 
                    request.getClientId(), duration);
                
            } else {
                logger.error("KYC service returned error status: {}", response.getStatusCode());
                result.put("status", "FAILED");
                result.put("message", "KYC service returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("KYC verification failed for client: {}", request.getClientId(), e);
            result.put("status", "FAILED");
            result.put("message", "KYC verification failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "KYC");
        result.put("clientId", request.getClientId());
        result.put("correlationId", request.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    private Map<String, Object> prepareKycRequest(ClientOnboardingRequest request) {
        Map<String, Object> kycRequest = new HashMap<>();
        
        kycRequest.put("clientId", request.getClientId());
        kycRequest.put("firstName", request.getFirstName());
        kycRequest.put("lastName", request.getLastName());
        kycRequest.put("email", request.getEmail());
        kycRequest.put("phoneNumber", request.getPhoneNumber());
        kycRequest.put("documentType", request.getDocumentType());
        kycRequest.put("documentNumber", request.getDocumentNumber());
        kycRequest.put("address", request.getAddress());
        kycRequest.put("correlationId", request.getCorrelationId());
        kycRequest.put("requestTimestamp", LocalDateTime.now());
        
        return kycRequest;
    }

    private ResponseEntity<Map> callKycService(Map<String, Object> kycRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + getKycServiceToken());
        headers.set("X-Correlation-ID", (String) kycRequest.get("correlationId"));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(kycRequest, headers);
        
        logger.debug("Calling KYC service at: {}", kycServiceUrl);
        
        return restTemplate.exchange(
            kycServiceUrl,
            HttpMethod.POST,
            entity,
            Map.class
        );
    }

    private Map<String, Object> processKycResponse(Map<String, Object> kycResponse, 
                                                   ClientOnboardingRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (kycResponse != null) {
            String status = (String) kycResponse.get("status");
            String message = (String) kycResponse.get("message");
            
            result.put("status", status);
            result.put("message", message);
            result.put("kycScore", kycResponse.get("kycScore"));
            result.put("riskLevel", kycResponse.get("riskLevel"));
            result.put("verificationLevel", kycResponse.get("verificationLevel"));
            result.put("documentsVerified", kycResponse.get("documentsVerified"));
            result.put("biometricVerified", kycResponse.get("biometricVerified"));
            result.put("sanctionsCheck", kycResponse.get("sanctionsCheck"));
            result.put("pepCheck", kycResponse.get("pepCheck"));
            
            // Additional KYC specific data
            result.put("kycReferenceId", kycResponse.get("kycReferenceId"));
            result.put("verificationDate", kycResponse.get("verificationDate"));
            result.put("expiryDate", kycResponse.get("expiryDate"));
            
        } else {
            result.put("status", "FAILED");
            result.put("message", "Empty response from KYC service");
        }
        
        return result;
    }

    private String getKycServiceToken() {
        // In real implementation, this would get JWT token from Apigee or OAuth2 service
        // For demo purposes, returning a mock token
        return "mock-kyc-token-" + System.currentTimeMillis();
    }

    /**
     * Check KYC status for existing client
     */
    public Map<String, Object> checkKycStatus(String clientId) {
        logger.info("Checking KYC status for client: {}", clientId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getKycServiceToken());
            headers.set("X-Client-ID", clientId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                kycServiceUrl + "/status/" + clientId,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to get KYC status: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to get KYC status");
            }
            
        } catch (Exception e) {
            logger.error("Error checking KYC status for client: {}", clientId, e);
            return Map.of("status", "ERROR", "message", "Error checking KYC status: " + e.getMessage());
        }
    }

    /**
     * Update KYC information
     */
    public Map<String, Object> updateKycInformation(String clientId, Map<String, Object> updateData) {
        logger.info("Updating KYC information for client: {}", clientId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getKycServiceToken());
            headers.set("X-Client-ID", clientId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateData, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                kycServiceUrl + "/update/" + clientId,
                HttpMethod.PUT,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to update KYC information: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to update KYC information");
            }
            
        } catch (Exception e) {
            logger.error("Error updating KYC information for client: {}", clientId, e);
            return Map.of("status", "ERROR", "message", "Error updating KYC information: " + e.getMessage());
        }
    }
}
