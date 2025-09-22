package com.adyanta.onboarding.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration
 * Configures Swagger UI and API documentation
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Client Onboarding Service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .tags(tags());
    }

    private Info apiInfo() {
        return new Info()
                .title("Spring Integration Client Onboarding API")
                .description("""
                        ## Overview
                        
                        This API provides comprehensive client onboarding capabilities with Spring Integration orchestration and Fenergo compliance integration.
                        
                        ## Key Features
                        
                        - **Client Onboarding**: Complete end-to-end client onboarding process
                        - **Fenergo Integration**: Entity creation, journey management, and task processing
                        - **Service Orchestration**: Multi-step business process coordination
                        - **Enterprise Integration Patterns**: Message routing, transformation, and aggregation
                        
                        ## Authentication
                        
                        All API endpoints require JWT authentication. Include the Bearer token in the Authorization header:
                        
                        ```
                        Authorization: Bearer <your-jwt-token>
                        ```
                        
                        ## Rate Limiting
                        
                        API requests are rate-limited to prevent abuse:
                        - **Standard endpoints**: 100 requests per minute
                        - **Heavy operations**: 10 requests per minute
                        
                        ## Error Handling
                        
                        The API uses standard HTTP status codes and returns detailed error information:
                        
                        - **400 Bad Request**: Invalid request data
                        - **401 Unauthorized**: Missing or invalid authentication
                        - **403 Forbidden**: Insufficient permissions
                        - **404 Not Found**: Resource not found
                        - **429 Too Many Requests**: Rate limit exceeded
                        - **500 Internal Server Error**: Server error
                        
                        ## Correlation IDs
                        
                        All requests support correlation IDs for tracking and debugging:
                        
                        ```
                        X-Correlation-ID: <your-correlation-id>
                        ```
                        
                        ## Support
                        
                        For API support and questions:
                        - **Email**: api-support@adyanta.com
                        - **Documentation**: [API Documentation](https://docs.adyanta.com)
                        - **Status Page**: [API Status](https://status.adyanta.com)
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Adyanta API Support")
                        .email("api-support@adyanta.com")
                        .url("https://docs.adyanta.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Development Server"),
                new Server()
                        .url("https://api-dev.adyanta.com")
                        .description("Development Environment"),
                new Server()
                        .url("https://api-staging.adyanta.com")
                        .description("Staging Environment"),
                new Server()
                        .url("https://api.adyanta.com")
                        .description("Production Environment")
        );
    }

    private List<Tag> tags() {
        return List.of(
                new Tag()
                        .name("Client Onboarding")
                        .description("Client onboarding and verification operations"),
                new Tag()
                        .name("Fenergo Integration")
                        .description("Fenergo entity, journey, and task management"),
                new Tag()
                        .name("Monitoring")
                        .description("Health checks, metrics, and monitoring endpoints"),
                new Tag()
                        .name("Authentication")
                        .description("Authentication and authorization operations")
        );
    }
}
