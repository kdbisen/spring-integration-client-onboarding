package com.adyanta.onboarding.controller;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import com.adyanta.onboarding.model.ClientOnboardingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Client Onboarding
 * Provides HTTP endpoints for the client onboarding process
 */
@RestController
@RequestMapping("/api/v1/onboarding")
@Validated
public class ClientOnboardingController {

    private static final Logger logger = LoggerFactory.getLogger(ClientOnboardingController.class);

    @Autowired
    private MessageChannel clientOnboardingInputChannel;

    /**
     * Submit client onboarding request
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitOnboardingRequest(
            @Valid @RequestBody ClientOnboardingRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received client onboarding request for client: {}", request.getClientId());
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            request.setCorrelationId(correlationId);
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("clientId", request.getClientId());
            response.put("status", "ACCEPTED");
            response.put("message", "Client onboarding request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<ClientOnboardingRequest> message = MessageBuilder
                    .withPayload(request)
                    .setHeader("correlationId", correlationId)
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            clientOnboardingInputChannel.send(message);
            
            logger.info("Client onboarding request submitted successfully for client: {}", request.getClientId());
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting client onboarding request for client: {}", request.getClientId(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("clientId", request.getClientId());
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting client onboarding request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get client onboarding status
     */
    @GetMapping("/status/{clientId}")
    public ResponseEntity<Map<String, Object>> getOnboardingStatus(
            @PathVariable String clientId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Getting onboarding status for client: {}", clientId);
        
        try {
            // In real implementation, this would query the database or cache
            Map<String, Object> statusResponse = new HashMap<>();
            statusResponse.put("clientId", clientId);
            statusResponse.put("correlationId", correlationId);
            statusResponse.put("status", "IN_PROGRESS");
            statusResponse.put("message", "Client onboarding is in progress");
            statusResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(statusResponse);
            
        } catch (Exception e) {
            logger.error("Error getting onboarding status for client: {}", clientId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("clientId", clientId);
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error getting onboarding status: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Cancel client onboarding request
     */
    @PostMapping("/cancel/{clientId}")
    public ResponseEntity<Map<String, Object>> cancelOnboardingRequest(
            @PathVariable String clientId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @RequestBody(required = false) Map<String, Object> cancelReason) {
        
        logger.info("Cancelling onboarding request for client: {}", clientId);
        
        try {
            // In real implementation, this would update the database and send cancellation message
            Map<String, Object> cancelResponse = new HashMap<>();
            cancelResponse.put("clientId", clientId);
            cancelResponse.put("correlationId", correlationId);
            cancelResponse.put("status", "CANCELLED");
            cancelResponse.put("message", "Client onboarding request cancelled successfully");
            cancelResponse.put("cancelReason", cancelReason != null ? cancelReason.get("reason") : "No reason provided");
            cancelResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(cancelResponse);
            
        } catch (Exception e) {
            logger.error("Error cancelling onboarding request for client: {}", clientId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("clientId", clientId);
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error cancelling onboarding request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "client-onboarding");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Get service metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("service", "client-onboarding");
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("activeRequests", 0); // In real implementation, this would be actual metrics
        metrics.put("totalRequests", 0);
        metrics.put("successfulRequests", 0);
        metrics.put("failedRequests", 0);
        metrics.put("averageProcessingTime", 0);
        
        return ResponseEntity.ok(metrics);
    }
}
