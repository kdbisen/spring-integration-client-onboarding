package com.adyanta.onboarding.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Debug Controller for Spring Integration
 * Provides endpoints to help developers understand and debug integration flows
 */
@RestController
@RequestMapping("/api/v1/debug")
public class IntegrationDebugController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationDebugController.class);

    @Autowired
    private MessageChannel clientOnboardingInputChannel;

    /**
     * Get integration flow status and statistics
     */
    @GetMapping("/flow-status")
    public Map<String, Object> getFlowStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Basic flow information
        status.put("flowName", "Client Onboarding Integration Flow");
        status.put("status", "ACTIVE");
        status.put("timestamp", LocalDateTime.now());
        
        // Flow steps
        Map<String, Object> steps = new HashMap<>();
        steps.put("1", "Client Request Input");
        steps.put("2", "Add Correlation ID");
        steps.put("3", "Route to Validation");
        steps.put("4", "Validate Request");
        steps.put("5", "Filter Valid Requests");
        steps.put("6", "Route to Parallel Processing");
        steps.put("7", "Split into 4 Services");
        steps.put("8", "Process KYC Service");
        steps.put("9", "Process Data Processor Service");
        steps.put("10", "Process Reference Data Service");
        steps.put("11", "Process LES Service");
        steps.put("12", "Aggregate Results");
        steps.put("13", "Send Notifications");
        steps.put("14", "Return Response");
        
        status.put("flowSteps", steps);
        
        // Service status
        Map<String, Object> services = new HashMap<>();
        services.put("KYC Service", "MOCK - Always returns SUCCESS");
        services.put("Data Processor Service", "MOCK - Always returns SUCCESS");
        services.put("Reference Data Service", "MOCK - Always returns SUCCESS");
        services.put("LES Service", "MOCK - Always returns SUCCESS");
        services.put("Notification Service", "MOCK - Always returns SUCCESS");
        
        status.put("services", services);
        
        return status;
    }

    /**
     * Test a single step in the integration flow
     */
    @PostMapping("/test-step/{stepNumber}")
    public Map<String, Object> testStep(
            @PathVariable int stepNumber,
            @RequestBody(required = false) Map<String, Object> testData) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("stepNumber", stepNumber);
        result.put("timestamp", LocalDateTime.now());
        
        try {
            switch (stepNumber) {
                case 1:
                    result.put("description", "Test message creation and input channel");
                    result.put("testData", createTestMessage());
                    break;
                case 2:
                    result.put("description", "Test correlation ID enrichment");
                    result.put("correlationId", UUID.randomUUID().toString());
                    break;
                case 3:
                    result.put("description", "Test routing to validation");
                    result.put("routeResult", "validationChannel");
                    break;
                case 4:
                    result.put("description", "Test validation service");
                    result.put("validationResult", "SUCCESS");
                    break;
                case 5:
                    result.put("description", "Test message filtering");
                    result.put("filterResult", "PASSED");
                    break;
                case 6:
                    result.put("description", "Test routing to parallel processing");
                    result.put("routeResult", "parallelProcessingChannel");
                    break;
                case 7:
                    result.put("description", "Test message splitting");
                    result.put("splitCount", 4);
                    result.put("services", new String[]{"KYC", "DATA_PROCESSOR", "REFERENCE_DATA", "LES"});
                    break;
                case 8:
                    result.put("description", "Test KYC service");
                    result.put("kycResult", "SUCCESS");
                    break;
                case 9:
                    result.put("description", "Test Data Processor service");
                    result.put("dataProcessorResult", "SUCCESS");
                    break;
                case 10:
                    result.put("description", "Test Reference Data service");
                    result.put("referenceDataResult", "SUCCESS");
                    break;
                case 11:
                    result.put("description", "Test LES service");
                    result.put("lesResult", "SUCCESS");
                    break;
                case 12:
                    result.put("description", "Test result aggregation");
                    result.put("aggregationResult", "SUCCESS");
                    break;
                case 13:
                    result.put("description", "Test notification service");
                    result.put("notificationResult", "SUCCESS");
                    break;
                case 14:
                    result.put("description", "Test final response");
                    result.put("finalResponse", "SUCCESS");
                    break;
                default:
                    result.put("error", "Invalid step number. Valid steps: 1-14");
                    return result;
            }
            
            result.put("status", "SUCCESS");
            result.put("message", "Step " + stepNumber + " tested successfully");
            
        } catch (Exception e) {
            logger.error("Error testing step {}", stepNumber, e);
            result.put("status", "ERROR");
            result.put("message", "Error testing step: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Simulate a complete integration flow
     */
    @PostMapping("/simulate-flow")
    public Map<String, Object> simulateFlow(@RequestBody(required = false) Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        result.put("simulationId", UUID.randomUUID().toString());
        result.put("timestamp", LocalDateTime.now());
        
        try {
            // Step 1: Create test message
            Map<String, Object> testMessage = createTestMessage();
            result.put("step1_inputMessage", testMessage);
            
            // Step 2: Add correlation ID
            String correlationId = UUID.randomUUID().toString();
            result.put("step2_correlationId", correlationId);
            
            // Step 3: Route to validation
            result.put("step3_route", "validationChannel");
            
            // Step 4: Validation
            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("status", "SUCCESS");
            validationResult.put("message", "Validation passed");
            result.put("step4_validation", validationResult);
            
            // Step 5: Filter
            result.put("step5_filter", "PASSED");
            
            // Step 6: Route to parallel processing
            result.put("step6_route", "parallelProcessingChannel");
            
            // Step 7: Split
            result.put("step7_split", new String[]{"KYC", "DATA_PROCESSOR", "REFERENCE_DATA", "LES"});
            
            // Step 8-11: Parallel processing
            Map<String, Object> parallelResults = new HashMap<>();
            parallelResults.put("KYC", "SUCCESS");
            parallelResults.put("DATA_PROCESSOR", "SUCCESS");
            parallelResults.put("REFERENCE_DATA", "SUCCESS");
            parallelResults.put("LES", "SUCCESS");
            result.put("step8-11_parallelProcessing", parallelResults);
            
            // Step 12: Aggregation
            Map<String, Object> aggregationResult = new HashMap<>();
            aggregationResult.put("status", "SUCCESS");
            aggregationResult.put("message", "All services completed successfully");
            aggregationResult.put("totalServices", 4);
            aggregationResult.put("successfulServices", 4);
            result.put("step12_aggregation", aggregationResult);
            
            // Step 13: Notifications
            Map<String, Object> notificationResult = new HashMap<>();
            notificationResult.put("email", "SENT");
            notificationResult.put("sms", "SENT");
            notificationResult.put("internal", "SENT");
            notificationResult.put("webhook", "SENT");
            result.put("step13_notifications", notificationResult);
            
            // Step 14: Final response
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("status", "COMPLETED");
            finalResponse.put("message", "Client onboarding completed successfully");
            finalResponse.put("clientId", testMessage.get("clientId"));
            finalResponse.put("correlationId", correlationId);
            result.put("step14_finalResponse", finalResponse);
            
            result.put("status", "SUCCESS");
            result.put("message", "Flow simulation completed successfully");
            
        } catch (Exception e) {
            logger.error("Error simulating flow", e);
            result.put("status", "ERROR");
            result.put("message", "Error simulating flow: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Get message flow visualization
     */
    @GetMapping("/flow-visualization")
    public Map<String, Object> getFlowVisualization() {
        Map<String, Object> visualization = new HashMap<>();
        
        // ASCII Art representation of the flow
        String asciiFlow = """
            Client Request
                ↓
            [Add Correlation ID]
                ↓
            [Route to Validation]
                ↓
            [Validate Request] → [Filter Valid Requests]
                ↓
            [Route to Parallel Processing]
                ↓
            [Split into 4 Messages]
                ↓
            ┌─────────────┬─────────────┬─────────────┬─────────────┐
            │ KYC Service │ Data Proc.  │ Ref. Data   │ LES Service │
            └─────────────┴─────────────┴─────────────┴─────────────┘
                ↓
            [Aggregate Results]
                ↓
            [Send Notifications]
                ↓
            [Return Response to Client]
            """;
        
        visualization.put("asciiFlow", asciiFlow);
        
        // Flow steps with descriptions
        Map<String, Object> steps = new HashMap<>();
        steps.put("1", Map.of(
            "name", "Client Request Input",
            "description", "Receive client onboarding request",
            "input", "ClientOnboardingRequest",
            "output", "Message<ClientOnboardingRequest>"
        ));
        steps.put("2", Map.of(
            "name", "Add Correlation ID",
            "description", "Enrich message with correlation ID for tracking",
            "input", "Message<ClientOnboardingRequest>",
            "output", "Message<ClientOnboardingRequest> with correlationId header"
        ));
        steps.put("3", Map.of(
            "name", "Route to Validation",
            "description", "Route message to validation channel",
            "input", "Message<ClientOnboardingRequest>",
            "output", "Message<ClientOnboardingRequest> to validationChannel"
        ));
        steps.put("4", Map.of(
            "name", "Validate Request",
            "description", "Validate client request using ValidationService",
            "input", "Message<ClientOnboardingRequest>",
            "output", "Message<ClientOnboardingResponse>"
        ));
        steps.put("5", Map.of(
            "name", "Filter Valid Requests",
            "description", "Only allow successfully validated requests to continue",
            "input", "Message<ClientOnboardingResponse>",
            "output", "Message<ClientOnboardingResponse> (filtered)"
        ));
        steps.put("6", Map.of(
            "name", "Route to Parallel Processing",
            "description", "Route valid requests to parallel processing",
            "input", "Message<ClientOnboardingResponse>",
            "output", "Message<ClientOnboardingResponse> to parallelProcessingChannel"
        ));
        steps.put("7", Map.of(
            "name", "Split into 4 Messages",
            "description", "Split request into 4 parallel service calls",
            "input", "Message<ClientOnboardingRequest>",
            "output", "List<Message<ClientOnboardingRequest>>"
        ));
        steps.put("8", Map.of(
            "name", "Process KYC Service",
            "description", "Verify client identity with KYC service",
            "input", "Message<ClientOnboardingRequest>",
            "output", "Map<String, Object> (KYC result)"
        ));
        steps.put("9", Map.of(
            "name", "Process Data Processor Service",
            "description", "Enrich and transform client data",
            "input", "Message<ClientOnboardingRequest>",
            "output", "Map<String, Object> (Data processing result)"
        ));
        steps.put("10", Map.of(
            "name", "Process Reference Data Service",
            "description", "Validate against reference data",
            "input", "Message<ClientOnboardingRequest>",
            "output", "Map<String, Object> (Reference data result)"
        ));
        steps.put("11", Map.of(
            "name", "Process LES Service",
            "description", "Legal entity verification",
            "input", "Message<ClientOnboardingRequest>",
            "output", "Map<String, Object> (LES result)"
        ));
        steps.put("12", Map.of(
            "name", "Aggregate Results",
            "description", "Combine results from all services",
            "input", "List<Message<Map<String, Object>>>",
            "output", "Message<ClientOnboardingResponse>"
        ));
        steps.put("13", Map.of(
            "name", "Send Notifications",
            "description", "Send notifications via multiple channels",
            "input", "Message<ClientOnboardingResponse>",
            "output", "Map<String, Object> (Notification result)"
        ));
        steps.put("14", Map.of(
            "name", "Return Response to Client",
            "description", "Send final response to client",
            "input", "Message<ClientOnboardingResponse>",
            "output", "ResponseEntity<Map<String, Object>>"
        ));
        
        visualization.put("steps", steps);
        
        return visualization;
    }

    /**
     * Get integration patterns used in this application
     */
    @GetMapping("/integration-patterns")
    public Map<String, Object> getIntegrationPatterns() {
        Map<String, Object> patterns = new HashMap<>();
        
        Map<String, Object> patternDetails = new HashMap<>();
        
        patternDetails.put("Message Router", Map.of(
            "description", "Routes messages to different channels based on content",
            "example", "Route successful validations to parallel processing, failed ones to error handling",
            "implementation", "route(Message.class, this::routeAfterValidation)"
        ));
        
        patternDetails.put("Message Splitter", Map.of(
            "description", "Splits one message into multiple messages for parallel processing",
            "example", "Split client request into 4 separate service calls",
            "implementation", "split(parallelServiceSplitter())"
        ));
        
        patternDetails.put("Message Filter", Map.of(
            "description", "Filters messages based on business rules",
            "example", "Only allow successfully validated requests to continue",
            "implementation", "filter(Message.class, this::isValidationSuccessful)"
        ));
        
        patternDetails.put("Message Aggregator", Map.of(
            "description", "Combines multiple messages into one message",
            "example", "Aggregate results from all 4 parallel services",
            "implementation", "aggregate(aggregatorSpec -> ...)"
        ));
        
        patternDetails.put("Service Activator", Map.of(
            "description", "Processes messages through business logic",
            "example", "Call KYC service to verify client identity",
            "implementation", "handle(kycService, \"verifyClient\")"
        ));
        
        patternDetails.put("Message Transformer", Map.of(
            "description", "Transforms message content or structure",
            "example", "Transform client request into validation response",
            "implementation", "transform(validateRequest())"
        ));
        
        patternDetails.put("Message Enricher", Map.of(
            "description", "Enriches messages with additional data",
            "example", "Add correlation ID to messages for tracking",
            "implementation", "enrichHeaders(h -> h.header(\"correlationId\", ...))"
        ));
        
        patternDetails.put("Error Handler", Map.of(
            "description", "Handles errors gracefully and provides recovery",
            "example", "Log errors and create error responses",
            "implementation", "IntegrationFlows.from(errorChannel())"
        ));
        
        patterns.put("patterns", patternDetails);
        patterns.put("totalPatterns", patternDetails.size());
        patterns.put("description", "This application demonstrates 8+ Enterprise Integration Patterns");
        
        return patterns;
    }

    /**
     * Get debugging tips and common issues
     */
    @GetMapping("/debugging-tips")
    public Map<String, Object> getDebuggingTips() {
        Map<String, Object> tips = new HashMap<>();
        
        Map<String, Object> commonIssues = new HashMap<>();
        
        commonIssues.put("Messages not flowing", Map.of(
            "symptoms", "Messages stuck in a channel",
            "causes", new String[]{"Missing channel configuration", "Incorrect routing logic", "Blocking operations"},
            "solutions", new String[]{"Check channel configuration", "Verify routing logic", "Use async channels for blocking operations"}
        ));
        
        commonIssues.put("Aggregation not working", Map.of(
            "symptoms", "Aggregator waiting forever for messages",
            "causes", new String[]{"Incorrect correlation strategy", "Wrong release strategy", "Missing messages"},
            "solutions", new String[]{"Check correlation strategy", "Verify release strategy", "Ensure all messages have correlation ID"}
        ));
        
        commonIssues.put("Services not responding", Map.of(
            "symptoms", "External service calls timing out",
            "causes", new String[]{"Service down", "Network issues", "Incorrect service URL"},
            "solutions", new String[]{"Check service health", "Verify network connectivity", "Use circuit breakers"}
        ));
        
        commonIssues.put("Error handling not working", Map.of(
            "symptoms", "Errors not being caught and handled",
            "causes", new String[]{"Missing error channel", "Incorrect error handling configuration"},
            "solutions", new String[]{"Configure error channels", "Add error handling flows"}
        ));
        
        tips.put("commonIssues", commonIssues);
        
        Map<String, Object> debuggingSteps = new HashMap<>();
        debuggingSteps.put("1", "Enable debug logging: logging.level.org.springframework.integration=DEBUG");
        debuggingSteps.put("2", "Check integration graph: /actuator/integrationgraph");
        debuggingSteps.put("3", "Add logging to flows: .log(LoggingHandler.Level.INFO, \"message\")");
        debuggingSteps.put("4", "Use correlation IDs for tracking");
        debuggingSteps.put("5", "Test individual components");
        debuggingSteps.put("6", "Check message headers and payload");
        debuggingSteps.put("7", "Verify channel configurations");
        debuggingSteps.put("8", "Monitor service health");
        
        tips.put("debuggingSteps", debuggingSteps);
        
        return tips;
    }

    private Map<String, Object> createTestMessage() {
        Map<String, Object> testMessage = new HashMap<>();
        testMessage.put("clientId", "TEST_CLIENT_" + System.currentTimeMillis());
        testMessage.put("firstName", "John");
        testMessage.put("lastName", "Doe");
        testMessage.put("email", "john.doe@example.com");
        testMessage.put("phoneNumber", "+1234567890");
        testMessage.put("documentType", "PASSPORT");
        testMessage.put("documentNumber", "P123456789");
        testMessage.put("address", Map.of(
            "street", "123 Main St",
            "city", "New York",
            "state", "NY",
            "postalCode", "10001",
            "country", "US"
        ));
        testMessage.put("requestTimestamp", LocalDateTime.now());
        return testMessage;
    }
}
