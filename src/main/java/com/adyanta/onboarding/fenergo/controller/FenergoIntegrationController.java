package com.adyanta.onboarding.fenergo.controller;

import com.adyanta.onboarding.fenergo.model.FenergoEntity;
import com.adyanta.onboarding.fenergo.model.FenergoJourney;
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
 * REST Controller for Fenergo Integration
 * Provides HTTP endpoints for Fenergo entity and journey management
 */
@RestController
@RequestMapping("/api/v1/fenergo")
@Validated
@Tag(name = "Fenergo Integration", description = "Fenergo entity, journey, and task management")
@SecurityRequirement(name = "bearerAuth")
public class FenergoIntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(FenergoIntegrationController.class);

    @Autowired
    @Qualifier("fenergoEntityInputChannel")
    private MessageChannel fenergoEntityInputChannel;

    @Autowired
    @Qualifier("fenergoJourneyInputChannel")
    private MessageChannel fenergoJourneyInputChannel;

    @Autowired
    @Qualifier("fenergoTaskInputChannel")
    private MessageChannel fenergoTaskInputChannel;

    /**
     * Create a new entity in Fenergo
     */
    @Operation(
            summary = "Create Fenergo Entity",
            description = """
                    Creates a new legal entity in the Fenergo system for client onboarding and compliance management.
                    
                    ## Entity Types
                    - **INDIVIDUAL**: Personal clients (individuals)
                    - **CORPORATE**: Corporate entities (companies, organizations)
                    - **TRUST**: Trust entities
                    - **PARTNERSHIP**: Partnership entities
                    
                    ## Required Fields
                    - **Entity Name**: Legal name of the entity
                    - **Entity Type**: Type of entity being created
                    - **Address**: Primary address information
                    - **Contact Information**: Email and phone number
                    
                    ## Process Flow
                    1. **Validation**: Entity data validation
                    2. **Fenergo API Call**: Create entity via Fenergo API
                    3. **Response Processing**: Handle creation response
                    4. **Status Update**: Update entity status
                    
                    ## Integration Features
                    - **Auto-mapping**: Maps client data to Fenergo entity structure
                    - **Policy Application**: Applies relevant compliance policies
                    - **Journey Initiation**: Can automatically start onboarding journey
                    """,
            operationId = "createFenergoEntity"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Entity created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "status": "CREATED",
                                              "message": "Fenergo entity created successfully",
                                              "entityId": "ENT_123e4567",
                                              "fenergoEntityId": "FEN_789abc123",
                                              "entityName": "John Doe",
                                              "entityType": "INDIVIDUAL",
                                              "correlationId": "123e4567-e89b-12d3-a456-426614174000",
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "fenergoResponse": {
                                                "entityId": "FEN_789abc123",
                                                "status": "ACTIVE",
                                                "createdAt": "2024-01-15T10:30:00Z"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid entity data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "status": "FAILED",
                                              "message": "Entity validation failed",
                                              "errors": [
                                                {
                                                  "field": "entityName",
                                                  "message": "Entity name is required"
                                                },
                                                {
                                                  "field": "entityType",
                                                  "message": "Invalid entity type"
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
                    responseCode = "409",
                    description = "Conflict - Entity already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Conflict Error",
                                    value = """
                                            {
                                              "status": "CONFLICT",
                                              "message": "Entity already exists in Fenergo",
                                              "entityId": "ENT_123e4567",
                                              "existingFenergoEntityId": "FEN_789abc123",
                                              "correlationId": "123e4567-e89b-12d3-a456-426614174000",
                                              "timestamp": "2024-01-15T10:30:00Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/entities")
    public ResponseEntity<Map<String, Object>> createEntity(
            @Parameter(
                    description = "Fenergo entity data",
                    required = true,
                    schema = @Schema(implementation = FenergoEntity.class)
            )
            @Valid @RequestBody FenergoEntity entity,
            
            @Parameter(
                    description = "Correlation ID for request tracking",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received Fenergo entity creation request: {}", entity.getEntityName());
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            entity.setCorrelationId(correlationId);
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("entityName", entity.getEntityName());
            response.put("status", "ACCEPTED");
            response.put("message", "Fenergo entity creation request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<FenergoEntity> message = MessageBuilder
                    .withPayload(entity)
                    .setHeader("correlationId", correlationId)
                    .setHeader("operation", "CREATE")
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            fenergoEntityInputChannel.send(message);
            
            logger.info("Fenergo entity creation request submitted successfully: {}", entity.getEntityName());
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting Fenergo entity creation request: {}", entity.getEntityName(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("entityName", entity.getEntityName());
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting Fenergo entity creation request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing entity in Fenergo
     */
    @PutMapping("/entities/{entityId}")
    public ResponseEntity<Map<String, Object>> updateEntity(
            @PathVariable String entityId,
            @Valid @RequestBody FenergoEntity entity,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received Fenergo entity update request: {}", entityId);
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            entity.setCorrelationId(correlationId);
            entity.setEntityId(entityId);
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("entityId", entityId);
            response.put("status", "ACCEPTED");
            response.put("message", "Fenergo entity update request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<FenergoEntity> message = MessageBuilder
                    .withPayload(entity)
                    .setHeader("correlationId", correlationId)
                    .setHeader("operation", "UPDATE")
                    .setHeader("entityId", entityId)
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            fenergoEntityInputChannel.send(message);
            
            logger.info("Fenergo entity update request submitted successfully: {}", entityId);
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting Fenergo entity update request: {}", entityId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("entityId", entityId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting Fenergo entity update request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create a new journey in Fenergo
     */
    @PostMapping("/journeys")
    public ResponseEntity<Map<String, Object>> createJourney(
            @Valid @RequestBody FenergoJourney journey,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received Fenergo journey creation request: {}", journey.getJourneyName());
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            journey.setCorrelationId(correlationId);
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("journeyName", journey.getJourneyName());
            response.put("entityId", journey.getEntityId());
            response.put("status", "ACCEPTED");
            response.put("message", "Fenergo journey creation request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<FenergoJourney> message = MessageBuilder
                    .withPayload(journey)
                    .setHeader("correlationId", correlationId)
                    .setHeader("operation", "CREATE")
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            fenergoJourneyInputChannel.send(message);
            
            logger.info("Fenergo journey creation request submitted successfully: {}", journey.getJourneyName());
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting Fenergo journey creation request: {}", journey.getJourneyName(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("journeyName", journey.getJourneyName());
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting Fenergo journey creation request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Start a journey in Fenergo
     */
    @PostMapping("/journeys/{journeyId}/start")
    public ResponseEntity<Map<String, Object>> startJourney(
            @PathVariable String journeyId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received Fenergo journey start request: {}", journeyId);
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("journeyId", journeyId);
            response.put("status", "ACCEPTED");
            response.put("message", "Fenergo journey start request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<String> message = MessageBuilder
                    .withPayload(journeyId)
                    .setHeader("correlationId", correlationId)
                    .setHeader("operation", "START")
                    .setHeader("journeyId", journeyId)
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            fenergoJourneyInputChannel.send(message);
            
            logger.info("Fenergo journey start request submitted successfully: {}", journeyId);
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting Fenergo journey start request: {}", journeyId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("journeyId", journeyId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting Fenergo journey start request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Complete a journey in Fenergo
     */
    @PostMapping("/journeys/{journeyId}/complete")
    public ResponseEntity<Map<String, Object>> completeJourney(
            @PathVariable String journeyId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received Fenergo journey completion request: {}", journeyId);
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("journeyId", journeyId);
            response.put("status", "ACCEPTED");
            response.put("message", "Fenergo journey completion request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<String> message = MessageBuilder
                    .withPayload(journeyId)
                    .setHeader("correlationId", correlationId)
                    .setHeader("operation", "COMPLETE")
                    .setHeader("journeyId", journeyId)
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            fenergoJourneyInputChannel.send(message);
            
            logger.info("Fenergo journey completion request submitted successfully: {}", journeyId);
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting Fenergo journey completion request: {}", journeyId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("journeyId", journeyId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting Fenergo journey completion request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Process a task in Fenergo
     */
    @PostMapping("/tasks")
    public ResponseEntity<Map<String, Object>> processTask(
            @RequestBody Map<String, Object> taskRequest,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        logger.info("Received Fenergo task processing request: {}", taskRequest.get("taskId"));
        
        try {
            // Set correlation ID if not provided
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Create initial response
            Map<String, Object> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("taskId", taskRequest.get("taskId"));
            response.put("taskType", taskRequest.get("taskType"));
            response.put("status", "ACCEPTED");
            response.put("message", "Fenergo task processing request accepted for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Send message to integration flow
            Message<Map<String, Object>> message = MessageBuilder
                    .withPayload(taskRequest)
                    .setHeader("correlationId", correlationId)
                    .setHeader("taskType", taskRequest.get("taskType"))
                    .setHeader("taskId", taskRequest.get("taskId"))
                    .setHeader("requestTimestamp", LocalDateTime.now())
                    .build();
            
            fenergoTaskInputChannel.send(message);
            
            logger.info("Fenergo task processing request submitted successfully: {}", taskRequest.get("taskId"));
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Error submitting Fenergo task processing request: {}", taskRequest.get("taskId"), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("taskId", taskRequest.get("taskId"));
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error submitting Fenergo task processing request: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get Fenergo integration status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFenergoStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "fenergo-integration");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "1.0.0");
        status.put("description", "Fenergo Integration Service for client onboarding");
        
        return ResponseEntity.ok(status);
    }

    /**
     * Get Fenergo integration metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getFenergoMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("service", "fenergo-integration");
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("activeEntities", 0); // In real implementation, this would be actual metrics
        metrics.put("activeJourneys", 0);
        metrics.put("activeTasks", 0);
        metrics.put("totalEntities", 0);
        metrics.put("totalJourneys", 0);
        metrics.put("totalTasks", 0);
        metrics.put("averageProcessingTime", 0);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Health check endpoint for Fenergo integration
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "fenergo-integration");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }
}
