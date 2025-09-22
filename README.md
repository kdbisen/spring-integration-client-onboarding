# Spring Integration Client Onboarding Service

A comprehensive enterprise integration solution demonstrating Spring Integration patterns for client onboarding processes in banking and financial services.

## Overview

This application showcases Enterprise Integration Patterns (EIP) using Spring Integration to orchestrate complex client onboarding workflows involving multiple external services. It demonstrates:

- **Message Routing**: Directing messages to appropriate services based on content
- **Message Splitting**: Breaking down requests into parallel processing streams
- **Message Filtering**: Validating and filtering messages based on business rules
- **Message Aggregation**: Combining results from multiple services
- **Service Activators**: Processing messages through business logic
- **Error Handling**: Comprehensive error handling and recovery mechanisms
- **Circuit Breakers**: Resilience patterns for external service calls
- **JWT Authentication**: Secure API access with Apigee integration

## Architecture

### Integration Flow

```
Client Request → Validation → Parallel Processing → Aggregation → Notification → Response
                     ↓
                [KYC Service] [Data Processor] [Reference Data] [LES Service]
```

### Services Integration

1. **KYC Service**: Know Your Customer verification
2. **Data Processor Service**: Data enrichment and transformation
3. **Reference Data Service**: Regulatory and reference data validation
4. **LES Service**: Legal Entity System verification
5. **Notification Service**: Multi-channel notifications

## Features

### Enterprise Integration Patterns

- **Message Router**: Routes messages based on validation results
- **Message Splitter**: Splits requests for parallel processing
- **Message Filter**: Filters messages based on business rules
- **Message Aggregator**: Aggregates results from multiple services
- **Service Activator**: Processes messages through business logic
- **Error Handler**: Comprehensive error handling and recovery

### Security & Authentication

- **JWT Authentication**: OAuth2 resource server with JWT tokens
- **Apigee Integration**: API gateway integration with automatic token management
- **CORS Configuration**: Cross-origin resource sharing support
- **Role-based Access Control**: Fine-grained authorization

### Resilience Patterns

- **Circuit Breaker**: Prevents cascading failures
- **Retry Logic**: Automatic retry with exponential backoff
- **Timeout Handling**: Configurable timeouts for external services
- **Fallback Mechanisms**: Graceful degradation when services are unavailable

### Monitoring & Observability

- **Structured Logging**: JSON-formatted logs with correlation IDs
- **Metrics**: Prometheus metrics integration
- **Health Checks**: Comprehensive health monitoring
- **Distributed Tracing**: Request tracing across services

## Technology Stack

- **Spring Boot 3.2.0**: Application framework
- **Spring Integration 6.2.0**: Enterprise integration patterns
- **Spring Security**: Authentication and authorization
- **Spring Cloud**: Circuit breakers and resilience
- **H2 Database**: In-memory database for development
- **PostgreSQL**: Production database
- **Apache Kafka**: Message streaming (optional)
- **Prometheus**: Metrics collection
- **Logback**: Structured logging

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker (optional, for external services)

### Running the Application

1. **Clone and Build**
   ```bash
   git clone <repository-url>
   cd spring-integration-client-onboarding
   mvn clean install
   ```

2. **Run with Development Profile**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Access the Application**
   - Application: http://localhost:8080/client-onboarding
   - H2 Console: http://localhost:8080/client-onboarding/h2-console
   - Health Check: http://localhost:8080/client-onboarding/api/v1/onboarding/health
   - Metrics: http://localhost:8080/client-onboarding/actuator/metrics

### API Endpoints

#### Submit Client Onboarding Request
```bash
curl -X POST http://localhost:8080/client-onboarding/api/v1/onboarding/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "clientId": "CLIENT001",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "documentType": "PASSPORT",
    "documentNumber": "P123456789",
    "address": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "US"
    }
  }'
```

#### Get Onboarding Status
```bash
curl -X GET http://localhost:8080/client-onboarding/api/v1/onboarding/status/CLIENT001 \
  -H "Authorization: Bearer <jwt-token>"
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | Database connection URL | `jdbc:h2:mem:onboardingdb` |
| `DATABASE_USERNAME` | Database username | `sa` |
| `DATABASE_PASSWORD` | Database password | `password` |
| `APIGEE_BASE_URL` | Apigee base URL | `https://your-apigee-instance.apigee.net` |
| `APIGEE_CLIENT_ID` | Apigee client ID | `your-client-id` |
| `APIGEE_CLIENT_SECRET` | Apigee client secret | `your-client-secret` |
| `KYC_SERVICE_URL` | KYC service URL | `http://localhost:8081/kyc/verify` |
| `DATA_PROCESSOR_SERVICE_URL` | Data processor service URL | `http://localhost:8082/data/process` |
| `REFERENCE_DATA_SERVICE_URL` | Reference data service URL | `http://localhost:8083/reference/data` |
| `LES_SERVICE_URL` | LES service URL | `http://localhost:8084/les/verify` |
| `NOTIFICATION_SERVICE_URL` | Notification service URL | `http://localhost:8085/notification/send` |

### Profiles

- **dev**: Development configuration with H2 database and mock services
- **prod**: Production configuration with PostgreSQL and real services

## Integration Patterns Implementation

### 1. Message Router
```java
@Bean
public IntegrationFlow validationFlow() {
    return IntegrationFlows
        .from(validationChannel())
        .route(Message.class, this::routeAfterValidation)
        .get();
}
```

### 2. Message Splitter
```java
@Bean
public AbstractMessageSplitter parallelServiceSplitter() {
    return new AbstractMessageSplitter() {
        @Override
        protected Object splitMessage(Message<?> message) {
            // Split into parallel service calls
            return Arrays.asList(kycMessage, dataProcessorMessage, refDataMessage, lesMessage);
        }
    };
}
```

### 3. Message Aggregator
```java
@Bean
public IntegrationFlow parallelProcessingFlow() {
    return IntegrationFlows
        .from("parallelProcessingChannel")
        .split(parallelServiceSplitter())
        .aggregate(aggregatorSpec -> aggregatorSpec
            .correlationStrategy(m -> m.getHeaders().get("correlationId"))
            .releaseStrategy(group -> group.size() >= 4)
            .outputProcessor(aggregateResults())
        )
        .get();
}
```

### 4. Circuit Breaker Integration
```java
@Bean
public IntegrationFlow kycFlow() {
    return IntegrationFlows
        .from(kycChannel())
        .handle(kycService, "verifyClient")
        .circuitBreaker(cb -> cb
            .failureThreshold(5)
            .timeout(Duration.ofSeconds(30))
        )
        .get();
}
```

## Error Handling

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ServiceIntegrationException.class)
    public ResponseEntity<Map<String, Object>> handleServiceIntegrationException(
            ServiceIntegrationException ex, WebRequest request) {
        // Handle service integration errors
    }
}
```

### Integration Error Handling
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
```

## Monitoring & Observability

### Health Checks
- Application health: `/actuator/health`
- Service-specific health: `/actuator/health/{service}`
- Database health: `/actuator/health/db`

### Metrics
- Prometheus metrics: `/actuator/prometheus`
- Custom metrics: `/actuator/metrics`
- Service metrics: `/actuator/metrics/{metric}`

### Logging
- Structured JSON logs with correlation IDs
- Separate log files for different components
- Log rotation and retention policies

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 -H "Authorization: Bearer <token>" \
   http://localhost:8080/client-onboarding/api/v1/onboarding/submit
```

## Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t client-onboarding-service .

# Run with Docker Compose
docker-compose up -d
```

### Kubernetes Deployment
```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/
```

### Production Considerations

1. **Database**: Use PostgreSQL or Oracle for production
2. **Security**: Configure proper JWT validation and Apigee integration
3. **Monitoring**: Set up Prometheus and Grafana dashboards
4. **Logging**: Configure centralized logging (ELK stack)
5. **Scaling**: Use horizontal pod autoscaling
6. **Backup**: Implement database backup and recovery procedures

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation wiki
