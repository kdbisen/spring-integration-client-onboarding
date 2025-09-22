# 🧪 MongoDB SSL Testing Guide

## 🎯 Overview

This guide provides comprehensive instructions for testing MongoDB SSL connections with JKS files in the Spring Integration Client Onboarding service.

## 🚀 Quick Start

### **1. Deploy with MongoDB SSL Support**
```bash
# Deploy with MongoDB enabled
helm install client-onboarding ./helm/client-onboarding \
  --namespace default \
  --set mongodb.enabled=true \
  --set mongodb.ssl.enabled=true
```

### **2. Access Testing Endpoints**
```bash
# Get MongoDB configuration
curl http://localhost:8080/api/v1/mongo/config

# Test basic connection
curl http://localhost:8080/api/v1/mongo/test-basic

# Test SSL connection
curl http://localhost:8080/api/v1/mongo/test-ssl

# Run comprehensive test suite
curl http://localhost:8080/api/v1/mongo/test-comprehensive
```

## 📋 Available Test Endpoints

### **Configuration & Status**
- **GET** `/api/v1/mongo/config` - Get MongoDB configuration
- **GET** `/api/v1/mongo/check-jks-files` - Check JKS files status
- **GET** `/api/v1/mongo/test-jks-integrity` - Test JKS file integrity

### **Connection Tests**
- **GET** `/api/v1/mongo/test-connection` - Test MongoDB connection
- **GET** `/api/v1/mongo/test-basic` - Test basic connection
- **GET** `/api/v1/mongo/test-ssl` - Test SSL connection
- **GET** `/api/v1/mongo/test-auth` - Test authentication

### **Operation Tests**
- **GET** `/api/v1/mongo/test-write` - Test write operations
- **GET** `/api/v1/mongo/test-read` - Test read operations
- **GET** `/api/v1/mongo/test-comprehensive` - Run all tests

### **Information**
- **GET** `/api/v1/mongo/connection-info` - Get connection details

## 🔧 Configuration

### **Environment Variables**
```bash
# MongoDB Connection
MONGODB_URI=mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true
MONGODB_DATABASE=client_onboarding
MONGODB_USERNAME=onboarding_user
MONGODB_PASSWORD=onboarding_pass

# SSL Configuration
MONGODB_SSL_ENABLED=true
MONGODB_SSL_INVALID_HOSTNAME_ALLOWED=true
MONGODB_KEYSTORE_PATH=/app/keystore/keystore.jks
MONGODB_KEYSTORE_PASSWORD=your-keystore-password
MONGODB_TRUSTSTORE_PATH=/app/keystore/truststore.jks
MONGODB_TRUSTSTORE_PASSWORD=your-truststore-password
```

### **Application Properties**
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      database: ${MONGODB_DATABASE}
      username: ${MONGODB_USERNAME}
      password: ${MONGODB_PASSWORD}
      ssl:
        enabled: ${MONGODB_SSL_ENABLED}
        invalid-hostname-allowed: ${MONGODB_SSL_INVALID_HOSTNAME_ALLOWED}
        keystore:
          path: ${MONGODB_KEYSTORE_PATH}
          password: ${MONGODB_KEYSTORE_PASSWORD}
        truststore:
          path: ${MONGODB_TRUSTSTORE_PATH}
          password: ${MONGODB_TRUSTSTORE_PASSWORD}
```

## 🧪 Test Scenarios

### **Scenario 1: Basic Connection Test**
```bash
# Test basic MongoDB connection
curl -X GET http://localhost:8080/api/v1/mongo/test-basic

# Expected Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "test": "basic_connection",
  "status": "SUCCESS",
  "message": "Basic connection successful",
  "pingResult": {
    "ok": 1.0
  }
}
```

### **Scenario 2: SSL Connection Test**
```bash
# Test SSL connection with JKS files
curl -X GET http://localhost:8080/api/v1/mongo/test-ssl

# Expected Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "test": "ssl_connection",
  "status": "SUCCESS",
  "message": "SSL connection successful",
  "serverStatus": {
    "host": "mongodb-service",
    "version": "6.0.0",
    "process": "mongod"
  },
  "buildInfo": {
    "version": "6.0.0",
    "gitVersion": "1234567890abcdef"
  }
}
```

### **Scenario 3: JKS Files Check**
```bash
# Check JKS files status
curl -X GET http://localhost:8080/api/v1/mongo/check-jks-files

# Expected Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "sslEnabled": true,
  "keystore": {
    "path": "/app/keystore/keystore.jks",
    "type": "keystore",
    "exists": true,
    "size": 2048,
    "readable": true,
    "lastModified": 1705312200000
  },
  "truststore": {
    "path": "/app/keystore/truststore.jks",
    "type": "truststore",
    "exists": true,
    "size": 1024,
    "readable": true,
    "lastModified": 1705312200000
  },
  "allFilesPresent": true,
  "status": "SUCCESS"
}
```

### **Scenario 4: Comprehensive Test Suite**
```bash
# Run all tests
curl -X GET http://localhost:8080/api/v1/mongo/test-comprehensive

# Expected Response
{
  "timestamp": "2024-01-15T10:30:00Z",
  "test": "comprehensive_test_suite",
  "testResults": [
    {
      "timestamp": "2024-01-15T10:30:00Z",
      "test": "basic_connection",
      "status": "SUCCESS",
      "message": "Basic connection successful"
    },
    {
      "timestamp": "2024-01-15T10:30:01Z",
      "test": "ssl_connection",
      "status": "SUCCESS",
      "message": "SSL connection successful"
    },
    {
      "timestamp": "2024-01-15T10:30:02Z",
      "test": "write_operations",
      "status": "SUCCESS",
      "message": "Write operations successful"
    },
    {
      "timestamp": "2024-01-15T10:30:03Z",
      "test": "read_operations",
      "status": "SUCCESS",
      "message": "Read operations successful"
    },
    {
      "timestamp": "2024-01-15T10:30:04Z",
      "test": "authentication",
      "status": "SUCCESS",
      "message": "Authentication successful"
    }
  ],
  "overallStatus": "SUCCESS",
  "totalTests": 5,
  "passedTests": 5
}
```

## 🔍 Verification Steps

### **Step 1: Check JKS Files**
```bash
# Check if JKS files are mounted
kubectl exec -it <pod-name> -- ls -la /app/keystore/

# Verify keystore
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/keystore.jks -storepass $MONGODB_KEYSTORE_PASSWORD

# Verify truststore
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/truststore.jks -storepass $MONGODB_TRUSTSTORE_PASSWORD
```

### **Step 2: Test Configuration**
```bash
# Get MongoDB configuration
curl http://localhost:8080/api/v1/mongo/config

# Check JKS files status
curl http://localhost:8080/api/v1/mongo/check-jks-files
```

### **Step 3: Test Connections**
```bash
# Test basic connection
curl http://localhost:8080/api/v1/mongo/test-basic

# Test SSL connection
curl http://localhost:8080/api/v1/mongo/test-ssl
```

### **Step 4: Test Operations**
```bash
# Test write operations
curl http://localhost:8080/api/v1/mongo/test-write

# Test read operations
curl http://localhost:8080/api/v1/mongo/test-read
```

### **Step 5: Run Comprehensive Test**
```bash
# Run all tests
curl http://localhost:8080/api/v1/mongo/test-comprehensive
```

## 🚨 Troubleshooting

### **Common Issues**

#### **Issue 1: JKS Files Not Found**
```bash
# Check if files are mounted
kubectl exec -it <pod-name> -- ls -la /app/keystore/

# Check secret
kubectl get secret mongodb-keystore-secret -o yaml

# Check volume mounts
kubectl describe pod <pod-name>
```

#### **Issue 2: SSL Connection Failed**
```bash
# Check SSL configuration
curl http://localhost:8080/api/v1/mongo/config

# Test JKS integrity
curl http://localhost:8080/api/v1/mongo/test-jks-integrity

# Check MongoDB logs
kubectl logs <pod-name> | grep -i mongodb
```

#### **Issue 3: Authentication Failed**
```bash
# Test authentication
curl http://localhost:8080/api/v1/mongo/test-auth

# Check MongoDB credentials
kubectl get secret mongodb-connection-secret -o yaml

# Verify MongoDB server SSL configuration
kubectl exec -it <pod-name> -- openssl s_client -connect mongodb-service:27017
```

#### **Issue 4: Write/Read Operations Failed**
```bash
# Test write operations
curl http://localhost:8080/api/v1/mongo/test-write

# Test read operations
curl http://localhost:8080/api/v1/mongo/test-read

# Check MongoDB permissions
kubectl exec -it <pod-name> -- mongo --eval "db.runCommand({connectionStatus: 1})"
```

### **Debug Commands**
```bash
# Check application logs
kubectl logs <pod-name> | grep -i mongodb

# Check environment variables
kubectl exec -it <pod-name> -- env | grep MONGODB

# Check JKS file permissions
kubectl exec -it <pod-name> -- ls -la /app/keystore/

# Test SSL connection manually
kubectl exec -it <pod-name> -- openssl s_client -connect mongodb-service:27017 -cert /app/ssl/mongodb-cert.pem -key /app/ssl/mongodb-key.pem
```

## 📊 Monitoring

### **Health Check Endpoint**
```bash
# Check MongoDB health
curl http://localhost:8080/actuator/health

# Expected Response
{
  "status": "UP",
  "components": {
    "mongoHealthIndicator": {
      "status": "UP",
      "details": {
        "timestamp": "2024-01-15T10:30:00Z",
        "sslEnabled": true,
        "database": "client_onboarding"
      }
    }
  }
}
```

### **Metrics Endpoint**
```bash
# Get MongoDB metrics
curl http://localhost:8080/actuator/metrics

# Get specific MongoDB metrics
curl http://localhost:8080/actuator/metrics/mongodb.connections
curl http://localhost:8080/actuator/metrics/mongodb.operations
```

## 🔄 Continuous Testing

### **Automated Test Script**
```bash
#!/bin/bash
# test-mongodb-ssl.sh

set -e

BASE_URL="http://localhost:8080/api/v1/mongo"
echo "Testing MongoDB SSL connection..."

# Test configuration
echo "1. Testing configuration..."
curl -s "$BASE_URL/config" | jq .

# Test JKS files
echo "2. Testing JKS files..."
curl -s "$BASE_URL/check-jks-files" | jq .

# Test connections
echo "3. Testing connections..."
curl -s "$BASE_URL/test-basic" | jq .
curl -s "$BASE_URL/test-ssl" | jq .

# Test operations
echo "4. Testing operations..."
curl -s "$BASE_URL/test-write" | jq .
curl -s "$BASE_URL/test-read" | jq .

# Run comprehensive test
echo "5. Running comprehensive test..."
curl -s "$BASE_URL/test-comprehensive" | jq .

echo "MongoDB SSL testing completed!"
```

### **CI/CD Integration**
```yaml
# .github/workflows/mongodb-test.yml
name: MongoDB SSL Test

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  mongodb-test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run MongoDB SSL tests
      run: |
        # Start application
        mvn spring-boot:run &
        sleep 30
        
        # Run tests
        ./test-mongodb-ssl.sh
        
        # Stop application
        pkill -f spring-boot:run
```

## 📚 Additional Resources

- **MongoDB SSL Configuration**: https://docs.mongodb.com/manual/core/security-transport-encryption/
- **Spring Data MongoDB**: https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/
- **JKS File Management**: https://docs.oracle.com/javase/tutorial/security/toolsign/rstep2.html
- **Kubernetes Secrets**: https://kubernetes.io/docs/concepts/configuration/secret/

## 🎉 Conclusion

This testing guide provides comprehensive instructions for testing MongoDB SSL connections with JKS files. The solution includes:

- **Multiple test endpoints** for different scenarios
- **Comprehensive test suite** covering all aspects
- **Troubleshooting guides** for common issues
- **Monitoring and health checks** for ongoing verification
- **CI/CD integration** for automated testing

The testing framework ensures that MongoDB SSL connections are working correctly and provides detailed feedback for debugging any issues.
