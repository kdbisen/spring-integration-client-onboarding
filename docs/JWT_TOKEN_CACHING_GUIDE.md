# 🔐 JWT Token Caching with Automatic Renewal Guide

## 🎯 Overview

This guide explains how to use the JWT token caching system with automatic renewal to optimize performance and reduce calls to the token service.

## 🚀 Key Features

- **Automatic Caching**: Tokens are cached in Redis for fast access
- **Smart Renewal**: Tokens are renewed 20 seconds before expiry (configurable)
- **Thread-Safe**: Concurrent access with proper locking mechanisms
- **Service-Specific**: Each service gets its own cached token
- **Health Monitoring**: Built-in health checks and statistics
- **REST API**: Complete REST API for token management

## 🔧 Configuration

### **Environment Variables**
```bash
# JWT Token Service Configuration
JWT_TOKEN_SERVICE_URL=http://your-token-service.com/oauth/token
JWT_CLIENT_ID=your-client-id
JWT_CLIENT_SECRET=your-client-secret
JWT_TOKEN_SCOPE=api.read api.write
JWT_GRANT_TYPE=client_credentials
JWT_RENEWAL_BUFFER=20

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
REDIS_DATABASE=0
REDIS_TIMEOUT=2000ms
REDIS_POOL_MAX_ACTIVE=8
REDIS_POOL_MAX_IDLE=8
REDIS_POOL_MIN_IDLE=0
REDIS_POOL_MAX_WAIT=-1ms

# Cache Configuration
CACHE_TTL=1800000
```

### **Application Properties**
```yaml
jwt:
  token:
    service:
      url: ${JWT_TOKEN_SERVICE_URL}
    client:
      id: ${JWT_CLIENT_ID}
      secret: ${JWT_CLIENT_SECRET}
    scope: ${JWT_TOKEN_SCOPE}
    grant:
      type: ${JWT_GRANT_TYPE}
    cache:
      renewal:
        buffer: ${JWT_RENEWAL_BUFFER}

spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      database: ${REDIS_DATABASE}
      timeout: ${REDIS_TIMEOUT}
      lettuce:
        pool:
          max-active: ${REDIS_POOL_MAX_ACTIVE}
          max-idle: ${REDIS_POOL_MAX_IDLE}
          min-idle: ${REDIS_POOL_MIN_IDLE}
          max-wait: ${REDIS_POOL_MAX_WAIT}
  
  cache:
    type: redis
    redis:
      time-to-live: ${CACHE_TTL}
      cache-null-values: false
      enable-statistics: true
```

## 📋 Available Endpoints

### **Token Management**
- **GET** `/api/v1/jwt/token/{serviceName}` - Get JWT token for service
- **GET** `/api/v1/jwt/bearer/{serviceName}` - Get bearer token string
- **POST** `/api/v1/jwt/refresh/{serviceName}` - Force refresh token
- **DELETE** `/api/v1/jwt/evict/{serviceName}` - Remove token from cache

### **Monitoring & Status**
- **GET** `/api/v1/jwt/status/{serviceName}` - Get token status
- **GET** `/api/v1/jwt/status` - Get all token statuses
- **GET** `/api/v1/jwt/statistics` - Get cache statistics
- **GET** `/api/v1/jwt/health` - Check token service health

## 🧪 Usage Examples

### **1. Get Token for Service**
```bash
# Get JWT token for KYC service
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

### **2. Get Bearer Token String**
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

### **3. Check Token Status**
```bash
# Check token status for KYC service
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

### **4. Get Cache Statistics**
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

### **5. Force Refresh Token**
```bash
# Force refresh token for KYC service
curl -X POST http://localhost:8080/api/v1/jwt/refresh/kyc

# Response
{
  "serviceName": "kyc",
  "message": "Token refreshed successfully",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "issuedAt": "2024-01-15T10:30:00Z",
  "expiresAt": "2024-01-15T11:30:00Z",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🔄 Automatic Renewal Process

### **How It Works**
1. **Token Caching**: Tokens are cached in Redis with TTL
2. **Renewal Check**: Every 30 seconds, system checks for tokens needing renewal
3. **Smart Renewal**: Tokens are renewed 20 seconds before expiry
4. **Async Renewal**: Renewal happens asynchronously to avoid blocking
5. **Thread Safety**: Locks prevent concurrent renewal attempts

### **Renewal Timeline**
```
Token Lifecycle:
┌─────────────────────────────────────────────────────────────┐
│ Token Issued                                                │
│ ↓                                                           │
│ [0s] Token cached                                           │
│ ↓                                                           │
│ [3580s] Cache expires, renewal triggered                   │
│ ↓                                                           │
│ [3600s] Token expires                                       │
└─────────────────────────────────────────────────────────────┘
```

### **Scheduled Tasks**
- **Cleanup Task**: Runs every minute to remove expired tokens
- **Renewal Task**: Runs every 30 seconds to check for renewal needs

## 🏗️ Integration with Services

### **Using in Service Classes**
```java
@Service
public class KycService {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    public KycResponse verifyClient(ClientOnboardingRequest request) {
        // Get bearer token automatically
        String bearerToken = jwtTokenService.getBearerToken("kyc");
        
        // Use token in HTTP request
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        
        // Make API call
        // ...
    }
}
```

### **Automatic Token Injection**
The `JwtTokenInterceptor` automatically adds tokens to HTTP requests:

```java
// Interceptor automatically adds token based on service name
// Example: kyc-service.company.com -> serviceName = "kyc"
// Example: data-processor-service -> serviceName = "data-processor"
```

## 📊 Monitoring & Health Checks

### **Health Check Endpoint**
```bash
# Check token service health
curl -X GET http://localhost:8080/api/v1/jwt/health

# Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "healthy": true,
  "status": "UP"
}
```

### **Cache Statistics**
```bash
# Get detailed cache statistics
curl -X GET http://localhost:8080/api/v1/jwt/statistics

# Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "totalTokens": 5,
  "activeTokens": 5,
  "expiredTokens": 0,
  "renewingTokens": 1
}
```

### **All Token Statuses**
```bash
# Get status of all cached tokens
curl -X GET http://localhost:8080/api/v1/jwt/status

# Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "totalTokens": 3,
  "tokens": {
    "kyc": {
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
      "isRenewing": false
    },
    "data-processor": {
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
      "isRenewing": false
    }
  }
}
```

## 🚨 Troubleshooting

### **Common Issues**

#### **Issue 1: Token Service Unavailable**
```bash
# Check token service health
curl -X GET http://localhost:8080/api/v1/jwt/health

# Check token service configuration
curl -X GET http://localhost:8080/api/v1/jwt/status/kyc
```

#### **Issue 2: Redis Connection Issues**
```bash
# Check Redis connection
redis-cli ping

# Check Redis configuration
curl -X GET http://localhost:8080/api/v1/jwt/statistics
```

#### **Issue 3: Token Renewal Failures**
```bash
# Check renewal status
curl -X GET http://localhost:8080/api/v1/jwt/status/kyc

# Force refresh if needed
curl -X POST http://localhost:8080/api/v1/jwt/refresh/kyc
```

#### **Issue 4: Cache Misses**
```bash
# Check cache statistics
curl -X GET http://localhost:8080/api/v1/jwt/statistics

# Check specific token status
curl -X GET http://localhost:8080/api/v1/jwt/status/kyc
```

### **Debug Commands**
```bash
# Check application logs
kubectl logs <pod-name> | grep -i jwt

# Check Redis cache
redis-cli keys "*jwt*"

# Check environment variables
kubectl exec -it <pod-name> -- env | grep JWT

# Test token service directly
curl -X POST http://your-token-service.com/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic $(echo -n 'client-id:client-secret' | base64)" \
  -d "grant_type=client_credentials&scope=api.read api.write"
```

## 🔄 Performance Optimization

### **Cache Hit Ratio**
- **Target**: >95% cache hit ratio
- **Monitoring**: Check statistics endpoint regularly
- **Optimization**: Adjust renewal buffer based on usage patterns

### **Renewal Buffer Tuning**
```yaml
# Adjust renewal buffer based on your needs
jwt:
  token:
    cache:
      renewal:
        buffer: 30  # Renew 30 seconds before expiry
```

### **Redis Optimization**
```yaml
# Optimize Redis connection pool
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 16    # Increase for high load
          max-idle: 8       # Keep connections alive
          min-idle: 2       # Pre-warm connections
          max-wait: 5000ms  # Increase timeout
```

## 📚 Best Practices

### **1. Service Naming**
- Use consistent service names: `kyc`, `data-processor`, `reference-data`
- Avoid special characters in service names
- Use lowercase with hyphens for multi-word services

### **2. Token Scope**
- Use minimal required scopes
- Separate read and write scopes when possible
- Review and update scopes regularly

### **3. Monitoring**
- Set up alerts for token service health
- Monitor cache hit ratios
- Track renewal failures

### **4. Security**
- Rotate client credentials regularly
- Use strong client secrets
- Monitor token usage patterns

### **5. Performance**
- Monitor cache statistics
- Tune renewal buffer based on usage
- Optimize Redis configuration

## 🎉 Conclusion

The JWT token caching system provides:

- **Automatic token management** with smart renewal
- **High performance** with Redis caching
- **Thread-safe operations** with proper locking
- **Comprehensive monitoring** with health checks
- **Easy integration** with existing services
- **REST API** for complete token management

This solution eliminates the need to call the token service repeatedly, significantly improving performance and reducing latency in your microservices architecture.
