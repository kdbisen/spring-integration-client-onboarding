# Fenergo Integration Guide

## Overview

This document describes the integration between the Spring Integration Client Onboarding Service and **Fenergo**, a leading client onboarding and compliance platform. The integration demonstrates how to use Spring Integration patterns to orchestrate complex client onboarding workflows with Fenergo's API-driven architecture.

## Fenergo Concepts

### 1. Entity
- **Definition**: Legal Entity records representing clients/customers
- **Purpose**: Holds structured data per business/regulatory requirements
- **API**: Create, update, query Legal Entity data via secure APIs

### 2. Journey
- **Definition**: Business process flows for onboarding, review, etc.
- **Purpose**: Each journey is tied to a Policy and gathers/updates data
- **Types**: Onboarding, Periodic Review, Enhanced Due Diligence, etc.

### 3. Task
- **Definition**: Granular steps or actions within a journey
- **Purpose**: Document collection, address validation, compliance checks
- **Management**: Tasks can be assigned, completed, and tracked via API

### 4. Process
- **Definition**: Groups together journeys, tasks, and related actions
- **Purpose**: Defines how entities transition through lifecycle stages
- **Outcome**: Business outcomes like onboarding, periodic review

### 5. Policy
- **Definition**: Defines what data, documents, and ownership information are required
- **Purpose**: Configurable rules that drive requirements on entity and journey
- **Scoping**: Rules dictate which policy applies to which entity/client/product

## Integration Architecture

### Flow Overview

```
Client Onboarding Request
    ↓
[Validation & Processing]
    ↓
[KYC, Data Processor, Reference Data, LES Services]
    ↓
[Aggregation & Notification]
    ↓
[Fenergo Entity Creation]
    ↓
[Fenergo Journey Creation]
    ↓
[Fenergo Task Processing]
    ↓
[Fenergo Policy Validation]
    ↓
[Fenergo Compliance Check]
    ↓
[Final Response]
```

### Spring Integration Patterns Used

1. **Message Router**: Routes messages to appropriate Fenergo services
2. **Message Splitter**: Splits tasks into parallel processing streams
3. **Message Filter**: Validates entities and journeys based on business rules
4. **Message Aggregator**: Combines results from multiple Fenergo operations
5. **Service Activator**: Processes messages through Fenergo business logic
6. **Error Handler**: Comprehensive error handling for Fenergo operations

## API Endpoints

### Entity Management

#### Create Entity
```bash
curl -X POST http://localhost:8080/client-onboarding/api/v1/fenergo/entities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "entityName": "John Doe",
    "entityType": "INDIVIDUAL",
    "personalInformation": {
      "firstName": "John",
      "lastName": "Doe",
      "dateOfBirth": "1990-01-01T00:00:00",
      "nationality": "US"
    },
    "addresses": [{
      "addressType": "RESIDENTIAL",
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "US",
      "isPrimary": true
    }]
  }'
```

#### Update Entity
```bash
curl -X PUT http://localhost:8080/client-onboarding/api/v1/fenergo/entities/{entityId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "entityName": "John Doe Updated",
    "status": "VERIFIED",
    "complianceData": {
      "riskRating": "LOW",
      "complianceStatus": "COMPLIANT",
      "amlCheck": true,
      "sanctionsCheck": true,
      "pepCheck": true
    }
  }'
```

### Journey Management

#### Create Journey
```bash
curl -X POST http://localhost:8080/client-onboarding/api/v1/fenergo/journeys \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "journeyName": "Client Onboarding Journey",
    "journeyType": "ONBOARDING",
    "entityId": "ENTITY_123",
    "policyId": "POLICY_456",
    "processId": "PROCESS_789",
    "stages": [{
      "stageName": "Data Collection",
      "stageOrder": 1,
      "tasks": ["TASK_001", "TASK_002"]
    }],
    "tasks": [{
      "taskName": "Collect Personal Information",
      "taskType": "DATA_COLLECTION",
      "priority": "HIGH",
      "description": "Collect and verify personal information"
    }]
  }'
```

#### Start Journey
```bash
curl -X POST http://localhost:8080/client-onboarding/api/v1/fenergo/journeys/{journeyId}/start \
  -H "Authorization: Bearer <jwt-token>"
```

#### Complete Journey
```bash
curl -X POST http://localhost:8080/client-onboarding/api/v1/fenergo/journeys/{journeyId}/complete \
  -H "Authorization: Bearer <jwt-token>"
```

### Task Management

#### Process Task
```bash
curl -X POST http://localhost:8080/client-onboarding/api/v1/fenergo/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "taskId": "TASK_001",
    "taskType": "DATA_COLLECTION",
    "journeyId": "JOURNEY_123",
    "entityId": "ENTITY_456",
    "status": "IN_PROGRESS",
    "assignedTo": "USER_789",
    "dueDate": "2024-12-31T23:59:59",
    "requirements": ["PASSPORT", "ADDRESS_PROOF"],
    "outputs": {
      "collectedData": "Personal information collected",
      "verificationStatus": "PENDING"
    }
  }'
```

## Integration Flows

### 1. Entity Creation Flow

```
Fenergo Entity Request
    ↓
[Add Correlation ID]
    ↓
[Route to Entity Processing]
    ↓
[Validate Entity Request]
    ↓
[Filter Valid Entities]
    ↓
[Create Entity in Fenergo]
    ↓
[Process Entity Response]
    ↓
[Return Entity Creation Result]
```

### 2. Journey Creation Flow

```
Fenergo Journey Request
    ↓
[Add Correlation ID]
    ↓
[Route to Journey Processing]
    ↓
[Validate Journey Request]
    ↓
[Filter Valid Journeys]
    ↓
[Create Journey in Fenergo]
    ↓
[Process Journey Response]
    ↓
[Return Journey Creation Result]
```

### 3. Task Processing Flow

```
Fenergo Task Request
    ↓
[Add Correlation ID]
    ↓
[Route to Task Processing]
    ↓
[Split Task into Sub-tasks]
    ↓
[Process Task Types in Parallel]
    ↓
[Aggregate Task Results]
    ↓
[Update Task Status in Fenergo]
    ↓
[Return Task Processing Result]
```

## Configuration

### Application Properties

```yaml
# Fenergo Configuration
fenergo:
  api:
    base:
      url: https://your-fenergo-instance.fenergo.com/api/v1
    timeout: 30000
    client:
      id: your-fenergo-client-id
      secret: your-fenergo-client-secret
    tenant:
      id: your-fenergo-tenant-id
  integration:
    enabled: true
    auto-create-entities: true
    auto-create-journeys: true
    default-policy: "STANDARD_ONBOARDING"
    default-journey-type: "ONBOARDING"
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `FENERGO_API_BASE_URL` | Fenergo API base URL | `https://your-fenergo-instance.fenergo.com/api/v1` |
| `FENERGO_CLIENT_ID` | Fenergo client ID | `your-fenergo-client-id` |
| `FENERGO_CLIENT_SECRET` | Fenergo client secret | `your-fenergo-client-secret` |
| `FENERGO_TENANT_ID` | Fenergo tenant ID | `your-fenergo-tenant-id` |
| `FENERGO_API_TIMEOUT` | Fenergo API timeout | `30000` |

## Authentication

### OAuth2 Client Credentials Flow

The integration uses OAuth2 client credentials flow for authentication with Fenergo:

```java
@Service
public class FenergoTokenService {
    
    public String getValidAccessToken() {
        // Get OAuth2 token from Fenergo
        String tokenUrl = fenergoBaseUrl + "/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "read write");
        
        // Call Fenergo OAuth2 endpoint
        // Return access token
    }
}
```

## Error Handling

### Global Exception Handler

```java
@RestControllerAdvice
public class FenergoExceptionHandler {
    
    @ExceptionHandler(FenergoApiException.class)
    public ResponseEntity<Map<String, Object>> handleFenergoApiException(
            FenergoApiException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FENERGO_API_ERROR");
        response.put("message", ex.getMessage());
        response.put("errorCode", ex.getErrorCode());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
```

### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      fenergo-entity-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
        minimum-number-of-calls: 5
      fenergo-journey-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
        minimum-number-of-calls: 5
```

## Monitoring and Observability

### Health Checks

- **Fenergo Integration Health**: `/api/v1/fenergo/health`
- **Fenergo Status**: `/api/v1/fenergo/status`
- **Fenergo Metrics**: `/api/v1/fenergo/metrics`

### Logging

```yaml
logging:
  level:
    com.adyanta.onboarding.fenergo: DEBUG
    org.springframework.integration: DEBUG
```

### Metrics

The integration provides metrics for:
- Entity creation success/failure rates
- Journey processing times
- Task completion rates
- API response times
- Error rates

## Testing

### Unit Tests

```java
@Test
void testFenergoEntityCreation() {
    FenergoEntity entity = createTestEntity();
    Map<String, Object> result = fenergoEntityService.createEntity(entity);
    assertEquals("SUCCESS", result.get("status"));
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureTestDatabase
class FenergoIntegrationTest {
    
    @Test
    void testCompleteFenergoFlow() {
        // Test complete Fenergo integration flow
    }
}
```

## Best Practices

### 1. Entity Management
- Always validate entity data before sending to Fenergo
- Use correlation IDs for tracking across systems
- Handle entity status transitions properly

### 2. Journey Management
- Create journeys with appropriate policies
- Monitor journey progress and handle timeouts
- Implement proper error handling for failed journeys

### 3. Task Processing
- Use appropriate task priorities
- Implement task retry mechanisms
- Monitor task completion rates

### 4. Error Handling
- Implement circuit breakers for Fenergo API calls
- Use exponential backoff for retries
- Log all errors with correlation IDs

### 5. Security
- Use OAuth2 for authentication
- Implement proper token management
- Secure sensitive data in transit and at rest

## Troubleshooting

### Common Issues

#### 1. Authentication Failures
- **Symptoms**: 401 Unauthorized errors
- **Causes**: Invalid credentials, expired tokens
- **Solutions**: Check credentials, refresh tokens

#### 2. Entity Creation Failures
- **Symptoms**: Entity creation returns errors
- **Causes**: Invalid entity data, missing required fields
- **Solutions**: Validate entity data, check required fields

#### 3. Journey Processing Issues
- **Symptoms**: Journeys stuck in progress
- **Causes**: Task failures, policy violations
- **Solutions**: Check task status, validate policies

#### 4. API Timeouts
- **Symptoms**: Requests timing out
- **Causes**: Network issues, Fenergo service overload
- **Solutions**: Increase timeout values, implement retries

### Debugging Steps

1. **Check Logs**: Look for error messages and stack traces
2. **Verify Configuration**: Ensure Fenergo settings are correct
3. **Test Authentication**: Verify OAuth2 token generation
4. **Monitor Metrics**: Check success/failure rates
5. **Use Debug Endpoints**: Test individual components

## Resources

- [Fenergo Developer Hub](https://docs.fenergonebula.com/developer-hub/api-overview/api-catalogue)
- [Fenergo API Documentation](https://docs.fenergonebula.com/)
- [Spring Integration Documentation](https://docs.spring.io/spring-integration/docs/current/reference/html/)
- [OAuth2 Client Credentials Flow](https://tools.ietf.org/html/rfc6749#section-4.4)

This integration demonstrates how Spring Integration can be used to orchestrate complex client onboarding workflows with Fenergo, providing a robust, scalable, and maintainable solution for financial services compliance and onboarding processes.
