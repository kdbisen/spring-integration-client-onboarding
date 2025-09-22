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
 * LES (Legal Entity System) Service
 * Integrates with external legal entity verification and compliance systems
 */
@Service
public class LesService {

    private static final Logger logger = LoggerFactory.getLogger(LesService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${les.service.url:http://localhost:8084/les/verify}")
    private String lesServiceUrl;
    
    @Value("${les.service.timeout:30000}")
    private int lesServiceTimeout;

    public LesService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Verifies client information with LES system
     */
    public Map<String, Object> verifyWithLes(ClientOnboardingRequest request) {
        logger.info("Starting LES verification for client: {}", request.getClientId());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Prepare LES request
            Map<String, Object> lesRequest = prepareLesRequest(request);
            
            // Step 2: Call LES service
            ResponseEntity<Map> response = callLesService(lesRequest);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> lesResponse = response.getBody();
                
                // Step 3: Process LES response
                result = processLesResponse(lesResponse, request);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("LES verification completed for client: {} in {}ms", 
                    request.getClientId(), duration);
                
            } else {
                logger.error("LES service returned error status: {}", response.getStatusCode());
                result.put("status", "FAILED");
                result.put("message", "LES service returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("LES verification failed for client: {}", request.getClientId(), e);
            result.put("status", "FAILED");
            result.put("message", "LES verification failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "LES");
        result.put("clientId", request.getClientId());
        result.put("correlationId", request.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    private Map<String, Object> prepareLesRequest(ClientOnboardingRequest request) {
        Map<String, Object> lesRequest = new HashMap<>();
        
        // Basic client information
        lesRequest.put("clientId", request.getClientId());
        lesRequest.put("firstName", request.getFirstName());
        lesRequest.put("lastName", request.getLastName());
        lesRequest.put("email", request.getEmail());
        lesRequest.put("phoneNumber", request.getPhoneNumber());
        lesRequest.put("documentType", request.getDocumentType());
        lesRequest.put("documentNumber", request.getDocumentNumber());
        lesRequest.put("address", request.getAddress());
        lesRequest.put("correlationId", request.getCorrelationId());
        lesRequest.put("requestTimestamp", LocalDateTime.now());
        
        // LES specific fields
        lesRequest.put("verificationType", "FULL_VERIFICATION");
        lesRequest.put("priority", "NORMAL");
        lesRequest.put("source", "CLIENT_ONBOARDING");
        
        return lesRequest;
    }

    private ResponseEntity<Map> callLesService(Map<String, Object> lesRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + getLesServiceToken());
        headers.set("X-Correlation-ID", (String) lesRequest.get("correlationId"));
        headers.set("X-Client-ID", (String) lesRequest.get("clientId"));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(lesRequest, headers);
        
        logger.debug("Calling LES service at: {}", lesServiceUrl);
        
        return restTemplate.exchange(
            lesServiceUrl,
            HttpMethod.POST,
            entity,
            Map.class
        );
    }

    private Map<String, Object> processLesResponse(Map<String, Object> lesResponse, 
                                                   ClientOnboardingRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (lesResponse != null) {
            String status = (String) lesResponse.get("status");
            String message = (String) lesResponse.get("message");
            
            result.put("status", status);
            result.put("message", message);
            
            // LES verification results
            result.put("entityVerified", lesResponse.get("entityVerified"));
            result.put("entityType", lesResponse.get("entityType"));
            result.put("entityStatus", lesResponse.get("entityStatus"));
            result.put("registrationNumber", lesResponse.get("registrationNumber"));
            result.put("registrationDate", lesResponse.get("registrationDate"));
            result.put("registrationCountry", lesResponse.get("registrationCountry"));
            
            // Compliance checks
            result.put("amlCheck", lesResponse.get("amlCheck"));
            result.put("sanctionsCheck", lesResponse.get("sanctionsCheck"));
            result.put("pepCheck", lesResponse.get("pepCheck"));
            result.put("adverseMediaCheck", lesResponse.get("adverseMediaCheck"));
            
            // Risk assessment
            result.put("riskScore", lesResponse.get("riskScore"));
            result.put("riskLevel", lesResponse.get("riskLevel"));
            result.put("riskFactors", lesResponse.get("riskFactors"));
            
            // Legal entity information
            result.put("legalName", lesResponse.get("legalName"));
            result.put("tradingName", lesResponse.get("tradingName"));
            result.put("businessType", lesResponse.get("businessType"));
            result.put("industrySector", lesResponse.get("industrySector"));
            result.put("incorporationDate", lesResponse.get("incorporationDate"));
            result.put("authorizedCapital", lesResponse.get("authorizedCapital"));
            result.put("paidUpCapital", lesResponse.get("paidUpCapital"));
            
            // Directors and shareholders
            result.put("directors", lesResponse.get("directors"));
            result.put("shareholders", lesResponse.get("shareholders"));
            result.put("beneficialOwners", lesResponse.get("beneficialOwners"));
            
            // Regulatory information
            result.put("regulatoryStatus", lesResponse.get("regulatoryStatus"));
            result.put("licenses", lesResponse.get("licenses"));
            result.put("regulatoryBodies", lesResponse.get("regulatoryBodies"));
            
            // Financial information
            result.put("annualRevenue", lesResponse.get("annualRevenue"));
            result.put("numberOfEmployees", lesResponse.get("numberOfEmployees"));
            result.put("creditRating", lesResponse.get("creditRating"));
            
            // Additional metadata
            result.put("verificationDate", lesResponse.get("verificationDate"));
            result.put("verificationExpiry", lesResponse.get("verificationExpiry"));
            result.put("dataSource", lesResponse.get("dataSource"));
            result.put("confidenceScore", lesResponse.get("confidenceScore"));
            
        } else {
            result.put("status", "FAILED");
            result.put("message", "Empty response from LES service");
        }
        
        return result;
    }

    /**
     * Check LES status for existing client
     */
    public Map<String, Object> checkLesStatus(String clientId) {
        logger.info("Checking LES status for client: {}", clientId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getLesServiceToken());
            headers.set("X-Client-ID", clientId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                lesServiceUrl + "/status/" + clientId,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to get LES status: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to get LES status");
            }
            
        } catch (Exception e) {
            logger.error("Error checking LES status for client: {}", clientId, e);
            return Map.of("status", "ERROR", "message", "Error checking LES status: " + e.getMessage());
        }
    }

    /**
     * Update LES information
     */
    public Map<String, Object> updateLesInformation(String clientId, Map<String, Object> updateData) {
        logger.info("Updating LES information for client: {}", clientId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getLesServiceToken());
            headers.set("X-Client-ID", clientId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateData, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                lesServiceUrl + "/update/" + clientId,
                HttpMethod.PUT,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to update LES information: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to update LES information");
            }
            
        } catch (Exception e) {
            logger.error("Error updating LES information for client: {}", clientId, e);
            return Map.of("status", "ERROR", "message", "Error updating LES information: " + e.getMessage());
        }
    }

    /**
     * Perform enhanced due diligence
     */
    public Map<String, Object> performEnhancedDueDiligence(String clientId, String diligenceType) {
        logger.info("Performing enhanced due diligence for client: {}, type: {}", clientId, diligenceType);
        
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("clientId", clientId);
            request.put("diligenceType", diligenceType);
            request.put("requestTimestamp", LocalDateTime.now());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getLesServiceToken());
            headers.set("X-Client-ID", clientId);
            headers.set("X-Diligence-Type", diligenceType);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                lesServiceUrl + "/enhanced-due-diligence",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to perform enhanced due diligence: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to perform enhanced due diligence");
            }
            
        } catch (Exception e) {
            logger.error("Error performing enhanced due diligence for client: {}", clientId, e);
            return Map.of("status", "ERROR", "message", "Error performing enhanced due diligence: " + e.getMessage());
        }
    }

    /**
     * Get compliance report
     */
    public Map<String, Object> getComplianceReport(String clientId) {
        logger.info("Getting compliance report for client: {}", clientId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getLesServiceToken());
            headers.set("X-Client-ID", clientId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                lesServiceUrl + "/compliance-report/" + clientId,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to get compliance report: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to get compliance report");
            }
            
        } catch (Exception e) {
            logger.error("Error getting compliance report for client: {}", clientId, e);
            return Map.of("status", "ERROR", "message", "Error getting compliance report: " + e.getMessage());
        }
    }

    private String getLesServiceToken() {
        // In real implementation, this would get JWT token from Apigee or OAuth2 service
        return "mock-les-token-" + System.currentTimeMillis();
    }
}
