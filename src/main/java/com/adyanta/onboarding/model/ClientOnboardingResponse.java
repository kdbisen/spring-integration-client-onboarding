package com.adyanta.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Client onboarding response model
 */
public class ClientOnboardingResponse {
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("status")
    private OnboardingStatus status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    @JsonProperty("responseTimestamp")
    private LocalDateTime responseTimestamp;
    
    @JsonProperty("processingSteps")
    private Map<String, StepResult> processingSteps;
    
    @JsonProperty("errors")
    private Map<String, String> errors;

    // Constructors
    public ClientOnboardingResponse() {
        this.responseTimestamp = LocalDateTime.now();
    }

    public ClientOnboardingResponse(String clientId, OnboardingStatus status, String correlationId) {
        this();
        this.clientId = clientId;
        this.status = status;
        this.correlationId = correlationId;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public OnboardingStatus getStatus() {
        return status;
    }

    public void setStatus(OnboardingStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Map<String, StepResult> getProcessingSteps() {
        return processingSteps;
    }

    public void setProcessingSteps(Map<String, StepResult> processingSteps) {
        this.processingSteps = processingSteps;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public enum OnboardingStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        REJECTED
    }

    public static class StepResult {
        @JsonProperty("stepName")
        private String stepName;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        
        @JsonProperty("duration")
        private Long durationMs;

        // Constructors
        public StepResult() {
            this.timestamp = LocalDateTime.now();
        }

        public StepResult(String stepName, String status, String message) {
            this();
            this.stepName = stepName;
            this.status = status;
            this.message = message;
        }

        // Getters and Setters
        public String getStepName() {
            return stepName;
        }

        public void setStepName(String stepName) {
            this.stepName = stepName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public Long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(Long durationMs) {
            this.durationMs = durationMs;
        }
    }
}
