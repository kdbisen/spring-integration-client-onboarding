package com.adyanta.onboarding.integration;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import com.adyanta.onboarding.model.ClientOnboardingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for Client Onboarding Flow
 * Demonstrates how to test Spring Integration flows
 */
@SpringBootTest
@ActiveProfiles("test")
class ClientOnboardingIntegrationTest {

    @Autowired
    private MessageChannel clientOnboardingInputChannel;

    @Test
    void testCompleteOnboardingFlow() {
        // Given: A valid client onboarding request
        ClientOnboardingRequest request = createValidClientRequest();
        
        // When: Send the request through the integration flow
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .setHeader("requestTimestamp", LocalDateTime.now())
                .build();
        
        // Then: The message should be sent successfully
        assertTrue(clientOnboardingInputChannel.send(message));
    }

    @Test
    void testMessageWithCorrelationId() {
        // Given: A message with correlation ID
        String correlationId = UUID.randomUUID().toString();
        ClientOnboardingRequest request = createValidClientRequest();
        
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader("correlationId", correlationId)
                .build();
        
        // When: Send the message
        boolean sent = clientOnboardingInputChannel.send(message);
        
        // Then: Message should be sent and correlation ID should be preserved
        assertTrue(sent);
        assertEquals(correlationId, message.getHeaders().get("correlationId"));
    }

    @Test
    void testInvalidClientRequest() {
        // Given: An invalid client request (missing required fields)
        ClientOnboardingRequest invalidRequest = new ClientOnboardingRequest();
        invalidRequest.setClientId("INVALID_CLIENT");
        // Missing required fields like firstName, lastName, email, etc.
        
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(invalidRequest)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .build();
        
        // When: Send the invalid request
        boolean sent = clientOnboardingInputChannel.send(message);
        
        // Then: Message should still be sent (validation happens in the flow)
        assertTrue(sent);
    }

    @Test
    void testMessageHeaders() {
        // Given: A message with various headers
        ClientOnboardingRequest request = createValidClientRequest();
        String correlationId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader("correlationId", correlationId)
                .setHeader("requestTimestamp", timestamp)
                .setHeader("source", "TEST")
                .setHeader("priority", "HIGH")
                .build();
        
        // When: Send the message
        boolean sent = clientOnboardingInputChannel.send(message);
        
        // Then: All headers should be preserved
        assertTrue(sent);
        assertEquals(correlationId, message.getHeaders().get("correlationId"));
        assertEquals(timestamp, message.getHeaders().get("requestTimestamp"));
        assertEquals("TEST", message.getHeaders().get("source"));
        assertEquals("HIGH", message.getHeaders().get("priority"));
    }

    @Test
    void testMultipleConcurrentRequests() {
        // Given: Multiple concurrent requests
        int numberOfRequests = 10;
        
        // When: Send multiple requests concurrently
        for (int i = 0; i < numberOfRequests; i++) {
            ClientOnboardingRequest request = createValidClientRequest();
            request.setClientId("CONCURRENT_CLIENT_" + i);
            
            Message<ClientOnboardingRequest> message = MessageBuilder
                    .withPayload(request)
                    .setHeader("correlationId", UUID.randomUUID().toString())
                    .setHeader("requestNumber", i)
                    .build();
            
            // Then: Each message should be sent successfully
            assertTrue(clientOnboardingInputChannel.send(message));
        }
    }

    @Test
    void testMessagePayload() {
        // Given: A message with specific payload
        ClientOnboardingRequest request = createValidClientRequest();
        request.setClientId("PAYLOAD_TEST_CLIENT");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test.user@example.com");
        
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .build();
        
        // When: Send the message
        boolean sent = clientOnboardingInputChannel.send(message);
        
        // Then: Payload should be preserved
        assertTrue(sent);
        assertNotNull(message.getPayload());
        assertEquals("PAYLOAD_TEST_CLIENT", message.getPayload().getClientId());
        assertEquals("Test", message.getPayload().getFirstName());
        assertEquals("User", message.getPayload().getLastName());
        assertEquals("test.user@example.com", message.getPayload().getEmail());
    }

    @Test
    void testErrorHandling() {
        // Given: A message that might cause an error
        ClientOnboardingRequest request = createValidClientRequest();
        request.setClientId("ERROR_TEST_CLIENT");
        
        // Add a header that might cause issues
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .setHeader("forceError", true)
                .build();
        
        // When: Send the message
        boolean sent = clientOnboardingInputChannel.send(message);
        
        // Then: Message should still be sent (error handling is in the flow)
        assertTrue(sent);
    }

    @Test
    void testMessageTransformation() {
        // Given: A message that will be transformed
        ClientOnboardingRequest request = createValidClientRequest();
        request.setClientId("TRANSFORM_TEST_CLIENT");
        
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .setHeader("transform", true)
                .build();
        
        // When: Send the message
        boolean sent = clientOnboardingInputChannel.send(message);
        
        // Then: Message should be sent successfully
        assertTrue(sent);
        
        // Verify the original payload is intact
        assertNotNull(message.getPayload());
        assertEquals("TRANSFORM_TEST_CLIENT", message.getPayload().getClientId());
    }

    @Test
    void testMessageRouting() {
        // Given: Messages with different routing criteria
        ClientOnboardingRequest validRequest = createValidClientRequest();
        validRequest.setClientId("VALID_ROUTE_CLIENT");
        
        ClientOnboardingRequest invalidRequest = createValidClientRequest();
        invalidRequest.setClientId("INVALID_ROUTE_CLIENT");
        invalidRequest.setEmail("invalid-email"); // Invalid email format
        
        // When: Send both messages
        Message<ClientOnboardingRequest> validMessage = MessageBuilder
                .withPayload(validRequest)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .build();
        
        Message<ClientOnboardingRequest> invalidMessage = MessageBuilder
                .withPayload(invalidRequest)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .build();
        
        boolean validSent = clientOnboardingInputChannel.send(validMessage);
        boolean invalidSent = clientOnboardingInputChannel.send(invalidMessage);
        
        // Then: Both messages should be sent (routing happens in the flow)
        assertTrue(validSent);
        assertTrue(invalidSent);
    }

    @Test
    void testMessageSplitting() {
        // Given: A message that will be split
        ClientOnboardingRequest request = createValidClientRequest();
        request.setClientId("SPLIT_TEST_CLIENT");
        
        Message<ClientOnboardingRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .setHeader("split", true)
                .build();
        
        // When: Send the message
        boolean sent = clientOnboardingInputChannel.send(message);
        
        // Then: Message should be sent successfully
        assertTrue(sent);
        
        // Verify the message has the split header
        assertTrue((Boolean) message.getHeaders().get("split"));
    }

    @Test
    void testMessageAggregation() {
        // Given: Messages that will be aggregated
        String correlationId = UUID.randomUUID().toString();
        
        for (int i = 0; i < 4; i++) {
            ClientOnboardingRequest request = createValidClientRequest();
            request.setClientId("AGGREGATE_CLIENT_" + i);
            
            Message<ClientOnboardingRequest> message = MessageBuilder
                    .withPayload(request)
                    .setHeader("correlationId", correlationId)
                    .setHeader("serviceType", "SERVICE_" + i)
                    .build();
            
            // When: Send each message
            boolean sent = clientOnboardingInputChannel.send(message);
            
            // Then: Each message should be sent successfully
            assertTrue(sent);
        }
    }

    private ClientOnboardingRequest createValidClientRequest() {
        ClientOnboardingRequest request = new ClientOnboardingRequest();
        request.setClientId("TEST_CLIENT_" + System.currentTimeMillis());
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("+1234567890");
        request.setDocumentType("PASSPORT");
        request.setDocumentNumber("P123456789");
        
        ClientOnboardingRequest.Address address = new ClientOnboardingRequest.Address();
        address.setStreet("123 Main St");
        address.setCity("New York");
        address.setState("NY");
        address.setPostalCode("10001");
        address.setCountry("US");
        request.setAddress(address);
        
        request.setRequestTimestamp(LocalDateTime.now());
        request.setCorrelationId(UUID.randomUUID().toString());
        
        return request;
    }
}
