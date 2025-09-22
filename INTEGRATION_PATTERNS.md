# Enterprise Integration Patterns Implementation

This document details the implementation of Enterprise Integration Patterns (EIP) in the Spring Integration Client Onboarding Service.

## Overview

The application demonstrates various EIP patterns to orchestrate complex client onboarding workflows involving multiple external services. Each pattern is implemented using Spring Integration components.

## Pattern Implementations

### 1. Message Router

**Purpose**: Route messages to different channels based on message content or headers.

**Implementation**:
```java
@Bean
public IntegrationFlow validationFlow() {
    return IntegrationFlows
        .from(validationChannel())
        .route(Message.class, this::routeAfterValidation)
        .get();
}

private String routeAfterValidation(Message<?> message) {
    ClientOnboardingResponse response = (ClientOnboardingResponse) message.getPayload();
    if (response.getStatus() == ClientOnboardingResponse.OnboardingStatus.FAILED) {
        return "errorChannel";
    }
    return "parallelProcessingChannel";
}
```

**Use Case**: After validation, route successful requests to parallel processing and failed requests to error handling.

### 2. Message Splitter

**Purpose**: Split a single message into multiple messages for parallel processing.

**Implementation**:
```java
@Bean
public AbstractMessageSplitter parallelServiceSplitter() {
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
```

**Use Case**: Split client onboarding request into parallel service calls for KYC, data processing, reference data, and LES verification.

### 3. Message Filter

**Purpose**: Filter messages based on business rules or conditions.

**Implementation**:
```java
@Bean
public IntegrationFlow validationFlow() {
    return IntegrationFlows
        .from(validationChannel())
        .filter(Message.class, this::isValidationSuccessful)
        .route(Message.class, this::routeAfterValidation)
        .get();
}

private boolean isValidationSuccessful(Message<?> message) {
    ClientOnboardingResponse response = (ClientOnboardingResponse) message.getPayload();
    return response.getStatus() != ClientOnboardingResponse.OnboardingStatus.FAILED;
}
```

**Use Case**: Only allow successfully validated requests to proceed to the next step.

### 4. Message Aggregator

**Purpose**: Combine multiple messages into a single message.

**Implementation**:
```java
@Bean
public IntegrationFlow parallelProcessingFlow() {
    return IntegrationFlows
        .from("parallelProcessingChannel")
        .split(parallelServiceSplitter())
        .route(Message.class, this::routeToService)
        .aggregate(aggregatorSpec -> aggregatorSpec
            .correlationStrategy(m -> m.getHeaders().get("correlationId"))
            .releaseStrategy(group -> group.size() >= 4) // KYC, DataProcessor, RefData, LES
            .outputProcessor(aggregateResults())
        )
        .get();
}

private GenericTransformer<List<Message<?>>, Message<ClientOnboardingResponse>> aggregateResults() {
    return messages -> {
        // Aggregate results from all services
        ClientOnboardingResponse response = new ClientOnboardingResponse();
        response.setStatus(ClientOnboardingResponse.OnboardingStatus.COMPLETED);
        response.setMessage("Client onboarding completed successfully");
        
        // Process individual service results
        for (Message<?> msg : messages) {
            // Add service results to response
        }
        
        return MessageBuilder
                .withPayload(response)
                .setHeader("correlationId", messages.get(0).getHeaders().get("correlationId"))
                .build();
    };
}
```

**Use Case**: Aggregate results from all parallel services into a single response.

### 5. Service Activator

**Purpose**: Process messages through business logic components.

**Implementation**:
```java
@Bean
public IntegrationFlow kycFlow() {
    return IntegrationFlows
        .from(kycChannel())
        .handle(kycService, "verifyClient")
        .get();
}

@Bean
public IntegrationFlow dataProcessorFlow() {
    return IntegrationFlows
        .from(dataProcessorChannel())
        .handle(dataProcessorService, "processClientData")
        .get();
}
```

**Use Case**: Process messages through specific business services (KYC, data processing, etc.).

### 6. Message Transformer

**Purpose**: Transform message content or structure.

**Implementation**:
```java
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
```

**Use Case**: Transform client onboarding request into validation response.

### 7. Message Enricher

**Purpose**: Enrich messages with additional data.

**Implementation**:
```java
@Bean
public IntegrationFlow clientOnboardingFlow() {
    return IntegrationFlows
        .from(clientOnboardingInputChannel())
        .enrichHeaders(h -> h.header("correlationId", 
            m -> java.util.UUID.randomUUID().toString()))
        .route(Message.class, this::routeToValidation)
        .get();
}
```

**Use Case**: Add correlation ID and other metadata to messages.

### 8. Error Handling Pattern

**Purpose**: Handle errors gracefully and provide recovery mechanisms.

**Implementation**:
```java
@Bean
public IntegrationFlow errorHandlingFlow() {
    return IntegrationFlows
        .from(errorChannel())
        .log(LoggingHandler.Level.ERROR, "Error occurred in onboarding process")
        .transform(createErrorResponse())
        .channel(responseChannel())
        .get();
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
```

**Use Case**: Handle errors from any step in the integration flow.

### 9. Circuit Breaker Pattern

**Purpose**: Prevent cascading failures by breaking the circuit when services are down.

**Implementation**:
```java
@Bean
public IntegrationFlow kycFlow() {
    return IntegrationFlows
        .from(kycChannel())
        .handle(kycService, "verifyClient")
        .circuitBreaker(cb -> cb
            .failureThreshold(5)
            .timeout(Duration.ofSeconds(30))
            .recoveryTimeout(Duration.ofSeconds(10))
        )
        .get();
}
```

**Use Case**: Protect against external service failures.

### 10. Retry Pattern

**Purpose**: Automatically retry failed operations.

**Implementation**:
```java
@Bean
public IntegrationFlow kycFlow() {
    return IntegrationFlows
        .from(kycChannel())
        .handle(kycService, "verifyClient")
        .retry(retrySpec -> retrySpec
            .maxAttempts(3)
            .backoff(Duration.ofSeconds(1), Duration.ofSeconds(5))
        )
        .get();
}
```

**Use Case**: Handle transient failures in external service calls.

## Flow Orchestration

### Complete Integration Flow

```
Client Request
    ↓
[Message Enricher] - Add correlation ID
    ↓
[Message Router] - Route to validation
    ↓
[Service Activator] - Validation Service
    ↓
[Message Filter] - Filter successful validations
    ↓
[Message Router] - Route to parallel processing
    ↓
[Message Splitter] - Split into parallel service calls
    ↓
[Service Activator] - KYC Service
[Service Activator] - Data Processor Service
[Service Activator] - Reference Data Service
[Service Activator] - LES Service
    ↓
[Message Aggregator] - Aggregate results
    ↓
[Service Activator] - Notification Service
    ↓
[Message Transformer] - Create final response
    ↓
Response to Client
```

### Error Flow

```
Any Error
    ↓
[Message Router] - Route to error channel
    ↓
[Error Handler] - Log and transform error
    ↓
[Message Transformer] - Create error response
    ↓
Response to Client
```

## Benefits of This Implementation

1. **Scalability**: Parallel processing of independent services
2. **Resilience**: Circuit breakers and retry mechanisms
3. **Maintainability**: Clear separation of concerns
4. **Observability**: Comprehensive logging and monitoring
5. **Flexibility**: Easy to add/remove services
6. **Error Handling**: Graceful error handling and recovery
7. **Performance**: Asynchronous processing and aggregation

## Configuration

### Channel Configuration
```java
@Bean
public MessageChannel clientOnboardingInputChannel() {
    return MessageChannels.direct("clientOnboardingInput").get();
}

@Bean
public MessageChannel validationChannel() {
    return MessageChannels.direct("validation").get();
}
```

### Flow Configuration
```java
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
```

## Testing Integration Patterns

### Unit Testing
```java
@Test
public void testMessageRouter() {
    // Test routing logic
    String channel = routeAfterValidation(message);
    assertEquals("parallelProcessingChannel", channel);
}

@Test
public void testMessageSplitter() {
    // Test splitting logic
    List<Message<?>> messages = splitter.splitMessage(message);
    assertEquals(4, messages.size());
}
```

### Integration Testing
```java
@SpringBootTest
@AutoConfigureTestDatabase
class IntegrationFlowTest {
    
    @Test
    public void testCompleteFlow() {
        // Test complete integration flow
    }
}
```

This implementation demonstrates how Enterprise Integration Patterns can be effectively used to build robust, scalable, and maintainable integration solutions using Spring Integration.
