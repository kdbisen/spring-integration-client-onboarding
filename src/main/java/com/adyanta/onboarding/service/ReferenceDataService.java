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
 * Reference Data Service
 * Fetches reference data for client onboarding validation and enrichment
 */
@Service
public class ReferenceDataService {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceDataService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${reference.data.service.url:http://localhost:8083/reference/data}")
    private String referenceDataServiceUrl;
    
    @Value("${reference.data.service.timeout:30000}")
    private int referenceDataServiceTimeout;

    public ReferenceDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches reference data for client onboarding
     */
    public Map<String, Object> fetchReferenceData(ClientOnboardingRequest request) {
        logger.info("Starting reference data fetch for client: {}", request.getClientId());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Fetch country and region data
            Map<String, Object> countryData = fetchCountryData(request.getAddress().getCountry());
            
            // Step 2: Fetch document type validation rules
            Map<String, Object> documentRules = fetchDocumentValidationRules(request.getDocumentType());
            
            // Step 3: Fetch industry classification data
            Map<String, Object> industryData = fetchIndustryClassification(request);
            
            // Step 4: Fetch regulatory requirements
            Map<String, Object> regulatoryData = fetchRegulatoryRequirements(request.getAddress().getCountry());
            
            // Step 5: Fetch currency and exchange rate data
            Map<String, Object> currencyData = fetchCurrencyData(request.getAddress().getCountry());
            
            // Aggregate all reference data
            result.put("countryData", countryData);
            result.put("documentRules", documentRules);
            result.put("industryData", industryData);
            result.put("regulatoryData", regulatoryData);
            result.put("currencyData", currencyData);
            
            // Add validation results
            result.put("validationResults", validateAgainstReferenceData(request, result));
            
            result.put("status", "SUCCESS");
            result.put("message", "Reference data fetched successfully");
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Reference data fetch completed for client: {} in {}ms", 
                request.getClientId(), duration);
            
        } catch (Exception e) {
            logger.error("Reference data fetch failed for client: {}", request.getClientId(), e);
            result.put("status", "FAILED");
            result.put("message", "Reference data fetch failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "REFERENCE_DATA");
        result.put("clientId", request.getClientId());
        result.put("correlationId", request.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    private Map<String, Object> fetchCountryData(String countryCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getReferenceDataToken());
            headers.set("X-Country-Code", countryCode);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                referenceDataServiceUrl + "/country/" + countryCode,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.warn("Failed to fetch country data for: {}, status: {}", 
                    countryCode, response.getStatusCode());
                return createDefaultCountryData(countryCode);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching country data for: {}", countryCode, e);
            return createDefaultCountryData(countryCode);
        }
    }

    private Map<String, Object> fetchDocumentValidationRules(String documentType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getReferenceDataToken());
            headers.set("X-Document-Type", documentType);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                referenceDataServiceUrl + "/document-rules/" + documentType,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.warn("Failed to fetch document rules for: {}, status: {}", 
                    documentType, response.getStatusCode());
                return createDefaultDocumentRules(documentType);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching document rules for: {}", documentType, e);
            return createDefaultDocumentRules(documentType);
        }
    }

    private Map<String, Object> fetchIndustryClassification(ClientOnboardingRequest request) {
        try {
            // Determine industry based on additional data or other criteria
            String industryCode = determineIndustryCode(request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getReferenceDataToken());
            headers.set("X-Industry-Code", industryCode);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                referenceDataServiceUrl + "/industry/" + industryCode,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.warn("Failed to fetch industry data for: {}, status: {}", 
                    industryCode, response.getStatusCode());
                return createDefaultIndustryData(industryCode);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching industry data", e);
            return createDefaultIndustryData("UNKNOWN");
        }
    }

    private Map<String, Object> fetchRegulatoryRequirements(String countryCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getReferenceDataToken());
            headers.set("X-Country-Code", countryCode);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                referenceDataServiceUrl + "/regulatory/" + countryCode,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.warn("Failed to fetch regulatory data for: {}, status: {}", 
                    countryCode, response.getStatusCode());
                return createDefaultRegulatoryData(countryCode);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching regulatory data for: {}", countryCode, e);
            return createDefaultRegulatoryData(countryCode);
        }
    }

    private Map<String, Object> fetchCurrencyData(String countryCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getReferenceDataToken());
            headers.set("X-Country-Code", countryCode);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                referenceDataServiceUrl + "/currency/" + countryCode,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.warn("Failed to fetch currency data for: {}, status: {}", 
                    countryCode, response.getStatusCode());
                return createDefaultCurrencyData(countryCode);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching currency data for: {}", countryCode, e);
            return createDefaultCurrencyData(countryCode);
        }
    }

    private Map<String, Object> validateAgainstReferenceData(ClientOnboardingRequest request, 
                                                           Map<String, Object> referenceData) {
        Map<String, Object> validationResults = new HashMap<>();
        
        try {
            // Validate document number against rules
            Map<String, Object> documentRules = (Map<String, Object>) referenceData.get("documentRules");
            boolean documentValid = validateDocumentNumber(request.getDocumentNumber(), documentRules);
            validationResults.put("documentValid", documentValid);
            
            // Validate country-specific requirements
            Map<String, Object> countryData = (Map<String, Object>) referenceData.get("countryData");
            boolean countryValid = validateCountryRequirements(request, countryData);
            validationResults.put("countryValid", countryValid);
            
            // Validate regulatory compliance
            Map<String, Object> regulatoryData = (Map<String, Object>) referenceData.get("regulatoryData");
            boolean regulatoryCompliant = validateRegulatoryCompliance(request, regulatoryData);
            validationResults.put("regulatoryCompliant", regulatoryCompliant);
            
            // Overall validation status
            boolean overallValid = documentValid && countryValid && regulatoryCompliant;
            validationResults.put("overallValid", overallValid);
            validationResults.put("validationTimestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            logger.error("Error validating against reference data", e);
            validationResults.put("overallValid", false);
            validationResults.put("error", e.getMessage());
        }
        
        return validationResults;
    }

    // Helper methods
    private String determineIndustryCode(ClientOnboardingRequest request) {
        // Simple industry determination logic
        if (request.getAdditionalData() != null) {
            Object industry = request.getAdditionalData().get("industry");
            if (industry != null) {
                return industry.toString();
            }
        }
        return "UNKNOWN";
    }

    private boolean validateDocumentNumber(String documentNumber, Map<String, Object> documentRules) {
        if (documentRules == null || documentNumber == null) {
            return false;
        }
        
        // Simple validation - in real implementation, this would use the actual rules
        Integer minLength = (Integer) documentRules.get("minLength");
        Integer maxLength = (Integer) documentRules.get("maxLength");
        
        if (minLength != null && documentNumber.length() < minLength) {
            return false;
        }
        
        if (maxLength != null && documentNumber.length() > maxLength) {
            return false;
        }
        
        return true;
    }

    private boolean validateCountryRequirements(ClientOnboardingRequest request, Map<String, Object> countryData) {
        if (countryData == null) {
            return false;
        }
        
        // Simple validation - in real implementation, this would use actual country requirements
        Boolean requiresAddress = (Boolean) countryData.get("requiresAddress");
        if (requiresAddress != null && requiresAddress && request.getAddress() == null) {
            return false;
        }
        
        return true;
    }

    private boolean validateRegulatoryCompliance(ClientOnboardingRequest request, Map<String, Object> regulatoryData) {
        if (regulatoryData == null) {
            return false;
        }
        
        // Simple validation - in real implementation, this would use actual regulatory requirements
        Boolean requiresKyc = (Boolean) regulatoryData.get("requiresKyc");
        if (requiresKyc != null && requiresKyc) {
            // This would be validated in the KYC service
            return true;
        }
        
        return true;
    }

    // Default data creation methods
    private Map<String, Object> createDefaultCountryData(String countryCode) {
        Map<String, Object> countryData = new HashMap<>();
        countryData.put("countryCode", countryCode);
        countryData.put("countryName", "Unknown Country");
        countryData.put("requiresAddress", true);
        countryData.put("currencyCode", "USD");
        countryData.put("timezone", "UTC");
        return countryData;
    }

    private Map<String, Object> createDefaultDocumentRules(String documentType) {
        Map<String, Object> documentRules = new HashMap<>();
        documentRules.put("documentType", documentType);
        documentRules.put("minLength", 5);
        documentRules.put("maxLength", 20);
        documentRules.put("pattern", ".*");
        documentRules.put("required", true);
        return documentRules;
    }

    private Map<String, Object> createDefaultIndustryData(String industryCode) {
        Map<String, Object> industryData = new HashMap<>();
        industryData.put("industryCode", industryCode);
        industryData.put("industryName", "Unknown Industry");
        industryData.put("riskLevel", "MEDIUM");
        industryData.put("regulatoryCategory", "STANDARD");
        return industryData;
    }

    private Map<String, Object> createDefaultRegulatoryData(String countryCode) {
        Map<String, Object> regulatoryData = new HashMap<>();
        regulatoryData.put("countryCode", countryCode);
        regulatoryData.put("requiresKyc", true);
        regulatoryData.put("requiresAml", true);
        regulatoryData.put("requiresSanctionsCheck", true);
        regulatoryData.put("dataRetentionYears", 7);
        return regulatoryData;
    }

    private Map<String, Object> createDefaultCurrencyData(String countryCode) {
        Map<String, Object> currencyData = new HashMap<>();
        currencyData.put("countryCode", countryCode);
        currencyData.put("currencyCode", "USD");
        currencyData.put("currencyName", "US Dollar");
        currencyData.put("exchangeRate", 1.0);
        currencyData.put("lastUpdated", LocalDateTime.now());
        return currencyData;
    }

    private String getReferenceDataToken() {
        // In real implementation, this would get JWT token from Apigee or OAuth2 service
        return "mock-reference-data-token-" + System.currentTimeMillis();
    }
}
