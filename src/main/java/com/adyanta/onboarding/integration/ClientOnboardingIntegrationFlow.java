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
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main Integration Flow Configuration for Client Onboarding
 * Demonstrates Enterprise Integration Patterns:
 * - Message Router
 * - Message Splitter
 * - Message Filter
 * - Message Aggregator
 * - Service Activator
 * - Error Handling
 */
@Configuration
@EnableIntegration
public class ClientOnboardingIntegrationFlow {

    private static final Logger logger = LoggerFactory.getLogger(ClientOnboardingIntegrationFlow.class);

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
        return MessageChannels.direct("clientOnboardingInput").get();
    }

    @Bean
    public MessageChannel validationChannel() {
        return MessageChannels.direct("validation").get();
    }

    @Bean
    public MessageChannel kycChannel() {
        return MessageChannels.direct("kyc").get();
    }

    @Bean
    public MessageChannel dataProcessorChannel() {
        return MessageChannels.direct("dataProcessor").get();
    }

    @Bean
    public MessageChannel referenceDataChannel() {
        return MessageChannels.direct("referenceData").get();
    }

    @Bean
    public MessageChannel lesChannel() {
        return MessageChannels.direct("les").get();
    }

    @Bean
    public MessageChannel notificationChannel() {
        return MessageChannels.direct("notification").get();
    }

    @Bean
    public MessageChannel errorChannel() {
        return MessageChannels.direct("error").get();
    }

    @Bean
    public MessageChannel responseChannel() {
        return MessageChannels.direct("response").get();
    }

    /**
     * Main Integration Flow - Orchestrates the entire client onboarding process
     */
    @Bean
    public IntegrationFlow clientOnboardingFlow() {
        return IntegrationFlows
                .from(clientOnboardingInputChannel())
                .log(LoggingHandler.Level.INFO, "Received client onboarding request")
                .enrichHeaders(h -> h.header("correlationId", 
                    m -> java.util.UUID.randomUUID().toString()))
                .route(Message.class, this::routeToValidation)
                .get();
    }

    /**
     * Validation Flow - Validates the incoming request
     */
    @Bean
    public IntegrationFlow validationFlow() {
        return IntegrationFlows
                .from(validationChannel())
                .log(LoggingHandler.Level.INFO, "Starting validation process")
                .transform(validateRequest())
                .filter(Message.class, this::isValidationSuccessful)
                .route(Message.class, this::routeAfterValidation)
                .get();
    }

    /**
     * Parallel Processing Flow - Processes multiple services in parallel
     */
    @Bean
    public IntegrationFlow parallelProcessingFlow() {
        return IntegrationFlows
                .from("parallelProcessingChannel")
                .log(LoggingHandler.Level.INFO, "Starting parallel processing")
                .split(parallelServiceSplitter())
                .route(Message.class, this::routeToService)
                .aggregate(aggregatorSpec -> aggregatorSpec
                    .correlationStrategy(m -> m.getHeaders().get("correlationId"))
                    .releaseStrategy(group -> group.size() >= 4) // KYC, DataProcessor, RefData, LES
                    .outputProcessor(aggregateResults())
                )
                .log(LoggingHandler.Level.INFO, "Parallel processing completed")
                .route(Message.class, this::routeAfterParallelProcessing)
                .get();
    }

    /**
     * KYC Service Flow
     */
    @Bean
    public IntegrationFlow kycFlow() {
        return IntegrationFlows
                .from(kycChannel())
                .log(LoggingHandler.Level.INFO, "Processing KYC verification")
                .handle(kycService, "verifyClient")
                .log(LoggingHandler.Level.INFO, "KYC verification completed")
                .channel("aggregationChannel")
                .get();
    }

    /**
     * Data Processor Service Flow
     */
    @Bean
    public IntegrationFlow dataProcessorFlow() {
        return IntegrationFlows
                .from(dataProcessorChannel())
                .log(LoggingHandler.Level.INFO, "Processing client data")
                .handle(dataProcessorService, "processClientData")
                .log(LoggingHandler.Level.INFO, "Data processing completed")
                .channel("aggregationChannel")
                .get();
    }

    /**
     * Reference Data Service Flow
     */
    @Bean
    public IntegrationFlow referenceDataFlow() {
        return IntegrationFlows
                .from(referenceDataChannel())
                .log(LoggingHandler.Level.INFO, "Fetching reference data")
                .handle(referenceDataService, "fetchReferenceData")
                .log(LoggingHandler.Level.INFO, "Reference data fetched")
                .channel("aggregationChannel")
                .get();
    }

    /**
     * LES Service Flow
     */
    @Bean
    public IntegrationFlow lesFlow() {
        return IntegrationFlows
                .from(lesChannel())
                .log(LoggingHandler.Level.INFO, "Processing LES verification")
                .handle(lesService, "verifyWithLes")
                .log(LoggingHandler.Level.INFO, "LES verification completed")
                .channel("aggregationChannel")
                .get();
    }

    /**
     * Notification Flow
     */
    @Bean
    public IntegrationFlow notificationFlow() {
        return IntegrationFlows
                .from(notificationChannel())
                .log(LoggingHandler.Level.INFO, "Sending notifications")
                .handle(notificationService, "sendNotification")
                .log(LoggingHandler.Level.INFO, "Notifications sent")
                .channel(responseChannel())
                .get();
    }

    /**
     * Error Handling Flow
     */
    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlows
                .from(errorChannel())
                .log(LoggingHandler.Level.ERROR, "Error occurred in onboarding process")
                .transform(createErrorResponse())
                .channel(responseChannel())
                .get();
    }

    // Helper Methods

    private String routeToValidation(Message<?> message) {
        return "validationChannel";
    }

    private String routeAfterValidation(Message<?> message) {
        ClientOnboardingResponse response = (ClientOnboardingResponse) message.getPayload();
        if (response.getStatus() == ClientOnboardingResponse.OnboardingStatus.FAILED) {
            return "errorChannel";
        }
        return "parallelProcessingChannel";
    }

    private String routeToService(Message<?> message) {
        String serviceType = (String) message.getHeaders().get("serviceType");
        return switch (serviceType) {
            case "KYC" -> "kycChannel";
            case "DATA_PROCESSOR" -> "dataProcessorChannel";
            case "REFERENCE_DATA" -> "referenceDataChannel";
            case "LES" -> "lesChannel";
            default -> "errorChannel";
        };
    }

    private String routeAfterParallelProcessing(Message<?> message) {
        return "notificationChannel";
    }

    private boolean isValidationSuccessful(Message<?> message) {
        ClientOnboardingResponse response = (ClientOnboardingResponse) message.getPayload();
        return response.getStatus() != ClientOnboardingResponse.OnboardingStatus.FAILED;
    }

    private GenericTransformer<Message<ClientOnboardingRequest>, Message<ClientOnboardingResponse>> validateRequest() {
        return message -> {
            try {
                ClientOnboardingRequest request = message.getPayload();
                ClientOnboardingResponse response = validationService.validateRequest(request);
                return MessageBuilder
                        .withPayload(response)
                        .copyHeaders(message.getHeaders())
                        .build();
            } catch (Exception e) {
                logger.error("Validation failed", e);
                ClientOnboardingResponse errorResponse = new ClientOnboardingResponse();
                errorResponse.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
                errorResponse.setMessage("Validation failed: " + e.getMessage());
                return MessageBuilder
                        .withPayload(errorResponse)
                        .copyHeaders(message.getHeaders())
                        .build();
            }
        };
    }

    private AbstractMessageSplitter parallelServiceSplitter() {
        return new AbstractMessageSplitter() {
            @Override
            protected Object splitMessage(Message<?> message) {
                ClientOnboardingRequest request = (ClientOnboardingRequest) message.getPayload();
                String correlationId = (String) message.getHeaders().get("correlationId");
                
                return Arrays.asList(
                    MessageBuilder.withPayload(request)
                        .setHeader("serviceType", "KYC")
                        .setHeader("correlationId", correlationId)
                        .build(),
                    MessageBuilder.withPayload(request)
                        .setHeader("serviceType", "DATA_PROCESSOR")
                        .setHeader("correlationId", correlationId)
                        .build(),
                    MessageBuilder.withPayload(request)
                        .setHeader("serviceType", "REFERENCE_DATA")
                        .setHeader("correlationId", correlationId)
                        .build(),
                    MessageBuilder.withPayload(request)
                        .setHeader("serviceType", "LES")
                        .setHeader("correlationId", correlationId)
                        .build()
                );
            }
        };
    }

    private GenericTransformer<List<Message<?>>, Message<ClientOnboardingResponse>> aggregateResults() {
        return messages -> {
            try {
                // Aggregate results from all services
                ClientOnboardingResponse response = new ClientOnboardingResponse();
                response.setStatus(ClientOnboardingResponse.OnboardingStatus.COMPLETED);
                response.setMessage("Client onboarding completed successfully");
                
                // Process individual service results
                for (Message<?> msg : messages) {
                    // Add service results to response
                    // Implementation depends on service response format
                }
                
                return MessageBuilder
                        .withPayload(response)
                        .setHeader("correlationId", messages.get(0).getHeaders().get("correlationId"))
                        .build();
            } catch (Exception e) {
                logger.error("Error aggregating results", e);
                ClientOnboardingResponse errorResponse = new ClientOnboardingResponse();
                errorResponse.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
                errorResponse.setMessage("Error aggregating service results: " + e.getMessage());
                return MessageBuilder
                        .withPayload(errorResponse)
                        .build();
            }
        };
    }

    private GenericTransformer<Message<?>, Message<ClientOnboardingResponse>> createErrorResponse() {
        return message -> {
            ClientOnboardingResponse errorResponse = new ClientOnboardingResponse();
            errorResponse.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
            errorResponse.setMessage("Client onboarding failed");
            errorResponse.setCorrelationId((String) message.getHeaders().get("correlationId"));
            
            return MessageBuilder
                    .withPayload(errorResponse)
                    .copyHeaders(message.getHeaders())
                    .build();
        };
    }
}
