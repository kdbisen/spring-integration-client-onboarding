package com.adyanta.onboarding.controller;

import com.adyanta.onboarding.config.MongoConfig;
import com.adyanta.onboarding.service.MongoTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB SSL Test Controller
 * Provides endpoints to test MongoDB SSL connection and JKS files
 */
@RestController
@RequestMapping("/api/v1/mongo")
@Tag(name = "MongoDB Testing", description = "MongoDB SSL connection and JKS file testing")
public class MongoTestController {

    private static final Logger logger = LoggerFactory.getLogger(MongoTestController.class);

    @Autowired
    private MongoConfig mongoConfig;

    @Autowired
    private MongoTestService mongoTestService;

    @Value("${spring.data.mongodb.ssl.keystore.path:/app/keystore/keystore.jks}")
    private String keystorePath;

    @Value("${spring.data.mongodb.ssl.truststore.path:/app/keystore/truststore.jks}")
    private String truststorePath;

    @Value("${spring.data.mongodb.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/client_onboarding}")
    private String mongoUri;

    /**
     * Test MongoDB SSL connection
     */
    @Operation(
            summary = "Test MongoDB SSL Connection",
            description = "Tests the MongoDB SSL connection using JKS files"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection test completed"),
            @ApiResponse(responseCode = "500", description = "Connection test failed")
    })
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testMongoConnection() {
        logger.info("Testing MongoDB SSL connection");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("sslEnabled", sslEnabled);
        response.put("mongoUri", mongoUri.replaceAll("password=[^&]*", "password=***"));
        
        try {
            boolean isHealthy = mongoConfig.isMongoConnectionHealthy();
            response.put("connectionStatus", isHealthy ? "SUCCESS" : "FAILED");
            response.put("message", isHealthy ? "MongoDB connection successful" : "MongoDB connection failed");
            
            if (isHealthy) {
                logger.info("MongoDB connection test passed");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("MongoDB connection test failed");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            logger.error("MongoDB connection test error", e);
            response.put("connectionStatus", "ERROR");
            response.put("message", "MongoDB connection error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Check JKS files status
     */
    @Operation(
            summary = "Check JKS Files Status",
            description = "Checks if JKS files are present and accessible"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JKS files check completed")
    })
    @GetMapping("/check-jks-files")
    public ResponseEntity<Map<String, Object>> checkJksFiles() {
        logger.info("Checking JKS files status");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("sslEnabled", sslEnabled);
        
        // Check keystore file
        Map<String, Object> keystoreInfo = checkJksFile(keystorePath, "keystore");
        response.put("keystore", keystoreInfo);
        
        // Check truststore file
        Map<String, Object> truststoreInfo = checkJksFile(truststorePath, "truststore");
        response.put("truststore", truststoreInfo);
        
        boolean allFilesPresent = (Boolean) keystoreInfo.get("exists") && (Boolean) truststoreInfo.get("exists");
        response.put("allFilesPresent", allFilesPresent);
        response.put("status", allFilesPresent ? "SUCCESS" : "FAILED");
        
        logger.info("JKS files check completed: {}", allFilesPresent ? "SUCCESS" : "FAILED");
        return ResponseEntity.ok(response);
    }

    /**
     * Test JKS file integrity
     */
    @Operation(
            summary = "Test JKS File Integrity",
            description = "Tests if JKS files are valid and can be opened"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JKS integrity test completed")
    })
    @GetMapping("/test-jks-integrity")
    public ResponseEntity<Map<String, Object>> testJksIntegrity() {
        logger.info("Testing JKS files integrity");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        // Test keystore integrity
        Map<String, Object> keystoreTest = testJksFileIntegrity(keystorePath, "keystore");
        response.put("keystore", keystoreTest);
        
        // Test truststore integrity
        Map<String, Object> truststoreTest = testJksFileIntegrity(truststorePath, "truststore");
        response.put("truststore", truststoreTest);
        
        boolean allFilesValid = (Boolean) keystoreTest.get("valid") && (Boolean) truststoreTest.get("valid");
        response.put("allFilesValid", allFilesValid);
        response.put("status", allFilesValid ? "SUCCESS" : "FAILED");
        
        logger.info("JKS integrity test completed: {}", allFilesValid ? "SUCCESS" : "FAILED");
        return ResponseEntity.ok(response);
    }

    /**
     * Get MongoDB configuration details
     */
    @Operation(
            summary = "Get MongoDB Configuration",
            description = "Returns MongoDB configuration details (without sensitive information)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully")
    })
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getMongoConfig() {
        logger.info("Retrieving MongoDB configuration");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("sslEnabled", sslEnabled);
        response.put("mongoUri", mongoUri.replaceAll("password=[^&]*", "password=***"));
        response.put("keystorePath", keystorePath);
        response.put("truststorePath", truststorePath);
        response.put("database", mongoConfig.getMongoDatabaseName());
        
        logger.info("MongoDB configuration retrieved");
        return ResponseEntity.ok(response);
    }

    /**
     * Test MongoDB basic connection
     */
    @Operation(
            summary = "Test MongoDB Basic Connection",
            description = "Tests basic MongoDB connection without SSL"
    )
    @GetMapping("/test-basic")
    public ResponseEntity<Map<String, Object>> testBasicConnection() {
        logger.info("Testing MongoDB basic connection");
        return ResponseEntity.ok(mongoTestService.testBasicConnection());
    }

    /**
     * Test MongoDB SSL connection
     */
    @Operation(
            summary = "Test MongoDB SSL Connection",
            description = "Tests MongoDB SSL connection with JKS files"
    )
    @GetMapping("/test-ssl")
    public ResponseEntity<Map<String, Object>> testSslConnection() {
        logger.info("Testing MongoDB SSL connection");
        return ResponseEntity.ok(mongoTestService.testSslConnection());
    }

    /**
     * Test MongoDB write operations
     */
    @Operation(
            summary = "Test MongoDB Write Operations",
            description = "Tests MongoDB write operations (insert, update, delete)"
    )
    @GetMapping("/test-write")
    public ResponseEntity<Map<String, Object>> testWriteOperations() {
        logger.info("Testing MongoDB write operations");
        return ResponseEntity.ok(mongoTestService.testWriteOperations());
    }

    /**
     * Test MongoDB read operations
     */
    @Operation(
            summary = "Test MongoDB Read Operations",
            description = "Tests MongoDB read operations (find, count, aggregate)"
    )
    @GetMapping("/test-read")
    public ResponseEntity<Map<String, Object>> testReadOperations() {
        logger.info("Testing MongoDB read operations");
        return ResponseEntity.ok(mongoTestService.testReadOperations());
    }

    /**
     * Test MongoDB authentication
     */
    @Operation(
            summary = "Test MongoDB Authentication",
            description = "Tests MongoDB authentication with SSL certificates"
    )
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuthentication() {
        logger.info("Testing MongoDB authentication");
        return ResponseEntity.ok(mongoTestService.testAuthentication());
    }

    /**
     * Run comprehensive MongoDB test suite
     */
    @Operation(
            summary = "Run Comprehensive MongoDB Test Suite",
            description = "Runs all MongoDB tests including connection, SSL, read/write operations, and authentication"
    )
    @GetMapping("/test-comprehensive")
    public ResponseEntity<Map<String, Object>> runComprehensiveTest() {
        logger.info("Running comprehensive MongoDB test suite");
        return ResponseEntity.ok(mongoTestService.runComprehensiveTest());
    }

    /**
     * Get MongoDB connection information
     */
    @Operation(
            summary = "Get MongoDB Connection Information",
            description = "Returns detailed MongoDB connection and server information"
    )
    @GetMapping("/connection-info")
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        logger.info("Getting MongoDB connection information");
        return ResponseEntity.ok(mongoTestService.getConnectionInfo());
    }

    /**
     * Check individual JKS file
     */
    private Map<String, Object> checkJksFile(String filePath, String fileType) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("path", filePath);
        fileInfo.put("type", fileType);
        
        File file = new File(filePath);
        fileInfo.put("exists", file.exists());
        
        if (file.exists()) {
            fileInfo.put("size", file.length());
            fileInfo.put("readable", file.canRead());
            fileInfo.put("lastModified", file.lastModified());
        }
        
        return fileInfo;
    }

    /**
     * Test JKS file integrity
     */
    private Map<String, Object> testJksFileIntegrity(String filePath, String fileType) {
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("path", filePath);
        testResult.put("type", fileType);
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                testResult.put("valid", false);
                testResult.put("error", "File does not exist");
                return testResult;
            }
            
            // Try to load the JKS file
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("JKS");
            keyStore.load(new java.io.FileInputStream(file), "dummy".toCharArray());
            
            testResult.put("valid", true);
            testResult.put("size", keyStore.size());
            testResult.put("message", "JKS file is valid");
            
        } catch (Exception e) {
            testResult.put("valid", false);
            testResult.put("error", e.getMessage());
            testResult.put("exception", e.getClass().getSimpleName());
        }
        
        return testResult;
    }
}
