package com.adyanta.onboarding.service;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import com.adyanta.onboarding.model.ClientOnboardingResponse;
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
 * Notification Service
 * Handles notifications for client onboarding process
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${notification.service.url:http://localhost:8085/notification/send}")
    private String notificationServiceUrl;
    
    @Value("${notification.service.timeout:30000}")
    private int notificationServiceTimeout;

    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends notification for client onboarding
     */
    public Map<String, Object> sendNotification(ClientOnboardingResponse response) {
        logger.info("Starting notification for client: {}", response.getClientId());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Prepare notification request
            Map<String, Object> notificationRequest = prepareNotificationRequest(response);
            
            // Step 2: Send email notification
            Map<String, Object> emailResult = sendEmailNotification(notificationRequest);
            
            // Step 3: Send SMS notification
            Map<String, Object> smsResult = sendSmsNotification(notificationRequest);
            
            // Step 4: Send internal notification
            Map<String, Object> internalResult = sendInternalNotification(notificationRequest);
            
            // Step 5: Send webhook notification
            Map<String, Object> webhookResult = sendWebhookNotification(notificationRequest);
            
            // Aggregate results
            result.put("emailNotification", emailResult);
            result.put("smsNotification", smsResult);
            result.put("internalNotification", internalResult);
            result.put("webhookNotification", webhookResult);
            
            // Determine overall status
            boolean allSuccessful = isAllNotificationsSuccessful(result);
            result.put("status", allSuccessful ? "SUCCESS" : "PARTIAL_SUCCESS");
            result.put("message", allSuccessful ? "All notifications sent successfully" : "Some notifications failed");
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Notification completed for client: {} in {}ms", 
                response.getClientId(), duration);
            
        } catch (Exception e) {
            logger.error("Notification failed for client: {}", response.getClientId(), e);
            result.put("status", "FAILED");
            result.put("message", "Notification failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        // Add metadata
        result.put("serviceName", "NOTIFICATION");
        result.put("clientId", response.getClientId());
        result.put("correlationId", response.getCorrelationId());
        result.put("timestamp", LocalDateTime.now());
        result.put("durationMs", System.currentTimeMillis() - startTime);
        
        return result;
    }

    private Map<String, Object> prepareNotificationRequest(ClientOnboardingResponse response) {
        Map<String, Object> notificationRequest = new HashMap<>();
        
        // Basic information
        notificationRequest.put("clientId", response.getClientId());
        notificationRequest.put("status", response.getStatus());
        notificationRequest.put("message", response.getMessage());
        notificationRequest.put("correlationId", response.getCorrelationId());
        notificationRequest.put("responseTimestamp", response.getResponseTimestamp());
        
        // Processing steps
        notificationRequest.put("processingSteps", response.getProcessingSteps());
        
        // Errors
        notificationRequest.put("errors", response.getErrors());
        
        // Notification preferences
        notificationRequest.put("emailEnabled", true);
        notificationRequest.put("smsEnabled", true);
        notificationRequest.put("internalNotificationEnabled", true);
        notificationRequest.put("webhookEnabled", true);
        
        // Template information
        notificationRequest.put("emailTemplate", getEmailTemplate(response.getStatus()));
        notificationRequest.put("smsTemplate", getSmsTemplate(response.getStatus()));
        notificationRequest.put("internalTemplate", getInternalTemplate(response.getStatus()));
        
        return notificationRequest;
    }

    private Map<String, Object> sendEmailNotification(Map<String, Object> notificationRequest) {
        try {
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("to", getClientEmail(notificationRequest));
            emailRequest.put("subject", getEmailSubject(notificationRequest));
            emailRequest.put("template", notificationRequest.get("emailTemplate"));
            emailRequest.put("data", notificationRequest);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getNotificationToken());
            headers.set("X-Notification-Type", "EMAIL");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(emailRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                notificationServiceUrl + "/email",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Email notification failed: {}", response.getStatusCode());
                return Map.of("status", "FAILED", "message", "Email notification failed");
            }
            
        } catch (Exception e) {
            logger.error("Error sending email notification", e);
            return Map.of("status", "FAILED", "message", "Email notification error: " + e.getMessage());
        }
    }

    private Map<String, Object> sendSmsNotification(Map<String, Object> notificationRequest) {
        try {
            Map<String, Object> smsRequest = new HashMap<>();
            smsRequest.put("to", getClientPhoneNumber(notificationRequest));
            smsRequest.put("message", getSmsMessage(notificationRequest));
            smsRequest.put("template", notificationRequest.get("smsTemplate"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getNotificationToken());
            headers.set("X-Notification-Type", "SMS");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(smsRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                notificationServiceUrl + "/sms",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("SMS notification failed: {}", response.getStatusCode());
                return Map.of("status", "FAILED", "message", "SMS notification failed");
            }
            
        } catch (Exception e) {
            logger.error("Error sending SMS notification", e);
            return Map.of("status", "FAILED", "message", "SMS notification error: " + e.getMessage());
        }
    }

    private Map<String, Object> sendInternalNotification(Map<String, Object> notificationRequest) {
        try {
            Map<String, Object> internalRequest = new HashMap<>();
            internalRequest.put("recipients", getInternalRecipients(notificationRequest));
            internalRequest.put("subject", getInternalSubject(notificationRequest));
            internalRequest.put("message", getInternalMessage(notificationRequest));
            internalRequest.put("priority", getNotificationPriority(notificationRequest));
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getNotificationToken());
            headers.set("X-Notification-Type", "INTERNAL");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(internalRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                notificationServiceUrl + "/internal",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Internal notification failed: {}", response.getStatusCode());
                return Map.of("status", "FAILED", "message", "Internal notification failed");
            }
            
        } catch (Exception e) {
            logger.error("Error sending internal notification", e);
            return Map.of("status", "FAILED", "message", "Internal notification error: " + e.getMessage());
        }
    }

    private Map<String, Object> sendWebhookNotification(Map<String, Object> notificationRequest) {
        try {
            Map<String, Object> webhookRequest = new HashMap<>();
            webhookRequest.put("url", getWebhookUrl(notificationRequest));
            webhookRequest.put("payload", notificationRequest);
            webhookRequest.put("headers", getWebhookHeaders(notificationRequest));
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getNotificationToken());
            headers.set("X-Notification-Type", "WEBHOOK");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(webhookRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                notificationServiceUrl + "/webhook",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.error("Webhook notification failed: {}", response.getStatusCode());
                return Map.of("status", "FAILED", "message", "Webhook notification failed");
            }
            
        } catch (Exception e) {
            logger.error("Error sending webhook notification", e);
            return Map.of("status", "FAILED", "message", "Webhook notification error: " + e.getMessage());
        }
    }

    // Helper methods
    private boolean isAllNotificationsSuccessful(Map<String, Object> result) {
        return "SUCCESS".equals(result.get("emailNotification")) &&
               "SUCCESS".equals(result.get("smsNotification")) &&
               "SUCCESS".equals(result.get("internalNotification")) &&
               "SUCCESS".equals(result.get("webhookNotification"));
    }

    private String getEmailTemplate(ClientOnboardingResponse.OnboardingStatus status) {
        return switch (status) {
            case COMPLETED -> "client-onboarding-success";
            case FAILED -> "client-onboarding-failure";
            case REJECTED -> "client-onboarding-rejection";
            default -> "client-onboarding-status";
        };
    }

    private String getSmsTemplate(ClientOnboardingResponse.OnboardingStatus status) {
        return switch (status) {
            case COMPLETED -> "sms-onboarding-success";
            case FAILED -> "sms-onboarding-failure";
            case REJECTED -> "sms-onboarding-rejection";
            default -> "sms-onboarding-status";
        };
    }

    private String getInternalTemplate(ClientOnboardingResponse.OnboardingStatus status) {
        return switch (status) {
            case COMPLETED -> "internal-onboarding-success";
            case FAILED -> "internal-onboarding-failure";
            case REJECTED -> "internal-onboarding-rejection";
            default -> "internal-onboarding-status";
        };
    }

    private String getClientEmail(Map<String, Object> notificationRequest) {
        // In real implementation, this would fetch from client data
        return "client@example.com";
    }

    private String getClientPhoneNumber(Map<String, Object> notificationRequest) {
        // In real implementation, this would fetch from client data
        return "+1234567890";
    }

    private String getEmailSubject(Map<String, Object> notificationRequest) {
        String status = (String) notificationRequest.get("status");
        return switch (status) {
            case "COMPLETED" -> "Client Onboarding Completed Successfully";
            case "FAILED" -> "Client Onboarding Failed";
            case "REJECTED" -> "Client Onboarding Rejected";
            default -> "Client Onboarding Status Update";
        };
    }

    private String getSmsMessage(Map<String, Object> notificationRequest) {
        String status = (String) notificationRequest.get("status");
        String clientId = (String) notificationRequest.get("clientId");
        return switch (status) {
            case "COMPLETED" -> "Your client onboarding for " + clientId + " has been completed successfully.";
            case "FAILED" -> "Your client onboarding for " + clientId + " has failed. Please contact support.";
            case "REJECTED" -> "Your client onboarding for " + clientId + " has been rejected. Please contact support.";
            default -> "Your client onboarding for " + clientId + " status has been updated.";
        };
    }

    private String[] getInternalRecipients(Map<String, Object> notificationRequest) {
        // In real implementation, this would be based on business rules
        return new String[]{"onboarding-team@company.com", "compliance-team@company.com"};
    }

    private String getInternalSubject(Map<String, Object> notificationRequest) {
        String status = (String) notificationRequest.get("status");
        String clientId = (String) notificationRequest.get("clientId");
        return "Client Onboarding " + status + " - " + clientId;
    }

    private String getInternalMessage(Map<String, Object> notificationRequest) {
        String status = (String) notificationRequest.get("status");
        String clientId = (String) notificationRequest.get("clientId");
        String message = (String) notificationRequest.get("message");
        return "Client " + clientId + " onboarding status: " + status + ". Message: " + message;
    }

    private String getNotificationPriority(Map<String, Object> notificationRequest) {
        String status = (String) notificationRequest.get("status");
        return switch (status) {
            case "FAILED", "REJECTED" -> "HIGH";
            case "COMPLETED" -> "NORMAL";
            default -> "LOW";
        };
    }

    private String getWebhookUrl(Map<String, Object> notificationRequest) {
        // In real implementation, this would be configurable
        return "https://webhook.company.com/onboarding";
    }

    private Map<String, String> getWebhookHeaders(Map<String, Object> notificationRequest) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Webhook-Source", "client-onboarding");
        headers.put("X-Client-ID", (String) notificationRequest.get("clientId"));
        headers.put("X-Correlation-ID", (String) notificationRequest.get("correlationId"));
        return headers;
    }

    private String getNotificationToken() {
        // In real implementation, this would get JWT token from Apigee or OAuth2 service
        return "mock-notification-token-" + System.currentTimeMillis();
    }
}
