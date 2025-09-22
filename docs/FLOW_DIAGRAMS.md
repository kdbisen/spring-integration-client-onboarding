# Integration Flow Diagrams

This document provides visual representations of the Spring Integration flows in the Client Onboarding Service.

## Overview Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT ONBOARDING FLOW                                │
└─────────────────────────────────────────────────────────────────────────────────┘

Client Request
    ↓
┌─────────────────┐
│ Add Correlation │ ← Message Enricher
│ ID              │
└─────────────────┘
    ↓
┌─────────────────┐
│ Route to        │ ← Message Router
│ Validation      │
└─────────────────┘
    ↓
┌─────────────────┐
│ Validate        │ ← Service Activator
│ Request         │
└─────────────────┘
    ↓
┌─────────────────┐
│ Filter Valid    │ ← Message Filter
│ Requests        │
└─────────────────┘
    ↓
┌─────────────────┐
│ Route to        │ ← Message Router
│ Parallel        │
│ Processing      │
└─────────────────┘
    ↓
┌─────────────────┐
│ Split into      │ ← Message Splitter
│ 4 Messages      │
└─────────────────┘
    ↓
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ KYC         │ Data        │ Reference   │ LES         │
│ Service     │ Processor   │ Data        │ Service     │
│             │ Service     │ Service     │             │
└─────────────┴─────────────┴─────────────┴─────────────┘
    ↓
┌─────────────────┐
│ Aggregate       │ ← Message Aggregator
│ Results         │
└─────────────────┘
    ↓
┌─────────────────┐
│ Send            │ ← Service Activator
│ Notifications   │
└─────────────────┘
    ↓
┌─────────────────┐
│ Return          │
│ Response        │
└─────────────────┘
```

## Detailed Flow Breakdown

### 1. Input Processing
```
Client Request
    ↓
┌─────────────────────────────────────────────────────────┐
│ clientOnboardingInputChannel                            │
│ - Receives ClientOnboardingRequest                      │
│ - Adds correlation ID for tracking                      │
│ - Logs incoming request                                 │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ Message Enricher                                        │
│ - Adds correlationId header                            │
│ - Adds requestTimestamp header                         │
│ - Adds source header                                   │
└─────────────────────────────────────────────────────────┘
```

### 2. Validation Flow
```
Enriched Message
    ↓
┌─────────────────────────────────────────────────────────┐
│ validationChannel                                       │
│ - Routes to validation service                         │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ ValidationService                                       │
│ - Basic field validation                               │
│ - Business rule validation                             │
│ - Data integrity validation                            │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ Message Filter                                          │
│ - Only allows successful validations to continue       │
│ - Failed validations go to error handling               │
└─────────────────────────────────────────────────────────┘
```

### 3. Parallel Processing Flow
```
Validated Message
    ↓
┌─────────────────────────────────────────────────────────┐
│ parallelProcessingChannel                               │
│ - Routes to parallel processing                        │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ Message Splitter                                        │
│ - Splits into 4 separate messages                       │
│ - Each message has serviceType header                   │
│ - All messages have same correlationId                  │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ kycChannel  │ dataProc    │ refData     │ lesChannel  │
│             │ Channel     │ Channel     │             │
└─────────────┴─────────────┴─────────────┴─────────────┘
    ↓
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ KycService  │ DataProc    │ RefData     │ LesService  │
│ .verify()   │ Service     │ Service     │ .verify()   │
│             │ .process()  │ .fetch()    │             │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

### 4. Aggregation Flow
```
Service Results
    ↓
┌─────────────────────────────────────────────────────────┐
│ aggregationChannel                                      │
│ - Collects results from all services                    │
│ - Groups by correlationId                               │
│ - Waits for all 4 services to complete                 │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ Message Aggregator                                      │
│ - Combines results from all services                   │
│ - Creates final ClientOnboardingResponse               │
│ - Adds processing steps and timing information         │
└─────────────────────────────────────────────────────────┘
```

### 5. Notification Flow
```
Aggregated Response
    ↓
┌─────────────────────────────────────────────────────────┐
│ notificationChannel                                     │
│ - Routes to notification service                        │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ NotificationService                                     │
│ - Sends email notification                              │
│ - Sends SMS notification                                │
│ - Sends internal notification                           │
│ - Sends webhook notification                            │
└─────────────────────────────────────────────────────────┘
```

### 6. Error Handling Flow
```
Error Message
    ↓
┌─────────────────────────────────────────────────────────┐
│ errorChannel                                           │
│ - Routes all errors to error handling                   │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ Error Handler                                           │
│ - Logs error details                                    │
│ - Creates error response                                │
│ - Preserves correlation ID                             │
└─────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────┐
│ responseChannel                                         │
│ - Returns error response to client                      │
└─────────────────────────────────────────────────────────┘
```

## Message Flow Sequence

### Successful Flow
```
1. Client Request → clientOnboardingInputChannel
2. Add Correlation ID → validationChannel
3. Validate Request → validationChannel
4. Filter Valid Requests → parallelProcessingChannel
5. Split into 4 Messages → kycChannel, dataProcessorChannel, refDataChannel, lesChannel
6. Process Services → aggregationChannel
7. Aggregate Results → notificationChannel
8. Send Notifications → responseChannel
9. Return Response → Client
```

### Error Flow
```
1. Client Request → clientOnboardingInputChannel
2. Add Correlation ID → validationChannel
3. Validate Request → validationChannel
4. Filter Valid Requests → errorChannel (if validation fails)
5. Error Handler → responseChannel
6. Return Error Response → Client
```

## Channel Configuration

### Direct Channels (Synchronous)
```
clientOnboardingInputChannel → Direct Channel
validationChannel → Direct Channel
kycChannel → Direct Channel
dataProcessorChannel → Direct Channel
referenceDataChannel → Direct Channel
lesChannel → Direct Channel
notificationChannel → Direct Channel
errorChannel → Direct Channel
responseChannel → Direct Channel
```

### Publish-Subscribe Channels (Asynchronous)
```
parallelProcessingChannel → Publish-Subscribe Channel
aggregationChannel → Publish-Subscribe Channel
```

## Service Integration Points

### External Services
```
┌─────────────────────────────────────────────────────────┐
│ External Service Integration                            │
└─────────────────────────────────────────────────────────┘

KYC Service
├── URL: http://localhost:8081/kyc/verify
├── Method: POST
├── Authentication: Bearer Token
├── Timeout: 30s
└── Circuit Breaker: Enabled

Data Processor Service
├── URL: http://localhost:8082/data/process
├── Method: POST
├── Authentication: Bearer Token
├── Timeout: 30s
└── Circuit Breaker: Enabled

Reference Data Service
├── URL: http://localhost:8083/reference/data
├── Method: GET
├── Authentication: Bearer Token
├── Timeout: 30s
└── Circuit Breaker: Enabled

LES Service
├── URL: http://localhost:8084/les/verify
├── Method: POST
├── Authentication: Bearer Token
├── Timeout: 30s
└── Circuit Breaker: Enabled

Notification Service
├── URL: http://localhost:8085/notification/send
├── Method: POST
├── Authentication: Bearer Token
├── Timeout: 30s
└── Circuit Breaker: Enabled
```

## Monitoring and Observability

### Health Checks
```
┌─────────────────────────────────────────────────────────┐
│ Health Check Endpoints                                  │
└─────────────────────────────────────────────────────────┘

Application Health
├── URL: /actuator/health
├── Status: UP/DOWN
├── Details: Service status, database status
└── Response: JSON

Integration Graph
├── URL: /actuator/integrationgraph
├── Purpose: Visual representation of flows
├── Format: JSON
└── Usage: Debugging and understanding flows

Metrics
├── URL: /actuator/metrics
├── Purpose: Performance metrics
├── Format: JSON
└── Usage: Monitoring and alerting
```

### Logging
```
┌─────────────────────────────────────────────────────────┐
│ Logging Configuration                                   │
└─────────────────────────────────────────────────────────┘

Application Logs
├── File: logs/client-onboarding.log
├── Format: JSON with correlation ID
├── Level: INFO (dev), WARN (prod)
└── Rotation: Daily, 100MB max

Integration Logs
├── File: logs/integration-flow.log
├── Format: JSON with correlation ID
├── Level: DEBUG (dev), INFO (prod)
└── Rotation: Daily, 100MB max

Error Logs
├── File: logs/client-onboarding-error.log
├── Format: JSON with correlation ID
├── Level: ERROR
└── Rotation: Daily, 100MB max
```

## Performance Considerations

### Parallel Processing
- **KYC Service**: ~2-5 seconds
- **Data Processor Service**: ~1-3 seconds
- **Reference Data Service**: ~500ms-1s
- **LES Service**: ~3-7 seconds
- **Total Parallel Time**: ~3-7 seconds (max of all services)

### Sequential Processing
- **Validation**: ~100-500ms
- **Aggregation**: ~100-200ms
- **Notifications**: ~1-2 seconds
- **Total Sequential Time**: ~1.2-2.7 seconds

### Total Processing Time
- **Best Case**: ~4.2 seconds
- **Worst Case**: ~9.7 seconds
- **Average**: ~6-7 seconds

## Scalability Considerations

### Horizontal Scaling
- **Stateless Design**: No shared state between instances
- **Load Balancing**: Multiple instances can handle requests
- **Database**: Shared database for all instances
- **External Services**: Can handle multiple concurrent requests

### Vertical Scaling
- **Memory**: JVM heap size can be increased
- **CPU**: More cores for parallel processing
- **Threads**: Thread pool size can be configured
- **Connections**: Connection pool size can be adjusted

This comprehensive flow diagram helps developers understand how messages flow through the system and how different components interact with each other.
