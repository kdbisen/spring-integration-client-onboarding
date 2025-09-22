# 📚 API Documentation Guide

## 🎯 Overview

This guide provides comprehensive information about the API documentation features added to the Spring Integration Client Onboarding service.

## 🚀 Features Added

### 1. **Swagger/OpenAPI 3.0 Integration**
- **Interactive API Explorer**: Test APIs directly from the browser
- **Comprehensive Documentation**: Detailed descriptions, examples, and schemas
- **Multiple API Groups**: Organized by functionality (Client Onboarding, Fenergo Integration, Monitoring)
- **Security Documentation**: JWT authentication requirements

### 2. **Enhanced Controllers**
- **Detailed Operation Descriptions**: Step-by-step process explanations
- **Response Examples**: Real-world response samples for all scenarios
- **Error Documentation**: Comprehensive error handling documentation
- **Parameter Documentation**: Clear parameter descriptions with examples

### 3. **Model Documentation**
- **Schema Annotations**: Detailed field descriptions and examples
- **Validation Documentation**: Clear validation rules and error messages
- **Type Information**: Proper data type documentation

## 📋 Available Endpoints

### **Client Onboarding API** (`/api/v1/onboarding`)

#### **POST** `/submit`
- **Purpose**: Submit client onboarding request
- **Authentication**: JWT Bearer Token required
- **Process Flow**: 7-step verification workflow
- **Response**: 202 Accepted with correlation ID

#### **GET** `/status/{clientId}`
- **Purpose**: Get onboarding status
- **Authentication**: JWT Bearer Token required
- **Response**: Current status, progress, and step details

### **Fenergo Integration API** (`/api/v1/fenergo`)

#### **POST** `/entities`
- **Purpose**: Create Fenergo entity
- **Authentication**: JWT Bearer Token required
- **Entity Types**: Individual, Corporate, Trust, Partnership
- **Response**: 201 Created with Fenergo entity ID

#### **POST** `/journeys`
- **Purpose**: Start Fenergo journey
- **Authentication**: JWT Bearer Token required
- **Journey Types**: Onboarding, Review, Update
- **Response**: 201 Created with journey details

#### **POST** `/tasks`
- **Purpose**: Process Fenergo task
- **Authentication**: JWT Bearer Token required
- **Task Types**: Document collection, verification, approval
- **Response**: 200 OK with task status

### **Monitoring API** (`/actuator`)

#### **GET** `/health`
- **Purpose**: Application health check
- **Authentication**: None required
- **Response**: Health status and details

#### **GET** `/metrics`
- **Purpose**: Application metrics
- **Authentication**: None required
- **Response**: Prometheus metrics

## 🔧 Configuration

### **OpenAPI Configuration**
```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
    filter: true
    displayRequestDuration: true
  group-configs:
    - group: 'client-onboarding'
      display-name: 'Client Onboarding API'
      paths-to-match: '/api/v1/onboarding/**'
    - group: 'fenergo-integration'
      display-name: 'Fenergo Integration API'
      paths-to-match: '/api/v1/fenergo/**'
    - group: 'monitoring'
      display-name: 'Monitoring & Health API'
      paths-to-match: '/actuator/**'
```

### **Security Configuration**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-server.com
```

## 🌐 Accessing Documentation

### **Swagger UI**
- **URL**: `http://localhost:8080/swagger-ui.html`
- **Features**: Interactive API testing, schema exploration, authentication testing

### **OpenAPI JSON**
- **URL**: `http://localhost:8080/api-docs`
- **Format**: OpenAPI 3.0 specification
- **Use Cases**: Code generation, API testing tools, documentation generation

### **Grouped Documentation**
- **Client Onboarding**: `http://localhost:8080/swagger-ui.html#/client-onboarding`
- **Fenergo Integration**: `http://localhost:8080/swagger-ui.html#/fenergo-integration`
- **Monitoring**: `http://localhost:8080/swagger-ui.html#/monitoring`

## 🔐 Authentication

### **JWT Token Requirements**
```bash
# Header format
Authorization: Bearer <your-jwt-token>

# Example
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### **Token Claims**
- **sub**: Subject (user ID)
- **aud**: Audience (API identifier)
- **exp**: Expiration time
- **iat**: Issued at time
- **scope**: API permissions

## 📊 Response Examples

### **Success Response**
```json
{
  "status": "ACCEPTED",
  "message": "Client onboarding request accepted for processing",
  "correlationId": "123e4567-e89b-12d3-a456-426614174000",
  "clientId": "CLIENT_123e4567",
  "timestamp": "2024-01-15T10:30:00Z",
  "estimatedCompletionTime": "2024-01-15T10:35:00Z"
}
```

### **Error Response**
```json
{
  "status": "FAILED",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ],
  "correlationId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🛠️ Development Tools

### **Postman Collection**
- **Import**: Use OpenAPI JSON to generate Postman collection
- **Environment**: Set up variables for different environments
- **Testing**: Automated API testing with Postman

### **Code Generation**
- **OpenAPI Generator**: Generate client SDKs
- **Spring Boot Generator**: Generate server stubs
- **TypeScript Generator**: Generate frontend types

### **API Testing**
- **Swagger UI**: Interactive testing
- **Postman**: Automated testing
- **Newman**: CLI testing
- **Jest**: Unit testing

## 📈 Monitoring & Analytics

### **API Metrics**
- **Request Count**: Total API calls
- **Response Time**: Average response time
- **Error Rate**: Error percentage
- **Throughput**: Requests per second

### **Usage Analytics**
- **Endpoint Usage**: Most used endpoints
- **Error Patterns**: Common error types
- **Performance Trends**: Response time trends
- **User Behavior**: API usage patterns

## 🔧 Troubleshooting

### **Common Issues**

#### **Swagger UI Not Loading**
```bash
# Check if application is running
curl http://localhost:8080/actuator/health

# Check OpenAPI endpoint
curl http://localhost:8080/api-docs
```

#### **Authentication Issues**
```bash
# Verify JWT token
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/onboarding/status/test
```

#### **CORS Issues**
```yaml
# Add CORS configuration
spring:
  web:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
```

### **Debug Mode**
```yaml
# Enable debug logging
logging:
  level:
    org.springdoc: DEBUG
    com.adyanta.onboarding: DEBUG
```

## 🚀 Next Steps

### **Enhanced Documentation**
1. **API Versioning**: Add version support
2. **Rate Limiting**: Document rate limits
3. **Webhooks**: Document webhook endpoints
4. **SDK Generation**: Generate client SDKs

### **Testing Enhancements**
1. **Contract Testing**: API contract validation
2. **Load Testing**: Performance testing
3. **Security Testing**: Security validation
4. **Integration Testing**: End-to-end testing

### **Monitoring Enhancements**
1. **Real-time Metrics**: Live API metrics
2. **Alerting**: Error and performance alerts
3. **Dashboards**: Custom monitoring dashboards
4. **Logging**: Structured logging integration

## 📚 Additional Resources

- **OpenAPI Specification**: https://swagger.io/specification/
- **SpringDoc Documentation**: https://springdoc.org/
- **Swagger UI**: https://swagger.io/tools/swagger-ui/
- **JWT.io**: https://jwt.io/ (JWT token debugging)

## 🎉 Conclusion

The API documentation provides a comprehensive, interactive, and developer-friendly way to explore and test the Spring Integration Client Onboarding service. With detailed examples, clear error handling, and organized endpoint groups, developers can quickly understand and integrate with the API.

The documentation is automatically generated from the code annotations, ensuring it stays up-to-date with the actual implementation. The interactive Swagger UI allows for immediate testing and validation of API endpoints.
