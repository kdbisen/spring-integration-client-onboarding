package com.adyanta.onboarding.integration;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import com.adyanta.onboarding.model.ClientOnboardingResponse;
import com.adyanta.onboarding.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * Simplified Client Onboarding Integration Flow
 * Uses Spring Integration annotations instead of DSL for better compatibility
 */
@Configuration
@EnableIntegration
public class SimpleClientOnboardingFlow {

    private static final Logger logger = LoggerFactory.getLogger(SimpleClientOnboardingFlow.class);

    @Autowired
    private ValidationService validationService;

    @Autowired
    private KycService kycService;

    @Autowired
    private DataProcessorService dataProcessorService;

    @Autowired
    private ReferenceDataService referenceDataService;

    @Autowired
    private LesService lesService;

    @Autowired
    private NotificationService notificationService;

    // Message Channels
    @Bean
    public MessageChannel clientOnboardingInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel validationChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel kycChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel dataProcessorChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel referenceDataChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel lesChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel notificationChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel responseChannel() {
        return new DirectChannel();
    }

    /**
     * Main entry point for client onboarding
     */
    @ServiceActivator(inputChannel = "clientOnboardingInputChannel")
    public Message<ClientOnboardingResponse> processClientOnboarding(Message<ClientOnboardingRequest> message) {
        logger.info("Processing client onboarding request: {}", message.getPayload().getFirstName());
        
        try {
            ClientOnboardingRequest request = message.getPayload();
            
            // Add correlation ID if not present
            String correlationId = (String) message.getHeaders().get("correlationId");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Process through validation
            ClientOnboardingResponse validationResponse = validationService.validateRequest(request);
            if (validationResponse.getStatus() == ClientOnboardingResponse.OnboardingStatus.FAILED) {
                return MessageBuilder
                        .withPayload(validationResponse)
                        .setHeader("correlationId", correlationId)
                        .build();
            }
            
            // Process through KYC
            Map<String, Object> kycResult = kycService.verifyClient(request);
            logger.info("KYC verification completed: {}", kycResult.get("status"));
            
            // Process through data processor
            Map<String, Object> dataResult = dataProcessorService.processClientData(request);
            logger.info("Data enrichment completed: {}", dataResult.get("status"));
            
            // Process through reference data
            Map<String, Object> referenceResult = referenceDataService.fetchReferenceData(request);
            logger.info("Reference data validation completed: {}", referenceResult.get("status"));
            
            // Process through LES
            Map<String, Object> lesResult = lesService.verifyWithLes(request);
            logger.info("Legal entity verification completed: {}", lesResult.get("status"));
            
            // Send notification
            Map<String, Object> notificationResult = notificationService.sendNotification(validationResponse);
            logger.info("Notification sent: {}", notificationResult.get("status"));
            
            // Create success response
            ClientOnboardingResponse response = new ClientOnboardingResponse();
            response.setStatus(ClientOnboardingResponse.OnboardingStatus.COMPLETED);
            response.setMessage("Client onboarding completed successfully");
            response.setCorrelationId(correlationId);
            response.setClientId("CLIENT_" + correlationId.substring(0, 8));
            
            logger.info("Client onboarding completed successfully: {}", correlationId);
            
            return MessageBuilder
                    .withPayload(response)
                    .setHeader("correlationId", correlationId)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error processing client onboarding request", e);
            
            ClientOnboardingResponse errorResponse = new ClientOnboardingResponse();
            errorResponse.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
            errorResponse.setMessage("Client onboarding failed: " + e.getMessage());
            errorResponse.setCorrelationId((String) message.getHeaders().get("correlationId"));
            
            return MessageBuilder
                    .withPayload(errorResponse)
                    .setHeader("correlationId", message.getHeaders().get("correlationId"))
                    .build();
        }
    }

    /**
     * Validation service activator
     */
    @ServiceActivator(inputChannel = "validationChannel")
    public Message<ClientOnboardingResponse> validateRequest(Message<ClientOnboardingRequest> message) {
        logger.info("Validating client onboarding request");
        
        ClientOnboardingRequest request = message.getPayload();
        ClientOnboardingResponse response = validationService.validateRequest(request);
        
        return MessageBuilder
                .withPayload(response)
                .copyHeaders(message.getHeaders())
                .build();
    }

    /**
     * KYC service activator
     */
    @ServiceActivator(inputChannel = "kycChannel")
    public Message<Map<String, Object>> performKyc(Message<ClientOnboardingRequest> message) {
        logger.info("Performing KYC verification");
        
        ClientOnboardingRequest request = message.getPayload();
        Map<String, Object> result = kycService.verifyClient(request);
        
        return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .build();
    }

    /**
     * Data processor service activator
     */
    @ServiceActivator(inputChannel = "dataProcessorChannel")
    public Message<Map<String, Object>> enrichData(Message<ClientOnboardingRequest> message) {
        logger.info("Enriching client data");
        
        ClientOnboardingRequest request = message.getPayload();
        Map<String, Object> result = dataProcessorService.processClientData(request);
        
        return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .build();
    }

    /**
     * Reference data service activator
     */
    @ServiceActivator(inputChannel = "referenceDataChannel")
    public Message<Map<String, Object>> validateReferenceData(Message<ClientOnboardingRequest> message) {
        logger.info("Validating reference data");
        
        ClientOnboardingRequest request = message.getPayload();
        Map<String, Object> result = referenceDataService.fetchReferenceData(request);
        
        return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .build();
    }

    /**
     * LES service activator
     */
    @ServiceActivator(inputChannel = "lesChannel")
    public Message<Map<String, Object>> verifyLegalEntity(Message<ClientOnboardingRequest> message) {
        logger.info("Verifying legal entity");
        
        ClientOnboardingRequest request = message.getPayload();
        Map<String, Object> result = lesService.verifyWithLes(request);
        
        return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .build();
    }

    /**
     * Notification service activator
     */
    @ServiceActivator(inputChannel = "notificationChannel")
    public Message<Map<String, Object>> sendNotification(Message<ClientOnboardingResponse> message) {
        logger.info("Sending notification");
        
        ClientOnboardingResponse response = message.getPayload();
        Map<String, Object> result = notificationService.sendNotification(response);
        
        return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .build();
    }
}
