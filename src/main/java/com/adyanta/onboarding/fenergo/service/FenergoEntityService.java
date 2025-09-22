package com.adyanta.onboarding.fenergo.service;

import com.adyanta.onboarding.fenergo.model.FenergoEntity;
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
 * Fenergo Entity Service
 * Handles entity creation, updates, and management in Fenergo
 */
@Service
public class FenergoEntityService {

    private static final Logger logger = LoggerFactory.getLogger(FenergoEntityService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${fenergo.api.base.url:https://your-fenergo-instance.fenergo.com/api/v1}")
    private String fenergoApiBaseUrl;
    
    @Value("${fenergo.api.timeout:30000}")
    private int fenergoApiTimeout;

    public FenergoEntityService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Create a new entity in Fenergo
     */
    public Map<String, Object> createEntity(FenergoEntity entity) {
        logger.info("Creating Fenergo entity: {}", entity.getEntityName());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Prepare Fenergo API request
            Map<String, Object> fenergoRequest = prepareEntityRequest(entity);
            
            // Call Fenergo API
            ResponseEntity<Map> response = callFenergoEntityApi(fenergoRequest, HttpMethod.POST, "/entities");
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> fenergoResponse = response.getBody();
                
                // Process Fenergo response
                result = processEntityResponse(fenergoResponse, entity);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Fenergo entity created successfully: {} in {}ms", 
                    entity.getEntityName(), duration);
                
            } else {
                logger.error("Fenergo API returned error status: {}", response.getStatusCode());
                result.put("status", "FAILED");
                result.put("message", "Fenergo API returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Fenergo entity creation failed: {}", entity.getEntityName(), e);
            result.put("status", "FAILED");
            result.put("message", "Fenergo entity creation failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "FENERGO_ENTITY");
        result.put("entityName", entity.getEntityName());
        result.put("correlationId", entity.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    /**
     * Update an existing entity in Fenergo
     */
    public Map<String, Object> updateEntity(String entityId, FenergoEntity entity) {
        logger.info("Updating Fenergo entity: {}", entityId);
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Prepare Fenergo API request
            Map<String, Object> fenergoRequest = prepareEntityRequest(entity);
            
            // Call Fenergo API
            ResponseEntity<Map> response = callFenergoEntityApi(fenergoRequest, HttpMethod.PUT, "/entities/" + entityId);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> fenergoResponse = response.getBody();
                
                // Process Fenergo response
                result = processEntityResponse(fenergoResponse, entity);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Fenergo entity updated successfully: {} in {}ms", 
                    entityId, duration);
                
            } else {
                logger.error("Fenergo API returned error status: {}", response.getStatusCode());
                result.put("status", "FAILED");
                result.put("message", "Fenergo API returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Fenergo entity update failed: {}", entityId, e);
            result.put("status", "FAILED");
            result.put("message", "Fenergo entity update failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "FENERGO_ENTITY");
        result.put("entityId", entityId);
        result.put("correlationId", entity.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    /**
     * Get entity details from Fenergo
     */
    public Map<String, Object> getEntity(String entityId) {
        logger.info("Getting Fenergo entity: {}", entityId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getFenergoToken());
            headers.set("X-Entity-ID", entityId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                fenergoApiBaseUrl + "/entities/" + entityId,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to get Fenergo entity: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to get Fenergo entity");
            }
            
        } catch (Exception e) {
            logger.error("Error getting Fenergo entity: {}", entityId, e);
            return Map.of("status", "ERROR", "message", "Error getting Fenergo entity: " + e.getMessage());
        }
    }

    /**
     * Delete an entity from Fenergo
     */
    public Map<String, Object> deleteEntity(String entityId) {
        logger.info("Deleting Fenergo entity: {}", entityId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getFenergoToken());
            headers.set("X-Entity-ID", entityId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                fenergoApiBaseUrl + "/entities/" + entityId,
                HttpMethod.DELETE,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to delete Fenergo entity: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to delete Fenergo entity");
            }
            
        } catch (Exception e) {
            logger.error("Error deleting Fenergo entity: {}", entityId, e);
            return Map.of("status", "ERROR", "message", "Error deleting Fenergo entity: " + e.getMessage());
        }
    }

    private Map<String, Object> prepareEntityRequest(FenergoEntity entity) {
        Map<String, Object> fenergoRequest = new HashMap<>();
        
        // Basic entity information
        fenergoRequest.put("entityName", entity.getEntityName());
        fenergoRequest.put("entityType", entity.getEntityType());
        fenergoRequest.put("status", entity.getStatus());
        fenergoRequest.put("version", entity.getVersion());
        
        // Personal information
        if (entity.getPersonalInformation() != null) {
            Map<String, Object> personalInfo = new HashMap<>();
            personalInfo.put("firstName", entity.getPersonalInformation().getFirstName());
            personalInfo.put("lastName", entity.getPersonalInformation().getLastName());
            personalInfo.put("middleName", entity.getPersonalInformation().getMiddleName());
            personalInfo.put("dateOfBirth", entity.getPersonalInformation().getDateOfBirth());
            personalInfo.put("gender", entity.getPersonalInformation().getGender());
            personalInfo.put("nationality", entity.getPersonalInformation().getNationality());
            personalInfo.put("maritalStatus", entity.getPersonalInformation().getMaritalStatus());
            personalInfo.put("occupation", entity.getPersonalInformation().getOccupation());
            fenergoRequest.put("personalInformation", personalInfo);
        }
        
        // Corporate information
        if (entity.getCorporateInformation() != null) {
            Map<String, Object> corporateInfo = new HashMap<>();
            corporateInfo.put("legalName", entity.getCorporateInformation().getLegalName());
            corporateInfo.put("tradingName", entity.getCorporateInformation().getTradingName());
            corporateInfo.put("registrationNumber", entity.getCorporateInformation().getRegistrationNumber());
            corporateInfo.put("incorporationDate", entity.getCorporateInformation().getIncorporationDate());
            corporateInfo.put("incorporationCountry", entity.getCorporateInformation().getIncorporationCountry());
            corporateInfo.put("businessType", entity.getCorporateInformation().getBusinessType());
            corporateInfo.put("industrySector", entity.getCorporateInformation().getIndustrySector());
            corporateInfo.put("authorizedCapital", entity.getCorporateInformation().getAuthorizedCapital());
            corporateInfo.put("paidUpCapital", entity.getCorporateInformation().getPaidUpCapital());
            fenergoRequest.put("corporateInformation", corporateInfo);
        }
        
        // Addresses
        if (entity.getAddresses() != null) {
            fenergoRequest.put("addresses", entity.getAddresses());
        }
        
        // Ownership
        if (entity.getOwnership() != null) {
            fenergoRequest.put("ownership", entity.getOwnership());
        }
        
        // Compliance data
        if (entity.getComplianceData() != null) {
            Map<String, Object> complianceData = new HashMap<>();
            complianceData.put("riskRating", entity.getComplianceData().getRiskRating());
            complianceData.put("complianceStatus", entity.getComplianceData().getComplianceStatus());
            complianceData.put("amlCheck", entity.getComplianceData().getAmlCheck());
            complianceData.put("sanctionsCheck", entity.getComplianceData().getSanctionsCheck());
            complianceData.put("pepCheck", entity.getComplianceData().getPepCheck());
            fenergoRequest.put("complianceData", complianceData);
        }
        
        // KYC data
        if (entity.getKycData() != null) {
            Map<String, Object> kycData = new HashMap<>();
            kycData.put("kycStatus", entity.getKycData().getKycStatus());
            kycData.put("kycLevel", entity.getKycData().getKycLevel());
            kycData.put("verificationDate", entity.getKycData().getVerificationDate());
            kycData.put("expiryDate", entity.getKycData().getExpiryDate());
            kycData.put("documentsVerified", entity.getKycData().getDocumentsVerified());
            fenergoRequest.put("kycData", kycData);
        }
        
        // Additional data
        if (entity.getAdditionalData() != null) {
            fenergoRequest.put("additionalData", entity.getAdditionalData());
        }
        
        // Metadata
        fenergoRequest.put("correlationId", entity.getCorrelationId());
        fenergoRequest.put("requestTimestamp", LocalDateTime.now());
        
        return fenergoRequest;
    }

    private ResponseEntity<Map> callFenergoEntityApi(Map<String, Object> request, HttpMethod method, String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + getFenergoToken());
        headers.set("X-Correlation-ID", (String) request.get("correlationId"));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        logger.debug("Calling Fenergo API: {} {}", method, fenergoApiBaseUrl + endpoint);
        
        return restTemplate.exchange(
            fenergoApiBaseUrl + endpoint,
            method,
            entity,
            Map.class
        );
    }

    private Map<String, Object> processEntityResponse(Map<String, Object> fenergoResponse, FenergoEntity entity) {
        Map<String, Object> result = new HashMap<>();
        
        if (fenergoResponse != null) {
            String status = (String) fenergoResponse.get("status");
            String message = (String) fenergoResponse.get("message");
            
            result.put("status", status);
            result.put("message", message);
            result.put("fenergoEntityId", fenergoResponse.get("entityId"));
            result.put("fenergoVersion", fenergoResponse.get("version"));
            result.put("fenergoStatus", fenergoResponse.get("status"));
            result.put("createdDate", fenergoResponse.get("createdDate"));
            result.put("lastModifiedDate", fenergoResponse.get("lastModifiedDate"));
            
            // Additional Fenergo specific data
            result.put("fenergoReferenceId", fenergoResponse.get("referenceId"));
            result.put("fenergoWorkflowId", fenergoResponse.get("workflowId"));
            result.put("fenergoPolicyId", fenergoResponse.get("policyId"));
            
        } else {
            result.put("status", "FAILED");
            result.put("message", "Empty response from Fenergo API");
        }
        
        return result;
    }

    private String getFenergoToken() {
        // In real implementation, this would get JWT token from Fenergo OAuth2 service
        // For demo purposes, returning a mock token
        return "mock-fenergo-token-" + System.currentTimeMillis();
    }
}
