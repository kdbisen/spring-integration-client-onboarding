package com.adyanta.onboarding.fenergo.service;

import com.adyanta.onboarding.fenergo.model.FenergoJourney;
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
 * Fenergo Journey Service
 * Handles journey creation, management, and task orchestration in Fenergo
 */
@Service
public class FenergoJourneyService {

    private static final Logger logger = LoggerFactory.getLogger(FenergoJourneyService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${fenergo.api.base.url:https://your-fenergo-instance.fenergo.com/api/v1}")
    private String fenergoApiBaseUrl;
    
    @Value("${fenergo.api.timeout:30000}")
    private int fenergoApiTimeout;

    public FenergoJourneyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Create a new journey in Fenergo
     */
    public Map<String, Object> createJourney(FenergoJourney journey) {
        logger.info("Creating Fenergo journey: {}", journey.getJourneyName());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Prepare Fenergo API request
            Map<String, Object> fenergoRequest = prepareJourneyRequest(journey);
            
            // Call Fenergo API
            ResponseEntity<Map> response = callFenergoJourneyApi(fenergoRequest, HttpMethod.POST, "/journeys");
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> fenergoResponse = response.getBody();
                
                // Process Fenergo response
                result = processJourneyResponse(fenergoResponse, journey);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Fenergo journey created successfully: {} in {}ms", 
                    journey.getJourneyName(), duration);
                
            } else {
                logger.error("Fenergo API returned error status: {}", response.getStatusCode());
                result.put("status", "FAILED");
                result.put("message", "Fenergo API returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Fenergo journey creation failed: {}", journey.getJourneyName(), e);
            result.put("status", "FAILED");
            result.put("message", "Fenergo journey creation failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "FENERGO_JOURNEY");
        result.put("journeyName", journey.getJourneyName());
        result.put("correlationId", journey.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    /**
     * Start a journey in Fenergo
     */
    public Map<String, Object> startJourney(String journeyId) {
        logger.info("Starting Fenergo journey: {}", journeyId);
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Prepare start journey request
            Map<String, Object> startRequest = new HashMap<>();
            startRequest.put("journeyId", journeyId);
            startRequest.put("startedDate", LocalDateTime.now());
            
            // Call Fenergo API
            ResponseEntity<Map> response = callFenergoJourneyApi(startRequest, HttpMethod.POST, "/journeys/" + journeyId + "/start");
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> fenergoResponse = response.getBody();
                
                result.put("status", "SUCCESS");
                result.put("message", "Journey started successfully");
                result.put("fenergoJourneyId", fenergoResponse.get("journeyId"));
                result.put("fenergoStatus", fenergoResponse.get("status"));
                result.put("startedDate", fenergoResponse.get("startedDate"));
                result.put("currentStage", fenergoResponse.get("currentStage"));
                result.put("activeTasks", fenergoResponse.get("activeTasks"));
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Fenergo journey started successfully: {} in {}ms", 
                    journeyId, duration);
                
            } else {
                logger.error("Fenergo API returned error status: {}", response.getStatusCode());
                result.put("status", "FAILED");
                result.put("message", "Fenergo API returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Fenergo journey start failed: {}", journeyId, e);
            result.put("status", "FAILED");
            result.put("message", "Fenergo journey start failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "FENERGO_JOURNEY");
        result.put("journeyId", journeyId);
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    /**
     * Complete a journey in Fenergo
     */
    public Map<String, Object> completeJourney(String journeyId) {
        logger.info("Completing Fenergo journey: {}", journeyId);
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Prepare complete journey request
            Map<String, Object> completeRequest = new HashMap<>();
            completeRequest.put("journeyId", journeyId);
            completeRequest.put("completedDate", LocalDateTime.now());
            
            // Call Fenergo API
            ResponseEntity<Map> response = callFenergoJourneyApi(completeRequest, HttpMethod.POST, "/journeys/" + journeyId + "/complete");
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> fenergoResponse = response.getBody();
                
                result.put("status", "SUCCESS");
                result.put("message", "Journey completed successfully");
                result.put("fenergoJourneyId", fenergoResponse.get("journeyId"));
                result.put("fenergoStatus", fenergoResponse.get("status"));
                result.put("completedDate", fenergoResponse.get("completedDate"));
                result.put("completionSummary", fenergoResponse.get("completionSummary"));
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Fenergo journey completed successfully: {} in {}ms", 
                    journeyId, duration);
                
            } else {
                logger.error("Fenergo API returned error status: {}", response.getStatusCode());
                result.put("status", "FAILED");
                result.put("message", "Fenergo API returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Fenergo journey completion failed: {}", journeyId, e);
            result.put("status", "FAILED");
            result.put("message", "Fenergo journey completion failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "FENERGO_JOURNEY");
        result.put("journeyId", journeyId);
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    /**
     * Get journey status from Fenergo
     */
    public Map<String, Object> getJourneyStatus(String journeyId) {
        logger.info("Getting Fenergo journey status: {}", journeyId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getFenergoToken());
            headers.set("X-Journey-ID", journeyId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                fenergoApiBaseUrl + "/journeys/" + journeyId + "/status",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to get Fenergo journey status: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to get Fenergo journey status");
            }
            
        } catch (Exception e) {
            logger.error("Error getting Fenergo journey status: {}", journeyId, e);
            return Map.of("status", "ERROR", "message", "Error getting Fenergo journey status: " + e.getMessage());
        }
    }

    /**
     * Update journey task status
     */
    public Map<String, Object> updateTaskStatus(String journeyId, String taskId, String status) {
        logger.info("Updating Fenergo task status: {} - {} to {}", journeyId, taskId, status);
        
        try {
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("taskId", taskId);
            updateRequest.put("status", status);
            updateRequest.put("updatedDate", LocalDateTime.now());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getFenergoToken());
            headers.set("X-Journey-ID", journeyId);
            headers.set("X-Task-ID", taskId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                fenergoApiBaseUrl + "/journeys/" + journeyId + "/tasks/" + taskId + "/status",
                HttpMethod.PUT,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Failed to update Fenergo task status: {}", response.getStatusCode());
                return Map.of("status", "ERROR", "message", "Failed to update Fenergo task status");
            }
            
        } catch (Exception e) {
            logger.error("Error updating Fenergo task status: {}", taskId, e);
            return Map.of("status", "ERROR", "message", "Error updating Fenergo task status: " + e.getMessage());
        }
    }

    private Map<String, Object> prepareJourneyRequest(FenergoJourney journey) {
        Map<String, Object> fenergoRequest = new HashMap<>();
        
        // Basic journey information
        fenergoRequest.put("journeyName", journey.getJourneyName());
        fenergoRequest.put("journeyType", journey.getJourneyType());
        fenergoRequest.put("status", journey.getStatus());
        fenergoRequest.put("entityId", journey.getEntityId());
        fenergoRequest.put("policyId", journey.getPolicyId());
        fenergoRequest.put("processId", journey.getProcessId());
        fenergoRequest.put("version", journey.getVersion());
        
        // Stages
        if (journey.getStages() != null) {
            fenergoRequest.put("stages", journey.getStages());
        }
        
        // Tasks
        if (journey.getTasks() != null) {
            fenergoRequest.put("tasks", journey.getTasks());
        }
        
        // Metadata
        if (journey.getMetadata() != null) {
            fenergoRequest.put("metadata", journey.getMetadata());
        }
        
        // Additional metadata
        fenergoRequest.put("correlationId", journey.getCorrelationId());
        fenergoRequest.put("requestTimestamp", LocalDateTime.now());
        
        return fenergoRequest;
    }

    private ResponseEntity<Map> callFenergoJourneyApi(Map<String, Object> request, HttpMethod method, String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + getFenergoToken());
        headers.set("X-Correlation-ID", (String) request.get("correlationId"));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        logger.debug("Calling Fenergo Journey API: {} {}", method, fenergoApiBaseUrl + endpoint);
        
        return restTemplate.exchange(
            fenergoApiBaseUrl + endpoint,
            method,
            entity,
            Map.class
        );
    }

    private Map<String, Object> processJourneyResponse(Map<String, Object> fenergoResponse, FenergoJourney journey) {
        Map<String, Object> result = new HashMap<>();
        
        if (fenergoResponse != null) {
            String status = (String) fenergoResponse.get("status");
            String message = (String) fenergoResponse.get("message");
            
            result.put("status", status);
            result.put("message", message);
            result.put("fenergoJourneyId", fenergoResponse.get("journeyId"));
            result.put("fenergoStatus", fenergoResponse.get("status"));
            result.put("fenergoVersion", fenergoResponse.get("version"));
            result.put("createdDate", fenergoResponse.get("createdDate"));
            result.put("lastModifiedDate", fenergoResponse.get("lastModifiedDate"));
            
            // Additional Fenergo specific data
            result.put("fenergoWorkflowId", fenergoResponse.get("workflowId"));
            result.put("fenergoPolicyId", fenergoResponse.get("policyId"));
            result.put("fenergoProcessId", fenergoResponse.get("processId"));
            result.put("activeStages", fenergoResponse.get("activeStages"));
            result.put("activeTasks", fenergoResponse.get("activeTasks"));
            
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
