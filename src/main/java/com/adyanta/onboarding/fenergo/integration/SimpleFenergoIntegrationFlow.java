package com.adyanta.onboarding.fenergo.integration;

import com.adyanta.onboarding.fenergo.model.FenergoEntity;
import com.adyanta.onboarding.fenergo.model.FenergoJourney;
import com.adyanta.onboarding.fenergo.service.FenergoEntityService;
import com.adyanta.onboarding.fenergo.service.FenergoJourneyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * Simplified Fenergo Integration Flow
 * Uses Spring Integration annotations for Fenergo operations
 */
@Configuration
@EnableIntegration
public class SimpleFenergoIntegrationFlow {

    private static final Logger logger = LoggerFactory.getLogger(SimpleFenergoIntegrationFlow.class);

    @Autowired
    private FenergoEntityService fenergoEntityService;
    
    @Autowired
    private FenergoJourneyService fenergoJourneyService;

    // Message Channels for Fenergo Integration
    @Bean
    public MessageChannel fenergoEntityInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel fenergoJourneyInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel fenergoTaskInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel fenergoResponseChannel() {
        return new DirectChannel();
    }

    /**
     * Fenergo Entity Creation Service Activator
     */
    @ServiceActivator(inputChannel = "fenergoEntityInputChannel")
    public Message<Map<String, Object>> createFenergoEntity(Message<FenergoEntity> message) {
        logger.info("Creating Fenergo entity: {}", message.getPayload().getEntityName());
        
        try {
            FenergoEntity entity = message.getPayload();
            
            // Add correlation ID if not present
            String correlationId = (String) message.getHeaders().get("correlationId");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            entity.setCorrelationId(correlationId);
            
            // Create entity in Fenergo
            Map<String, Object> result = fenergoEntityService.createEntity(entity);
            
            logger.info("Fenergo entity creation completed: {}", result.get("status"));
            
            return MessageBuilder
                    .withPayload(result)
                    .setHeader("correlationId", correlationId)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error creating Fenergo entity", e);
            
            Map<String, Object> errorResult = Map.of(
                "status", "FAILED",
                "message", "Fenergo entity creation failed: " + e.getMessage(),
                "correlationId", message.getHeaders().get("correlationId")
            );
            
            return MessageBuilder
                    .withPayload(errorResult)
                    .setHeader("correlationId", message.getHeaders().get("correlationId"))
                    .build();
        }
    }

    /**
     * Fenergo Journey Creation Service Activator
     */
    @ServiceActivator(inputChannel = "fenergoJourneyInputChannel")
    public Message<Map<String, Object>> createFenergoJourney(Message<FenergoJourney> message) {
        logger.info("Creating Fenergo journey: {}", message.getPayload().getJourneyName());
        
        try {
            FenergoJourney journey = message.getPayload();
            
            // Add correlation ID if not present
            String correlationId = (String) message.getHeaders().get("correlationId");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            journey.setCorrelationId(correlationId);
            
            // Create journey in Fenergo
            Map<String, Object> result = fenergoJourneyService.createJourney(journey);
            
            logger.info("Fenergo journey creation completed: {}", result.get("status"));
            
            return MessageBuilder
                    .withPayload(result)
                    .setHeader("correlationId", correlationId)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error creating Fenergo journey", e);
            
            Map<String, Object> errorResult = Map.of(
                "status", "FAILED",
                "message", "Fenergo journey creation failed: " + e.getMessage(),
                "correlationId", message.getHeaders().get("correlationId")
            );
            
            return MessageBuilder
                    .withPayload(errorResult)
                    .setHeader("correlationId", message.getHeaders().get("correlationId"))
                    .build();
        }
    }

    /**
     * Fenergo Task Processing Service Activator
     */
    @ServiceActivator(inputChannel = "fenergoTaskInputChannel")
    public Message<Map<String, Object>> processFenergoTask(Message<Map<String, Object>> message) {
        logger.info("Processing Fenergo task: {}", message.getPayload().get("taskId"));
        
        try {
            Map<String, Object> taskRequest = message.getPayload();
            
            // Add correlation ID if not present
            String correlationId = (String) message.getHeaders().get("correlationId");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            
            String taskType = (String) taskRequest.get("taskType");
            String journeyId = (String) taskRequest.get("journeyId");
            String taskId = (String) taskRequest.get("taskId");
            String status = (String) taskRequest.get("status");
            
            // Update task status in Fenergo
            Map<String, Object> result = fenergoJourneyService.updateTaskStatus(journeyId, taskId, status);
            
            logger.info("Fenergo task processing completed: {}", result.get("status"));
            
            return MessageBuilder
                    .withPayload(result)
                    .setHeader("correlationId", correlationId)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error processing Fenergo task", e);
            
            Map<String, Object> errorResult = Map.of(
                "status", "FAILED",
                "message", "Fenergo task processing failed: " + e.getMessage(),
                "correlationId", message.getHeaders().get("correlationId")
            );
            
            return MessageBuilder
                    .withPayload(errorResult)
                    .setHeader("correlationId", message.getHeaders().get("correlationId"))
                    .build();
        }
    }

    /**
     * Start Fenergo Journey Service Activator
     */
    @ServiceActivator(inputChannel = "fenergoJourneyStartChannel")
    public Message<Map<String, Object>> startFenergoJourney(Message<String> message) {
        logger.info("Starting Fenergo journey: {}", message.getPayload());
        
        try {
            String journeyId = message.getPayload();
            
            // Start journey in Fenergo
            Map<String, Object> result = fenergoJourneyService.startJourney(journeyId);
            
            logger.info("Fenergo journey start completed: {}", result.get("status"));
            
            return MessageBuilder
                    .withPayload(result)
                    .copyHeaders(message.getHeaders())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error starting Fenergo journey", e);
            
            Map<String, Object> errorResult = Map.of(
                "status", "FAILED",
                "message", "Fenergo journey start failed: " + e.getMessage(),
                "correlationId", message.getHeaders().get("correlationId")
            );
            
            return MessageBuilder
                    .withPayload(errorResult)
                    .copyHeaders(message.getHeaders())
                    .build();
        }
    }

    /**
     * Complete Fenergo Journey Service Activator
     */
    @ServiceActivator(inputChannel = "fenergoJourneyCompleteChannel")
    public Message<Map<String, Object>> completeFenergoJourney(Message<String> message) {
        logger.info("Completing Fenergo journey: {}", message.getPayload());
        
        try {
            String journeyId = message.getPayload();
            
            // Complete journey in Fenergo
            Map<String, Object> result = fenergoJourneyService.completeJourney(journeyId);
            
            logger.info("Fenergo journey completion completed: {}", result.get("status"));
            
            return MessageBuilder
                    .withPayload(result)
                    .copyHeaders(message.getHeaders())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error completing Fenergo journey", e);
            
            Map<String, Object> errorResult = Map.of(
                "status", "FAILED",
                "message", "Fenergo journey completion failed: " + e.getMessage(),
                "correlationId", message.getHeaders().get("correlationId")
            );
            
            return MessageBuilder
                    .withPayload(errorResult)
                    .copyHeaders(message.getHeaders())
                    .build();
        }
    }
}
