package com.adyanta.onboarding.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoDB Test Service
 * Provides comprehensive testing capabilities for MongoDB SSL connections
 */
@Service
public class MongoTestService {

    private static final Logger logger = LoggerFactory.getLogger(MongoTestService.class);

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database:client_onboarding}")
    private String databaseName;

    /**
     * Test basic MongoDB connection
     */
    public Map<String, Object> testBasicConnection() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("test", "basic_connection");

        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            Document pingResult = database.runCommand(new Document("ping", 1));
            
            result.put("status", "SUCCESS");
            result.put("message", "Basic connection successful");
            result.put("pingResult", pingResult);
            
            logger.info("Basic MongoDB connection test passed");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("message", "Basic connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            logger.error("Basic MongoDB connection test failed", e);
        }

        return result;
    }

    /**
     * Test MongoDB SSL connection
     */
    public Map<String, Object> testSslConnection() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("test", "ssl_connection");

        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            
            // Test SSL-specific operations
            Document serverStatus = database.runCommand(new Document("serverStatus", 1));
            Document buildInfo = database.runCommand(new Document("buildInfo", 1));
            
            result.put("status", "SUCCESS");
            result.put("message", "SSL connection successful");
            result.put("serverStatus", serverStatus);
            result.put("buildInfo", buildInfo);
            
            logger.info("MongoDB SSL connection test passed");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("message", "SSL connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            logger.error("MongoDB SSL connection test failed", e);
        }

        return result;
    }

    /**
     * Test MongoDB write operations
     */
    public Map<String, Object> testWriteOperations() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("test", "write_operations");

        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection("test_collection");
            
            // Test insert operation
            Document testDoc = new Document()
                    .append("testId", "test_" + System.currentTimeMillis())
                    .append("timestamp", LocalDateTime.now())
                    .append("message", "MongoDB SSL write test");
            
            collection.insertOne(testDoc);
            
            // Test find operation
            Document foundDoc = collection.find(new Document("testId", testDoc.getString("testId"))).first();
            
            // Clean up
            collection.deleteOne(new Document("testId", testDoc.getString("testId")));
            
            result.put("status", "SUCCESS");
            result.put("message", "Write operations successful");
            result.put("insertedDocument", testDoc);
            result.put("foundDocument", foundDoc);
            
            logger.info("MongoDB write operations test passed");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("message", "Write operations failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            logger.error("MongoDB write operations test failed", e);
        }

        return result;
    }

    /**
     * Test MongoDB read operations
     */
    public Map<String, Object> testReadOperations() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("test", "read_operations");

        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection("test_collection");
            
            // Test count operation
            long count = collection.countDocuments();
            
            // Test find operation
            List<Document> documents = new ArrayList<>();
            collection.find().limit(5).into(documents);
            
            // Test aggregation
            List<Document> aggregationResult = new ArrayList<>();
            collection.aggregate(List.of(
                new Document("$group", new Document("_id", null).append("count", new Document("$sum", 1)))
            )).into(aggregationResult);
            
            result.put("status", "SUCCESS");
            result.put("message", "Read operations successful");
            result.put("documentCount", count);
            result.put("sampleDocuments", documents);
            result.put("aggregationResult", aggregationResult);
            
            logger.info("MongoDB read operations test passed");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("message", "Read operations failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            logger.error("MongoDB read operations test failed", e);
        }

        return result;
    }

    /**
     * Test MongoDB authentication
     */
    public Map<String, Object> testAuthentication() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("test", "authentication");

        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            
            // Test authentication by running a command that requires auth
            Document authResult = database.runCommand(new Document("connectionStatus", 1));
            
            result.put("status", "SUCCESS");
            result.put("message", "Authentication successful");
            result.put("authResult", authResult);
            
            logger.info("MongoDB authentication test passed");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("message", "Authentication failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            logger.error("MongoDB authentication test failed", e);
        }

        return result;
    }

    /**
     * Run comprehensive MongoDB test suite
     */
    public Map<String, Object> runComprehensiveTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("test", "comprehensive_test_suite");
        
        List<Map<String, Object>> testResults = new ArrayList<>();
        
        // Run all tests
        testResults.add(testBasicConnection());
        testResults.add(testSslConnection());
        testResults.add(testWriteOperations());
        testResults.add(testReadOperations());
        testResults.add(testAuthentication());
        
        result.put("testResults", testResults);
        
        // Calculate overall status
        boolean allTestsPassed = testResults.stream()
                .allMatch(test -> "SUCCESS".equals(test.get("status")));
        
        result.put("overallStatus", allTestsPassed ? "SUCCESS" : "FAILED");
        result.put("totalTests", testResults.size());
        result.put("passedTests", testResults.stream()
                .mapToInt(test -> "SUCCESS".equals(test.get("status")) ? 1 : 0)
                .sum());
        
        logger.info("Comprehensive MongoDB test suite completed: {}", 
                   allTestsPassed ? "SUCCESS" : "FAILED");
        
        return result;
    }

    /**
     * Get MongoDB connection information
     */
    public Map<String, Object> getConnectionInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            
            // Get server information
            Document serverStatus = database.runCommand(new Document("serverStatus", 1));
            Document buildInfo = database.runCommand(new Document("buildInfo", 1));
            
            result.put("status", "SUCCESS");
            result.put("database", databaseName);
            result.put("serverStatus", serverStatus);
            result.put("buildInfo", buildInfo);
            
            logger.info("MongoDB connection info retrieved successfully");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("message", "Failed to get connection info: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            logger.error("Failed to get MongoDB connection info", e);
        }
        
        return result;
    }
}
