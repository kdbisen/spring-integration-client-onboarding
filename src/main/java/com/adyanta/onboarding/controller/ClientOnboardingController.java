package com.adyanta.onboarding.controller;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import com.adyanta.onboarding.model.ClientOnboardingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Client Onboarding
 * Provides HTTP endpoints for the client onboarding process
 */
@RestController
@RequestMapping("/api/v1/onboarding")
@Validated
@Tag(name = "Client Onboarding", description = "Client onboarding and verification operations")
@SecurityRequirement(name = "bearerAuth")
public class ClientOnboardingController {

    private static final Logger logger = LoggerFactory.getLogger(ClientOnboardingController.class);

    @Autowired
    @Qualifier("clientOnboardingInputChannel")
    private MessageChannel clientOnboardingInputChannel;

    /**
     * Submit client onboarding request
     */
    @Operation(
            summary = "Submit Client Onboarding Request",
            description = """
                    Submits a new client onboarding request for processing through the complete verification workflow.
                    
                    ## Process Flow
                    1. **Validation**: Request data validation and business rule checks
                    2. **KYC Verification**: Know Your Customer verification process
                    3. **Data Processing**: Data enrichment and quality checks
                    4. **Reference Data**: Reference data validation and enrichment
                    5. **LES Verification**: Legal Entity System verification
                    6. **Notification**: Send completion notifications
                    7. **Fenergo Integration**: Create entity and journey in Fenergo
                    
                    ## Processing Time
                    - **Standard Processing**: 2-5 minutes
                    - **Complex Cases**: Up to 15 minutes
                    - **Real-time Status**: Available via status endpoint
                    
                    ## Error Handling
                    - **Validation Errors**: 400 Bad Request with detailed field errors
                    - **Service Errors**: 500 Internal Server Error with error details
                    - **Timeout Errors**: 504 Gateway Timeout for long-running processes
                    """,
            operationId = "submitOnboardingRequest"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Request accepted for processing",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "status": "ACCEPTED",
                                              "message": "Client onboarding request accepted for processing",
                                              "correlationId": "123e4567-e89b-12d3-a456-426614174000",
                                              "clientId": "CLIENT_123e4567",
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "estimatedCompletionTime": "2024-01-15T10:35:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "status": "FAILED",
                                              "message": "Validation failed",
                                              "errors": [
                                                {
                                                  "field": "email",
                                                  "message": "Invalid email format"
                                                },
                                                {
                                                  "field": "phoneNumber",
                                                  "message": "Phone number is required"
                                                }
                                              ],
                                              "correlationId": "123e4567-e89b-12d3-a456-426614174000",
                                              "timestamp": "2024-01-15T10:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Authentication Error",
                                    value = """
                                            {
                                              "status": "UNAUTHORIZED",
                                              "message": "Invalid or missing authentication token",
                                              "timestamp": "2024-01-15T10:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too Many Requests - Rate limit exceeded",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Rate Limit Error",
                                    value = """
                                            {
                                              "status": "RATE_LIMITED",
                                              "message": "Rate limit exceeded. Please try again later.",
                                              "retryAfter": 60,
                                              "timestamp": "2024-01-15T10:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    value = """
                                            {
                                              "status": "ERROR",
                                              "message": "Internal server error occurred",
                                              "correlationId": "123e4567-e89b-12d3-a456-426614174000",
                                              "timestamp": "2024-01-15T10:30:00Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitOnboardingRequest(
            @Parameter(
                    description = "Client onboarding request data",
                    required = true,
                    schema = @Schema(implementation = ClientOnboardingRequest.class)
            )
            @Valid @RequestBody ClientOnboardingRequest request,
            
            @Parameter(
                    description = "Correlation ID for request tracking",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received client onboarding request for client: {}", request.getClientId());
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            request.setCorrelationId(correlationId);
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("clientId", request.getClientId());
            response.put("status", "ACCEPTED");
            response.put("message", "Client onboarding request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<ClientOnboardingRequest> message = MessageBuilder
                    .withPayload(request)
                    .setHeader("correlationId", correlationId)
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            clientOnboardingInputChannel.send(message);
            
            logger.info("Client onboarding request submitted successfully for client: {}", request.getClientId());
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting client onboarding request for client: {}", request.getClientId(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("clientId", request.getClientId());
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting client onboarding request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get client onboarding status
     */
    @Operation(
            summary = "Get Client Onboarding Status",
            description = """
                    Retrieves the current status of a client onboarding request.
                    
                    ## Status Values
                    - **PENDING**: Request received and queued for processing
                    - **PROCESSING**: Currently being processed through the workflow
                    - **COMPLETED**: Successfully completed all verification steps
                    - **FAILED**: Processing failed due to validation or service errors
                    - **CANCELLED**: Request was cancelled by user or system
                    
                    ## Response Information
                    - **Current Step**: Which step is currently being processed
                    - **Progress**: Percentage completion (0-100)
                    - **Estimated Time**: Remaining time to completion
                    - **Errors**: Any errors encountered during processing
                    - **Results**: Final verification results (when completed)
                    """,
            operationId = "getOnboardingStatus"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Processing Status",
                                    value = """
                                            {
                                              "clientId": "CLIENT_123e4567",
                                              "status": "PROCESSING",
                                              "currentStep": "KYC_VERIFICATION",
                                              "progress": 60,
                                              "message": "KYC verification in progress",
                                              "estimatedCompletionTime": "2024-01-15T10:35:00Z",
                                              "correlationId": "123e4567-e89b-12d3-a456-426614174000",
                                              "timestamp": "2024-01-15T10:32:00Z",
                                              "steps": [
                                                {
                                                  "name": "VALIDATION",
                                                  "status": "COMPLETED",
                                                  "completedAt": "2024-01-15T10:30:30Z"
                                                },
                                                {
                                                  "name": "KYC_VERIFICATION",
                                                  "status": "PROCESSING",
                                                  "startedAt": "2024-01-15T10:30:30Z"
                                                },
                                                {
                                                  "name": "DATA_PROCESSING",
                                                  "status": "PENDING"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Client onboarding request not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Not Found",
                                    value = """
                                            {
                                              "status": "NOT_FOUND",
                                              "message": "Client onboarding request not found",
                                              "clientId": "CLIENT_123e4567",
                                              "timestamp": "2024-01-15T10:30:00Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/status/{clientId}")
    public ResponseEntity<Map<String, Object>> getOnboardingStatus(
            @Parameter(
                    description = "Client ID to check status for",
                    example = "CLIENT_123e4567",
                    required = true
            )
            @PathVariable String clientId,
            
            @Parameter(
                    description = "Correlation ID for request tracking",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Getting onboarding status for client: {}", clientId);
        
        try {
            // In real implementation, this would query the database or cache
            Map<String, Object> statusResponse = new HashMap<>();
            statusResponse.put("clientId", clientId);
            statusResponse.put("correlationId", correlationId);
            statusResponse.put("status", "IN_PROGRESS");
            statusResponse.put("message", "Client onboarding is in progress");
            statusResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(statusResponse);
            
        } catch (Exception e) {
            logger.error("Error getting onboarding status for client: {}", clientId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("clientId", clientId);
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error getting onboarding status: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Cancel client onboarding request
     */
    @PostMapping("/cancel/{clientId}")
    public ResponseEntity<Map<String, Object>> cancelOnboardingRequest(
            @PathVariable String clientId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @RequestBody(required = false) Map<String, Object> cancelReason) {
        
        logger.info("Cancelling onboarding request for client: {}", clientId);
        
        try {
            // In real implementation, this would update the database and send cancellation message
            Map<String, Object> cancelResponse = new HashMap<>();
            cancelResponse.put("clientId", clientId);
            cancelResponse.put("correlationId", correlationId);
            cancelResponse.put("status", "CANCELLED");
            cancelResponse.put("message", "Client onboarding request cancelled successfully");
            cancelResponse.put("cancelReason", cancelReason != null ? cancelReason.get("reason") : "No reason provided");
            cancelResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(cancelResponse);
            
        } catch (Exception e) {
            logger.error("Error cancelling onboarding request for client: {}", clientId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("clientId", clientId);
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error cancelling onboarding request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "client-onboarding");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Get service metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("service", "client-onboarding");
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("activeRequests", 0); // In real implementation, this would be actual metrics
        metrics.put("totalRequests", 0);
        metrics.put("successfulRequests", 0);
        metrics.put("failedRequests", 0);
        metrics.put("averageProcessingTime", 0);
        
        return ResponseEntity.ok(metrics);
    }
}
