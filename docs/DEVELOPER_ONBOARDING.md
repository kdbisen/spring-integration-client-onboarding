# Developer Onboarding Guide

## Welcome to Spring Integration Client Onboarding Service! 🚀

This guide will help you understand and work with this Spring Integration application, even if you're new to Spring Integration.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Understanding the Architecture](#understanding-the-architecture)
3. [Key Concepts](#key-concepts)
4. [How to Debug](#how-to-debug)
5. [Common Tasks](#common-tasks)
6. [Testing](#testing)
7. [Troubleshooting](#troubleshooting)

## Quick Start

### 1. Run the Application
```bash
# Clone the repository
git clone <repository-url>
cd spring-integration-client-onboarding

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Test the API
```bash
# Test the health endpoint
curl http://localhost:8080/client-onboarding/api/v1/onboarding/health

# Test the debug endpoint
curl http://localhost:8080/client-onboarding/api/v1/debug/flow-status
```

### 3. Explore the Integration Graph
Visit: `http://localhost:8080/client-onboarding/actuator/integrationgraph`

This shows you a visual representation of your integration flows!

## Understanding the Architecture

### What This Application Does

This application demonstrates a **client onboarding process** for a bank or financial institution. When a new client wants to open an account, the system needs to:

1. **Validate** the client's information
2. **Verify** their identity (KYC - Know Your Customer)
3. **Process** their data for various systems
4. **Check** against reference data and regulations
5. **Verify** with legal entity systems
6. **Send** notifications to the client and internal teams

### Why Spring Integration?

Instead of writing complex code to coordinate all these services, Spring Integration provides **patterns** that make this easier:

- **Message Router**: Decides which path to take
- **Message Splitter**: Breaks work into parallel tasks
- **Message Aggregator**: Combines results back together
- **Service Activator**: Calls your business logic
- **Error Handler**: Handles problems gracefully

## Key Concepts

### 1. Messages
Everything in Spring Integration is a **Message**:
```java
Message<ClientOnboardingRequest> message = MessageBuilder
    .withPayload(clientRequest)           // The actual data
    .setHeader("correlationId", "12345")  // Metadata
    .build();
```

### 2. Channels
Channels are like **pipes** that carry messages:
```java
@Bean
public MessageChannel validationChannel() {
    return MessageChannels.direct("validation").get();
}
```

### 3. Integration Flows
Flows define **how messages move** through your system:
```java
@Bean
public IntegrationFlow clientOnboardingFlow() {
    return IntegrationFlows
        .from(clientOnboardingInputChannel())  // Start here
        .log(LoggingHandler.Level.INFO, "Received request")  // Log what's happening
        .route(Message.class, this::routeToValidation)  // Decide where to go next
        .get();
}
```

## How to Debug

### 1. Enable Debug Logging
Add this to your `application.yml`:
```yaml
logging:
  level:
    org.springframework.integration: DEBUG
    com.adyanta.onboarding: DEBUG
```

### 2. Use the Debug Controller
The application includes a debug controller with helpful endpoints:

```bash
# Get flow status
curl http://localhost:8080/client-onboarding/api/v1/debug/flow-status

# Test individual steps
curl -X POST http://localhost:8080/client-onboarding/api/v1/debug/test-step/1

# Simulate the complete flow
curl -X POST http://localhost:8080/client-onboarding/api/v1/debug/simulate-flow

# Get flow visualization
curl http://localhost:8080/client-onboarding/api/v1/debug/flow-visualization

# Get integration patterns explanation
curl http://localhost:8080/client-onboarding/api/v1/debug/integration-patterns

# Get debugging tips
curl http://localhost:8080/client-onboarding/api/v1/debug/debugging-tips
```

### 3. Check the Integration Graph
Visit: `http://localhost:8080/client-onboarding/actuator/integrationgraph`

This shows you a visual representation of your integration flows!

### 4. Monitor Messages
Add logging to your flows:
```java
@Bean
public IntegrationFlow clientOnboardingFlow() {
    return IntegrationFlows
        .from(clientOnboardingInputChannel())
        .log(LoggingHandler.Level.INFO, "Received client onboarding request")
        .enrichHeaders(h -> h.header("correlationId", 
            m -> java.util.UUID.randomUUID().toString()))
        .log(LoggingHandler.Level.INFO, "Added correlation ID: {}", 
            m -> m.getHeaders().get("correlationId"))
        .route(Message.class, this::routeToValidation)
        .get();
}
```

## Common Tasks

### Adding a New Service

1. **Create the Service Class**:
```java
@Service
public class NewService {
    public Map<String, Object> processRequest(ClientOnboardingRequest request) {
        // Your business logic here
        return result;
    }
}
```

2. **Add a Channel**:
```java
@Bean
public MessageChannel newServiceChannel() {
    return MessageChannels.direct("newService").get();
}
```

3. **Add a Flow**:
```java
@Bean
public IntegrationFlow newServiceFlow() {
    return IntegrationFlows
        .from(newServiceChannel())
        .handle(newService, "processRequest")
        .get();
}
```

4. **Update the Splitter**:
```java
private AbstractMessageSplitter parallelServiceSplitter() {
    return new AbstractMessageSplitter() {
        @Override
        protected Object splitMessage(Message<?> message) {
            return Arrays.asList(
                createKycMessage(message),
                createDataProcessorMessage(message),
                createReferenceDataMessage(message),
                createLesMessage(message),
                createNewServiceMessage(message)  // Add this
            );
        }
    };
}
```

5. **Update the Aggregator**:
```java
.releaseStrategy(group -> group.size() >= 5)  // Change from 4 to 5
```

### Modifying Validation Rules

1. **Update the ValidationService**:
```java
private ClientOnboardingResponse.StepResult validateBusinessRules(ClientOnboardingRequest request) {
    // Add your new validation rules here
    if (newValidationRule(request)) {
        return new ClientOnboardingResponse.StepResult(
            "businessValidation", 
            "FAILED", 
            "New validation rule failed"
        );
    }
    // ... existing validation
}
```

### Adding Error Handling

1. **Create an Error Handler**:
```java
@Bean
public IntegrationFlow errorHandlingFlow() {
    return IntegrationFlows
        .from(errorChannel())
        .log(LoggingHandler.Level.ERROR, "Error occurred: {}", 
            m -> m.getPayload())
        .transform(createErrorResponse())
        .get();
}
```

2. **Add Error Routes**:
```java
private String routeAfterValidation(Message<?> message) {
    ClientOnboardingResponse response = (ClientOnboardingResponse) message.getPayload();
    if (response.getStatus() == ClientOnboardingResponse.OnboardingStatus.FAILED) {
        return "errorChannel";  // Route to error handling
    }
    return "parallelProcessingChannel";
}
```

## Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClientOnboardingIntegrationTest

# Run with coverage
mvn test jacoco:report
```

### Writing Tests

1. **Test Individual Components**:
```java
@Test
void testValidationService() {
    // Test your validation logic
    ClientOnboardingRequest request = createValidRequest();
    ClientOnboardingResponse response = validationService.validateRequest(request);
    assertEquals(OnboardingStatus.COMPLETED, response.getStatus());
}
```

2. **Test Integration Flows**:
```java
@Test
void testCompleteFlow() {
    // Test the entire flow
    Message<ClientOnboardingRequest> message = MessageBuilder
        .withPayload(createValidRequest())
        .build();
    
    assertTrue(clientOnboardingInputChannel.send(message));
}
```

3. **Test Error Scenarios**:
```java
@Test
void testErrorHandling() {
    // Test error handling
    Message<ClientOnboardingRequest> message = MessageBuilder
        .withPayload(createInvalidRequest())
        .build();
    
    assertTrue(clientOnboardingInputChannel.send(message));
}
```

## Troubleshooting

### Common Issues

#### 1. Messages Not Flowing
**Symptoms**: Messages stuck in a channel
**Causes**: 
- Missing channel configuration
- Incorrect routing logic
- Blocking operations

**Solutions**:
- Check channel configuration
- Verify routing logic
- Use async channels for blocking operations

#### 2. Aggregation Not Working
**Symptoms**: Aggregator waiting forever for messages
**Causes**:
- Incorrect correlation strategy
- Wrong release strategy
- Missing messages

**Solutions**:
- Check correlation strategy
- Verify release strategy
- Ensure all messages have correlation ID

#### 3. Services Not Responding
**Symptoms**: External service calls timing out
**Causes**:
- Service down
- Network issues
- Incorrect service URL

**Solutions**:
- Check service health
- Verify network connectivity
- Use circuit breakers

#### 4. Error Handling Not Working
**Symptoms**: Errors not being caught and handled
**Causes**:
- Missing error channel
- Incorrect error handling configuration

**Solutions**:
- Configure error channels
- Add error handling flows

### Debugging Steps

1. **Enable Debug Logging**
2. **Check Integration Graph**
3. **Add Logging to Flows**
4. **Use Correlation IDs for Tracking**
5. **Test Individual Components**
6. **Check Message Headers and Payload**
7. **Verify Channel Configurations**
8. **Monitor Service Health**

### Getting Help

1. **Check the Logs**: Look for error messages and stack traces
2. **Use the Debug Controller**: Test individual components
3. **Check the Integration Graph**: Visualize your flows
4. **Read the Documentation**: Check the Spring Integration docs
5. **Ask Questions**: Create an issue in the repository

## Next Steps

1. **Explore the Code**: Start with the main integration flow
2. **Run the Tests**: Understand how components work
3. **Try the Debug Endpoints**: Experiment with the system
4. **Read the Spring Integration Guide**: Learn more about patterns
5. **Build Something**: Add a new service or modify existing logic

## Resources

- [Spring Integration Documentation](https://docs.spring.io/spring-integration/docs/current/reference/html/)
- [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/)
- [Spring Integration Samples](https://github.com/spring-projects/spring-integration-samples)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)

Remember: Spring Integration is like building with LEGO blocks - start with simple pieces and gradually build more complex structures! 🧱

Happy coding! 🎉
