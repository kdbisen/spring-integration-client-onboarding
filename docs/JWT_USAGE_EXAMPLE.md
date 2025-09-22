# 🔐 JWT Token Caching Usage Example

## 🎯 Quick Start Example

Here's how to use the JWT token caching system in your services:

### **1. Service Integration Example**

```java
@Service
public class KycService {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public KycResponse verifyClient(ClientOnboardingRequest request) {
        try {
            // Get bearer token automatically (cached and auto-renewed)
            String bearerToken = jwtTokenService.getBearerToken("kyc");
            
            // Set up HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", bearerToken);
            headers.set("Content-Type", "application/json");
            
            // Create request entity
            HttpEntity<ClientOnboardingRequest> requestEntity = 
                new HttpEntity<>(request, headers);
            
            // Make API call to KYC service
            ResponseEntity<KycResponse> response = restTemplate.exchange(
                "http://kyc-service:8081/kyc/verify",
                HttpMethod.POST,
                requestEntity,
                KycResponse.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("KYC verification failed", e);
            throw new RuntimeException("KYC verification failed", e);
        }
    }
}
```

### **2. Multiple Services Example**

```java
@Service
public class DataProcessorService {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public DataProcessorResponse processData(ClientOnboardingRequest request) {
        try {
            // Get bearer token for data processor service
            String bearerToken = jwtTokenService.getBearerToken("data-processor");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", bearerToken);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<ClientOnboardingRequest> requestEntity = 
                new HttpEntity<>(request, headers);
            
            ResponseEntity<DataProcessorResponse> response = restTemplate.exchange(
                "http://data-processor-service:8082/process",
                HttpMethod.POST,
                requestEntity,
                DataProcessorResponse.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Data processing failed", e);
            throw new RuntimeException("Data processing failed", e);
        }
    }
}
```

### **3. Automatic Token Injection Example**

The `JwtTokenInterceptor` automatically adds tokens to HTTP requests:

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add JWT token interceptor for automatic token injection
        restTemplate.getInterceptors().add(new JwtTokenInterceptor());
        
        return restTemplate;
    }
}
```

With this configuration, tokens are automatically added based on the service name extracted from the URL:

```java
@Service
public class ReferenceDataService {
    
    @Autowired
    private RestTemplate restTemplate; // Automatically injects tokens
    
    public ReferenceDataResponse getReferenceData(String clientId) {
        // Token automatically added based on URL: reference-data-service -> "reference-data"
        ResponseEntity<ReferenceDataResponse> response = restTemplate.getForEntity(
            "http://reference-data-service:8083/reference-data/{clientId}",
            ReferenceDataResponse.class,
            clientId
        );
        
        return response.getBody();
    }
}
```

## 🚀 Testing Examples

### **1. Test Token Retrieval**

```bash
# Get token for KYC service
curl -X GET http://localhost:8080/api/v1/jwt/token/kyc

# Response
{
  "serviceName": "kyc",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "scope": "api.read api.write",
  "issuedAt": "2024-01-15T10:30:00Z",
  "expiresAt": "2024-01-15T11:30:00Z",
  "cacheExpiresAt": "2024-01-15T11:29:40Z",
  "secondsUntilExpiry": 3600,
  "secondsUntilCacheExpiry": 3580,
  "needsRenewal": false,
  "isRenewing": false,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### **2. Test Bearer Token**

```bash
# Get bearer token for HTTP headers
curl -X GET http://localhost:8080/api/v1/jwt/bearer/kyc

# Response
{
  "serviceName": "kyc",
  "bearerToken": "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### **3. Test Token Status**

```bash
# Check token status
curl -X GET http://localhost:8080/api/v1/jwt/status/kyc

# Response
{
  "serviceName": "kyc",
  "exists": true,
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "issuedAt": "2024-01-15T10:30:00Z",
  "expiresAt": "2024-01-15T11:30:00Z",
  "cacheExpiresAt": "2024-01-15T11:29:40Z",
  "secondsUntilExpiry": 3600,
  "secondsUntilCacheExpiry": 3580,
  "isExpired": false,
  "isCacheExpired": false,
  "needsRenewal": false,
  "isRenewing": false,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### **4. Test Cache Statistics**

```bash
# Get cache statistics
curl -X GET http://localhost:8080/api/v1/jwt/statistics

# Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "totalTokens": 3,
  "activeTokens": 3,
  "expiredTokens": 0,
  "renewingTokens": 0
}
```

## 🔧 Configuration Examples

### **1. Environment Variables**

```bash
# JWT Token Service Configuration
export JWT_TOKEN_SERVICE_URL="http://your-token-service.com/oauth/token"
export JWT_CLIENT_ID="your-client-id"
export JWT_CLIENT_SECRET="your-client-secret"
export JWT_TOKEN_SCOPE="api.read api.write"
export JWT_GRANT_TYPE="client_credentials"
export JWT_RENEWAL_BUFFER="20"

# Redis Configuration
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
export REDIS_PASSWORD="your-redis-password"
export REDIS_DATABASE="0"
export REDIS_TIMEOUT="2000ms"
export REDIS_POOL_MAX_ACTIVE="8"
export REDIS_POOL_MAX_IDLE="8"
export REDIS_POOL_MIN_IDLE="0"
export REDIS_POOL_MAX_WAIT="-1ms"

# Cache Configuration
export CACHE_TTL="1800000"
```

### **2. Kubernetes ConfigMap**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: jwt-token-config
data:
  JWT_TOKEN_SERVICE_URL: "http://token-service:8080/oauth/token"
  JWT_CLIENT_ID: "your-client-id"
  JWT_TOKEN_SCOPE: "api.read api.write"
  JWT_GRANT_TYPE: "client_credentials"
  JWT_RENEWAL_BUFFER: "20"
  REDIS_HOST: "redis-service"
  REDIS_PORT: "6379"
  REDIS_DATABASE: "0"
  REDIS_TIMEOUT: "2000ms"
  CACHE_TTL: "1800000"
```

### **3. Kubernetes Secret**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: jwt-token-secret
type: Opaque
data:
  JWT_CLIENT_SECRET: <base64-encoded-secret>
  REDIS_PASSWORD: <base64-encoded-redis-password>
```

## 🧪 Testing Script Examples

### **1. Quick Test**

```bash
# Run quick JWT token tests
./scripts/test-jwt-tokens.sh -s kyc

# Output
[INFO] Testing JWT token caching
[INFO] Base URL: http://localhost:8080/api/v1/jwt
[INFO] Service Name: kyc
[INFO] Testing token service health...
[SUCCESS] Token service health: UP
[INFO] Testing JWT token caching for service: kyc
[SUCCESS] Initial token retrieved successfully
[SUCCESS] Token caching working correctly
[SUCCESS] Bearer token retrieved successfully
Bearer token: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
[SUCCESS] Quick JWT token caching tests completed!
```

### **2. Comprehensive Test**

```bash
# Run comprehensive JWT token tests
./scripts/test-jwt-tokens.sh -s kyc -u http://localhost:8080/api/v1/jwt

# Output
[INFO] Running comprehensive JWT token caching tests...
==================================================

[INFO] Testing token service health...
[SUCCESS] Token service health: UP

[INFO] Testing JWT token caching for service: kyc
[SUCCESS] Initial token retrieved successfully
[SUCCESS] Token caching working correctly
[SUCCESS] Bearer token retrieved successfully

[INFO] Testing token status for service: kyc
[SUCCESS] Token exists in cache
Token expires in: 3600 seconds
Cache expires in: 3580 seconds
Needs renewal: false
Is renewing: false

[INFO] Testing cache statistics...
[SUCCESS] Cache statistics retrieved
Total tokens: 1
Active tokens: 1
Expired tokens: 0
Renewing tokens: 0

[SUCCESS] Comprehensive JWT token caching tests completed!
```

## 🔄 Automatic Renewal Example

### **Timeline Example**

```
Token Lifecycle:
┌─────────────────────────────────────────────────────────────┐
│ [0s] Token issued and cached                                │
│ ↓                                                           │
│ [3580s] Cache expires, renewal triggered (20s before expiry)│
│ ↓                                                           │
│ [3600s] Original token expires                              │
│ ↓                                                           │
│ [3600s] New token ready for use                            │
└─────────────────────────────────────────────────────────────┘
```

### **Renewal Process**

1. **Token Cached**: Token cached with 3600s TTL
2. **Renewal Check**: System checks every 30s for renewal needs
3. **Smart Renewal**: At 3580s, renewal triggered (20s buffer)
4. **Async Renewal**: New token fetched asynchronously
5. **Seamless Transition**: New token ready before old one expires

## 🚨 Error Handling Examples

### **1. Token Service Unavailable**

```java
@Service
public class KycService {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    public KycResponse verifyClient(ClientOnboardingRequest request) {
        try {
            String bearerToken = jwtTokenService.getBearerToken("kyc");
            // ... make API call
        } catch (Exception e) {
            logger.error("Failed to get JWT token for KYC service", e);
            // Fallback or retry logic
            throw new RuntimeException("KYC service unavailable", e);
        }
    }
}
```

### **2. Token Refresh Failure**

```bash
# Check token service health
curl -X GET http://localhost:8080/api/v1/jwt/health

# Response when service is down
{
  "timestamp": "2024-01-15T10:30:00Z",
  "healthy": false,
  "status": "DOWN"
}
```

### **3. Cache Miss Handling**

```java
@Service
public class KycService {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    public KycResponse verifyClient(ClientOnboardingRequest request) {
        try {
            // This will automatically fetch new token if cache miss
            String bearerToken = jwtTokenService.getBearerToken("kyc");
            // ... make API call
        } catch (Exception e) {
            logger.error("JWT token service error", e);
            // Implement fallback strategy
            return createFallbackResponse();
        }
    }
}
```

## 📊 Monitoring Examples

### **1. Health Check Integration**

```java
@Component
public class JwtTokenHealthIndicator implements HealthIndicator {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Override
    public Health health() {
        boolean isHealthy = jwtTokenService.isTokenServiceHealthy();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("tokenService", "UP")
                    .withDetail("cacheStatistics", jwtTokenService.getCacheStatistics())
                    .build();
        } else {
            return Health.down()
                    .withDetail("tokenService", "DOWN")
                    .build();
        }
    }
}
```

### **2. Metrics Integration**

```java
@Component
public class JwtTokenMetrics {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @EventListener
    public void handleTokenEvent(TokenEvent event) {
        // Update metrics based on token events
        meterRegistry.counter("jwt.token.requests", "service", event.getServiceName()).increment();
    }
}
```

## 🎉 Conclusion

The JWT token caching system provides:

- **Automatic token management** with smart renewal
- **High performance** with Redis caching
- **Thread-safe operations** with proper locking
- **Easy integration** with existing services
- **Comprehensive monitoring** with health checks
- **REST API** for complete token management

This solution eliminates the need to call the token service repeatedly, significantly improving performance and reducing latency in your microservices architecture.
