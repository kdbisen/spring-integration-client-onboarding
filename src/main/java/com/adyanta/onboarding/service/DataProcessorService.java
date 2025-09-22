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
 * Data Processor Service
 * Processes and enriches client data for various downstream systems
 */
@Service
public class DataProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessorService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${data.processor.service.url:http://localhost:8082/data/process}")
    private String dataProcessorServiceUrl;
    
    @Value("${data.processor.service.timeout:30000}")
    private int dataProcessorServiceTimeout;

    public DataProcessorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Processes client data for various downstream systems
     */
    public Map<String, Object> processClientData(ClientOnboardingRequest request) {
        logger.info("Starting data processing for client: {}", request.getClientId());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Data enrichment
            Map<String, Object> enrichedData = enrichClientData(request);
            
            // Step 2: Data standardization
            Map<String, Object> standardizedData = standardizeData(enrichedData);
            
            // Step 3: Data validation
            Map<String, Object> validationResult = validateProcessedData(standardizedData);
            
            if (!"SUCCESS".equals(validationResult.get("status"))) {
                result.put("status", "FAILED");
                result.put("message", "Data validation failed: " + validationResult.get("message"));
                return result;
            }
            
            // Step 4: Data transformation
            Map<String, Object> transformedData = transformData(standardizedData);
            
            // Step 5: Send to data processor service
            Map<String, Object> serviceResult = callDataProcessorService(transformedData);
            
            result.putAll(serviceResult);
            result.put("enrichedData", enrichedData);
            result.put("standardizedData", standardizedData);
            result.put("transformedData", transformedData);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Data processing completed for client: {} in {}ms", 
                request.getClientId(), duration);
            
        } catch (Exception e) {
            logger.error("Data processing failed for client: {}", request.getClientId(), e);
            result.put("status", "FAILED");
            result.put("message", "Data processing failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "DATA_PROCESSOR");
        result.put("clientId", request.getClientId());
        result.put("correlationId", request.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    private Map<String, Object> enrichClientData(ClientOnboardingRequest request) {
        Map<String, Object> enrichedData = new HashMap<>();
        
        // Basic client information
        enrichedData.put("clientId", request.getClientId());
        enrichedData.put("firstName", request.getFirstName());
        enrichedData.put("lastName", request.getLastName());
        enrichedData.put("email", request.getEmail());
        enrichedData.put("phoneNumber", request.getPhoneNumber());
        enrichedData.put("documentType", request.getDocumentType());
        enrichedData.put("documentNumber", request.getDocumentNumber());
        enrichedData.put("address", request.getAddress());
        
        // Enriched data
        enrichedData.put("fullName", request.getFirstName() + " " + request.getLastName());
        enrichedData.put("emailDomain", extractEmailDomain(request.getEmail()));
        enrichedData.put("phoneCountryCode", extractCountryCode(request.getPhoneNumber()));
        enrichedData.put("addressHash", generateAddressHashFromAddress(request.getAddress()));
        enrichedData.put("dataQualityScore", calculateDataQualityScore(request));
        enrichedData.put("riskCategory", determineRiskCategory(request));
        enrichedData.put("clientSegment", determineClientSegment(request));
        
        // Timestamps
        enrichedData.put("enrichmentTimestamp", LocalDateTime.now());
        enrichedData.put("requestTimestamp", request.getRequestTimestamp());
        
        return enrichedData;
    }

    private Map<String, Object> standardizeData(Map<String, Object> data) {
        Map<String, Object> standardizedData = new HashMap<>(data);
        
        // Standardize names
        standardizedData.put("firstName", standardizeName((String) data.get("firstName")));
        standardizedData.put("lastName", standardizeName((String) data.get("lastName")));
        
        // Standardize email
        standardizedData.put("email", standardizeEmail((String) data.get("email")));
        
        // Standardize phone number
        standardizedData.put("phoneNumber", standardizePhoneNumber((String) data.get("phoneNumber")));
        
        // Standardize address
        Map<String, Object> address = (Map<String, Object>) data.get("address");
        if (address != null) {
            standardizedData.put("address", standardizeAddress(address));
        }
        
        // Add standardization metadata
        standardizedData.put("standardizationTimestamp", LocalDateTime.now());
        standardizedData.put("standardizationVersion", "1.0");
        
        return standardizedData;
    }

    private Map<String, Object> validateProcessedData(Map<String, Object> data) {
        Map<String, Object> validationResult = new HashMap<>();
        
        try {
            // Validate required fields
            if (data.get("clientId") == null || ((String) data.get("clientId")).trim().isEmpty()) {
                validationResult.put("status", "FAILED");
                validationResult.put("message", "Client ID is required");
                return validationResult;
            }
            
            // Validate email format
            String email = (String) data.get("email");
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                validationResult.put("status", "FAILED");
                validationResult.put("message", "Invalid email format");
                return validationResult;
            }
            
            // Validate phone number format
            String phoneNumber = (String) data.get("phoneNumber");
            if (phoneNumber == null || !phoneNumber.matches("^\\+?[1-9]\\d{1,14}$")) {
                validationResult.put("status", "FAILED");
                validationResult.put("message", "Invalid phone number format");
                return validationResult;
            }
            
            // Validate data quality score
            Integer dataQualityScore = (Integer) data.get("dataQualityScore");
            if (dataQualityScore == null || dataQualityScore < 70) {
                validationResult.put("status", "WARNING");
                validationResult.put("message", "Low data quality score: " + dataQualityScore);
            } else {
                validationResult.put("status", "SUCCESS");
                validationResult.put("message", "Data validation passed");
            }
            
        } catch (Exception e) {
            logger.error("Data validation error", e);
            validationResult.put("status", "FAILED");
            validationResult.put("message", "Data validation error: " + e.getMessage());
        }
        
        return validationResult;
    }

    private Map<String, Object> transformData(Map<String, Object> data) {
        Map<String, Object> transformedData = new HashMap<>();
        
        // Transform for different downstream systems
        transformedData.put("crmData", transformForCrm(data));
        transformedData.put("coreBankingData", transformForCoreBanking(data));
        transformedData.put("riskManagementData", transformForRiskManagement(data));
        transformedData.put("complianceData", transformForCompliance(data));
        transformedData.put("marketingData", transformForMarketing(data));
        
        // Add transformation metadata
        transformedData.put("transformationTimestamp", LocalDateTime.now());
        transformedData.put("transformationVersion", "1.0");
        transformedData.put("originalData", data);
        
        return transformedData;
    }

    private Map<String, Object> callDataProcessorService(Map<String, Object> data) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getDataProcessorToken());
            headers.set("X-Correlation-ID", (String) data.get("correlationId"));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            
            logger.debug("Calling data processor service at: {}", dataProcessorServiceUrl);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                dataProcessorServiceUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> serviceResult = response.getBody();
                serviceResult.put("status", "SUCCESS");
                return serviceResult;
            } else {
                logger.error("Data processor service returned error status: {}", response.getStatusCode());
                return Map.of(
                    "status", "FAILED",
                    "message", "Data processor service returned error: " + response.getStatusCode()
                );
            }
            
        } catch (Exception e) {
            logger.error("Error calling data processor service", e);
            return Map.of(
                "status", "FAILED",
                "message", "Error calling data processor service: " + e.getMessage()
            );
        }
    }

    // Helper methods for data processing
    private String extractEmailDomain(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(email.indexOf("@") + 1);
        }
        return null;
    }

    private String extractCountryCode(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.startsWith("+")) {
            return phoneNumber.substring(0, 3);
        }
        return null;
    }

    private String generateAddressHash(Map<String, Object> address) {
        if (address != null) {
            String addressString = address.toString();
            return String.valueOf(addressString.hashCode());
        }
        return null;
    }

    private String generateAddressHashFromAddress(ClientOnboardingRequest.Address address) {
        if (address != null) {
            String addressString = address.getStreet() + ", " + address.getCity() + ", " + 
                                 address.getState() + " " + address.getPostalCode() + ", " + address.getCountry();
            return String.valueOf(addressString.hashCode());
        }
        return null;
    }

    private Integer calculateDataQualityScore(ClientOnboardingRequest request) {
        int score = 0;
        
        // Check completeness
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) score += 10;
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) score += 10;
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) score += 10;
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) score += 10;
        if (request.getDocumentNumber() != null && !request.getDocumentNumber().trim().isEmpty()) score += 10;
        if (request.getAddress() != null) score += 20;
        
        // Check format validity
        if (request.getEmail() != null && request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) score += 10;
        if (request.getPhoneNumber() != null && request.getPhoneNumber().matches("^\\+?[1-9]\\d{1,14}$")) score += 10;
        
        // Check additional data
        if (request.getAdditionalData() != null && !request.getAdditionalData().isEmpty()) score += 10;
        
        return Math.min(score, 100);
    }

    private String determineRiskCategory(ClientOnboardingRequest request) {
        // Simple risk categorization logic
        if (request.getAdditionalData() != null && request.getAdditionalData().containsKey("highRisk")) {
            return "HIGH";
        }
        return "LOW";
    }

    private String determineClientSegment(ClientOnboardingRequest request) {
        // Simple client segmentation logic
        if (request.getAdditionalData() != null && request.getAdditionalData().containsKey("premium")) {
            return "PREMIUM";
        }
        return "STANDARD";
    }

    private String standardizeName(String name) {
        if (name == null) return null;
        return name.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String standardizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    private String standardizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        return phoneNumber.replaceAll("[^+0-9]", "");
    }

    private Map<String, Object> standardizeAddress(Map<String, Object> address) {
        Map<String, Object> standardizedAddress = new HashMap<>();
        
        if (address.get("street") != null) {
            standardizedAddress.put("street", ((String) address.get("street")).trim().toUpperCase());
        }
        if (address.get("city") != null) {
            standardizedAddress.put("city", ((String) address.get("city")).trim().toUpperCase());
        }
        if (address.get("state") != null) {
            standardizedAddress.put("state", ((String) address.get("state")).trim().toUpperCase());
        }
        if (address.get("postalCode") != null) {
            standardizedAddress.put("postalCode", ((String) address.get("postalCode")).trim());
        }
        if (address.get("country") != null) {
            standardizedAddress.put("country", ((String) address.get("country")).trim().toUpperCase());
        }
        
        return standardizedAddress;
    }

    // Transformation methods for different downstream systems
    private Map<String, Object> transformForCrm(Map<String, Object> data) {
        Map<String, Object> crmData = new HashMap<>();
        crmData.put("contactId", data.get("clientId"));
        crmData.put("name", data.get("fullName"));
        crmData.put("email", data.get("email"));
        crmData.put("phone", data.get("phoneNumber"));
        crmData.put("segment", data.get("clientSegment"));
        return crmData;
    }

    private Map<String, Object> transformForCoreBanking(Map<String, Object> data) {
        Map<String, Object> coreBankingData = new HashMap<>();
        coreBankingData.put("customerId", data.get("clientId"));
        coreBankingData.put("customerName", data.get("fullName"));
        coreBankingData.put("emailAddress", data.get("email"));
        coreBankingData.put("mobileNumber", data.get("phoneNumber"));
        coreBankingData.put("documentType", data.get("documentType"));
        coreBankingData.put("documentNumber", data.get("documentNumber"));
        return coreBankingData;
    }

    private Map<String, Object> transformForRiskManagement(Map<String, Object> data) {
        Map<String, Object> riskData = new HashMap<>();
        riskData.put("clientId", data.get("clientId"));
        riskData.put("riskCategory", data.get("riskCategory"));
        riskData.put("dataQualityScore", data.get("dataQualityScore"));
        riskData.put("addressHash", data.get("addressHash"));
        return riskData;
    }

    private Map<String, Object> transformForCompliance(Map<String, Object> data) {
        Map<String, Object> complianceData = new HashMap<>();
        complianceData.put("clientId", data.get("clientId"));
        complianceData.put("documentType", data.get("documentType"));
        complianceData.put("documentNumber", data.get("documentNumber"));
        complianceData.put("address", data.get("address"));
        return complianceData;
    }

    private Map<String, Object> transformForMarketing(Map<String, Object> data) {
        Map<String, Object> marketingData = new HashMap<>();
        marketingData.put("clientId", data.get("clientId"));
        marketingData.put("name", data.get("fullName"));
        marketingData.put("email", data.get("email"));
        marketingData.put("segment", data.get("clientSegment"));
        marketingData.put("emailDomain", data.get("emailDomain"));
        return marketingData;
    }

    private String getDataProcessorToken() {
        // In real implementation, this would get JWT token from Apigee or OAuth2 service
        return "mock-data-processor-token-" + System.currentTimeMillis();
    }
}
