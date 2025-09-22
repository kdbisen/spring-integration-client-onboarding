# Spring Integration Quick Reference Guide

## Quick Decision Tree

```
Do you need complex orchestration?
├── YES → Use Spring Integration
│   ├── Multiple services to coordinate?
│   ├── Complex error handling needed?
│   ├── Async processing required?
│   └── Built-in monitoring needed?
└── NO → Consider Payload-Based Processor
    ├── Simple linear processing?
    ├── Performance critical?
    ├── Non-Spring ecosystem?
    └── Minimal error handling?
```

## Spring Integration vs Payload-Based Processor

| **Use Case** | **Spring Integration** | **Payload-Based Processor** |
|--------------|------------------------|----------------------------|
| **Complex Orchestration** | ✅ **Recommended** | ❌ Not suitable |
| **Simple Processing** | ❌ Overkill | ✅ **Recommended** |
| **Performance Critical** | ❌ Overhead | ✅ **Recommended** |
| **Enterprise Features** | ✅ **Recommended** | ❌ Manual implementation |
| **Spring Ecosystem** | ✅ **Recommended** | ❌ Not integrated |
| **Non-Spring Framework** | ❌ Not suitable | ✅ **Recommended** |
| **Learning Curve** | ❌ Steep | ✅ **Easier** |
| **Debugging** | ❌ Complex | ✅ **Simpler** |

## Design Patterns Summary

### Spring Integration Patterns
- **Orchestrator Pattern**: Central coordinator
- **Service Activator Pattern**: Message processing
- **Message Router Pattern**: Dynamic routing
- **Aggregator Pattern**: Result combination
- **Circuit Breaker Pattern**: Fault tolerance

### Payload-Based Processor Patterns
- **Strategy Pattern**: Different processors
- **Factory Pattern**: Processor creation
- **Template Method Pattern**: Common processing flow
- **Chain of Responsibility**: Sequential processing

## Performance Comparison

| **Metric** | **Spring Integration** | **Payload-Based Processor** |
|------------|------------------------|----------------------------|
| **Latency** | Medium | Low |
| **Throughput** | Medium | High |
| **Memory Usage** | High | Low |
| **CPU Usage** | Medium | Low |
| **Scalability** | High | Medium |

## Error Handling Comparison

| **Feature** | **Spring Integration** | **Payload-Based Processor** |
|-------------|------------------------|----------------------------|
| **Retry Logic** | ✅ Built-in | ❌ Manual |
| **Circuit Breaker** | ✅ Built-in | ❌ Manual |
| **Dead Letter Queue** | ✅ Built-in | ❌ Manual |
| **Error Routing** | ✅ Built-in | ❌ Manual |
| **Monitoring** | ✅ Built-in | ❌ Manual |

## Monitoring Comparison

| **Feature** | **Spring Integration** | **Payload-Based Processor** |
|-------------|------------------------|----------------------------|
| **JMX Integration** | ✅ Built-in | ❌ Manual |
| **Actuator Endpoints** | ✅ Built-in | ❌ Manual |
| **Metrics Collection** | ✅ Built-in | ❌ Manual |
| **Health Checks** | ✅ Built-in | ❌ Manual |
| **Trace Logging** | ✅ Built-in | ❌ Manual |

## Code Examples

### Spring Integration (Current)
```java
@ServiceActivator(inputChannel = "clientOnboardingInputChannel")
public Message<ClientOnboardingResponse> processClientOnboarding(Message<ClientOnboardingRequest> message) {
    // Orchestrated multi-step process with built-in error handling
    ClientOnboardingRequest request = message.getPayload();
    
    ClientOnboardingResponse validationResponse = validationService.validateRequest(request);
    Map<String, Object> kycResult = kycService.verifyClient(request);
    Map<String, Object> dataResult = dataProcessorService.processClientData(request);
    Map<String, Object> referenceResult = referenceDataService.fetchReferenceData(request);
    Map<String, Object> lesResult = lesService.verifyWithLes(request);
    Map<String, Object> notificationResult = notificationService.sendNotification(validationResponse);
    
    return createSuccessResponse(validationResponse);
}
```

### Payload-Based Processor (Alternative)
```java
@Component("CLIENT_ONBOARDING")
public class ClientOnboardingProcessor implements PayloadProcessor {
    
    @Override
    public ProcessingResult process(Payload payload) {
        ClientOnboardingRequest request = (ClientOnboardingRequest) payload.getData();
        
        // Sequential processing with manual error handling
        ValidationResult validation = validate(request);
        KycResult kyc = performKyc(request);
        DataResult data = processData(request);
        ReferenceResult reference = validateReference(request);
        LesResult les = verifyLes(request);
        NotificationResult notification = sendNotification(request);
        
        return ProcessingResult.success(validation, kyc, data, reference, les, notification);
    }
}
```

## When to Choose What

### Choose Spring Integration When:
- ✅ Complex multi-service orchestration
- ✅ Need enterprise integration patterns
- ✅ Comprehensive error handling required
- ✅ Built-in monitoring and observability needed
- ✅ Spring ecosystem integration
- ✅ Async processing requirements
- ✅ Circuit breaker patterns needed

### Choose Payload-Based Processor When:
- ✅ Simple linear processing
- ✅ Performance is critical
- ✅ Minimal error handling sufficient
- ✅ Non-Spring ecosystem
- ✅ Simple debugging required
- ✅ Low learning curve needed
- ✅ Direct method calls preferred

## Migration Guide

### From Spring Integration to Payload-Based Processor

1. **Extract Service Logic**
   ```java
   // From Service Activator
   @ServiceActivator(inputChannel = "kycChannel")
   public Message<Map<String, Object>> performKyc(Message<ClientOnboardingRequest> message) {
       // Extract to direct service call
   }
   
   // To Direct Service Call
   public Map<String, Object> performKyc(ClientOnboardingRequest request) {
       return kycService.verifyClient(request);
   }
   ```

2. **Create Processor Interface**
   ```java
   public interface PayloadProcessor {
       ProcessingResult process(Payload payload);
       String getSupportedPayloadType();
   }
   ```

3. **Implement Processors**
   ```java
   @Component("CLIENT_ONBOARDING")
   public class ClientOnboardingProcessor implements PayloadProcessor {
       // Implementation
   }
   ```

4. **Create Processor Factory**
   ```java
   @Component
   public class PayloadProcessorFactory {
       @Autowired
       private Map<String, PayloadProcessor> processors;
       
       public PayloadProcessor getProcessor(String payloadType) {
           return processors.get(payloadType);
       }
   }
   ```

### From Payload-Based Processor to Spring Integration

1. **Create Message Channels**
   ```java
   @Bean
   public MessageChannel clientOnboardingInputChannel() {
       return new DirectChannel();
   }
   ```

2. **Create Service Activators**
   ```java
   @ServiceActivator(inputChannel = "clientOnboardingInputChannel")
   public Message<ClientOnboardingResponse> processClientOnboarding(Message<ClientOnboardingRequest> message) {
       // Implementation
   }
   ```

3. **Configure Integration Flows**
   ```java
   @Configuration
   @EnableIntegration
   public class IntegrationConfig {
       // Configuration
   }
   ```

4. **Add Error Handling**
   ```java
   @Bean
   public IntegrationFlow errorHandlingFlow() {
       return IntegrationFlows
               .from("errorChannel")
               .log(LoggingHandler.Level.ERROR, "Error occurred")
               .transform(createErrorResponse())
               .channel("responseChannel")
               .get();
   }
   ```

## Best Practices

### Spring Integration Best Practices
1. **Use appropriate message channels** (DirectChannel vs QueueChannel)
2. **Implement proper error handling** with error channels
3. **Use correlation IDs** for message tracking
4. **Enable monitoring** with IntegrationManagementConfigurer
5. **Test integration flows** with Spring Integration Test framework
6. **Use message transformers** for data conversion
7. **Implement circuit breakers** for external service calls

### Payload-Based Processor Best Practices
1. **Use strategy pattern** for different processors
2. **Implement factory pattern** for processor creation
3. **Add comprehensive error handling** with try-catch blocks
4. **Use correlation IDs** for request tracking
5. **Implement custom monitoring** with metrics
6. **Add retry logic** with exponential backoff
7. **Use template method pattern** for common processing flow

## Troubleshooting

### Spring Integration Issues
- **Message not processed**: Check channel configuration
- **Service activator not working**: Verify method signatures
- **Performance issues**: Monitor message queue sizes
- **Memory leaks**: Check for proper message cleanup

### Payload-Based Processor Issues
- **Processor not found**: Check processor registration
- **Performance issues**: Monitor thread usage
- **Error handling**: Implement comprehensive try-catch blocks
- **Memory leaks**: Check for proper resource cleanup

## Conclusion

Both approaches have their place in enterprise architecture. Choose based on your specific requirements:

- **Spring Integration**: Complex orchestration, enterprise features, Spring ecosystem
- **Payload-Based Processor**: Simple processing, performance critical, non-Spring ecosystem

The key is to understand your requirements and choose the approach that best fits your use case.
