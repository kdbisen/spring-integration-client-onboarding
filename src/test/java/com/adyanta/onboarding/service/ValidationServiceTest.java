package com.adyanta.onboarding.service;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import com.adyanta.onboarding.model.ClientOnboardingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for ValidationService
 * Demonstrates how to test Spring Integration service components
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationService validationService;

    private ClientOnboardingRequest validRequest;
    private ClientOnboardingRequest invalidRequest;

    @BeforeEach
    void setUp() {
        // Create valid request
        validRequest = createValidRequest();
        
        // Create invalid request
        invalidRequest = createInvalidRequest();
    }

    @Test
    void testValidateRequest_Success() {
        // Given: A valid request
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenAnswer(invocation -> {
                    BeanPropertyBindingResult result = invocation.getArgument(1);
                    // No errors added - validation passes
                    return null;
                });

        // When: Validate the request
        ClientOnboardingResponse response = validationService.validateRequest(validRequest);

        // Then: Response should be successful
        assertNotNull(response);
        assertEquals(validRequest.getClientId(), response.getClientId());
        assertEquals(ClientOnboardingResponse.OnboardingStatus.COMPLETED, response.getStatus());
        assertEquals("Validation completed successfully", response.getMessage());
        assertNotNull(response.getProcessingSteps());
        assertTrue(response.getProcessingSteps().containsKey("basicValidation"));
        assertTrue(response.getProcessingSteps().containsKey("businessValidation"));
        assertTrue(response.getProcessingSteps().containsKey("dataIntegrityValidation"));
    }

    @Test
    void testValidateRequest_BasicValidationFailure() {
        // Given: A request with basic validation errors
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenAnswer(invocation -> {
                    BeanPropertyBindingResult result = invocation.getArgument(1);
                    result.rejectValue("email", "email.invalid", "Invalid email format");
                    return null;
                });

        // When: Validate the request
        ClientOnboardingResponse response = validationService.validateRequest(invalidRequest);

        // Then: Response should indicate failure
        assertNotNull(response);
        assertEquals(invalidRequest.getClientId(), response.getClientId());
        assertEquals(ClientOnboardingResponse.OnboardingStatus.FAILED, response.getStatus());
        assertEquals("Basic validation failed", response.getMessage());
        assertNotNull(response.getProcessingSteps());
        
        ClientOnboardingResponse.StepResult basicValidation = response.getProcessingSteps().get("basicValidation");
        assertNotNull(basicValidation);
        assertEquals("FAILED", basicValidation.getStatus());
        assertTrue(basicValidation.getMessage().contains("Validation errors"));
    }

    @Test
    void testValidateRequest_BusinessValidationFailure() {
        // Given: A request that passes basic validation but fails business validation
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenAnswer(invocation -> {
                    BeanPropertyBindingResult result = invocation.getArgument(1);
                    // No basic validation errors
                    return null;
                });

        // When: Validate the request (this will fail business validation due to invalid email)
        ClientOnboardingResponse response = validationService.validateRequest(invalidRequest);

        // Then: Response should indicate business validation failure
        assertNotNull(response);
        assertEquals(invalidRequest.getClientId(), response.getClientId());
        assertEquals(ClientOnboardingResponse.OnboardingStatus.FAILED, response.getStatus());
        assertEquals("Business validation failed", response.getMessage());
        
        ClientOnboardingResponse.StepResult businessValidation = response.getProcessingSteps().get("businessValidation");
        assertNotNull(businessValidation);
        assertEquals("FAILED", businessValidation.getStatus());
    }

    @Test
    void testValidateRequest_DataIntegrityValidationFailure() {
        // Given: A request that passes basic and business validation but fails data integrity
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenAnswer(invocation -> {
                    BeanPropertyBindingResult result = invocation.getArgument(1);
                    // No basic validation errors
                    return null;
                });

        // Create a request that will fail data integrity validation
        ClientOnboardingRequest request = createValidRequest();
        request.setEmail("invalid-email-format"); // Invalid email format

        // When: Validate the request
        ClientOnboardingResponse response = validationService.validateRequest(request);

        // Then: Response should indicate data integrity validation failure
        assertNotNull(response);
        assertEquals(request.getClientId(), response.getClientId());
        assertEquals(ClientOnboardingResponse.OnboardingStatus.FAILED, response.getStatus());
        assertEquals("Data integrity validation failed", response.getMessage());
        
        ClientOnboardingResponse.StepResult dataIntegrityValidation = response.getProcessingSteps().get("dataIntegrityValidation");
        assertNotNull(dataIntegrityValidation);
        assertEquals("FAILED", dataIntegrityValidation.getStatus());
    }

    @Test
    void testValidateRequest_ExceptionHandling() {
        // Given: A request that causes an exception during validation
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenThrow(new RuntimeException("Validation service error"));

        // When: Validate the request
        ClientOnboardingResponse response = validationService.validateRequest(validRequest);

        // Then: Response should indicate failure due to exception
        assertNotNull(response);
        assertEquals(validRequest.getClientId(), response.getClientId());
        assertEquals(ClientOnboardingResponse.OnboardingStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Validation failed"));
    }

    @Test
    void testValidateRequest_ProcessingSteps() {
        // Given: A valid request
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenAnswer(invocation -> {
                    BeanPropertyBindingResult result = invocation.getArgument(1);
                    // No errors added - validation passes
                    return null;
                });

        // When: Validate the request
        ClientOnboardingResponse response = validationService.validateRequest(validRequest);

        // Then: All processing steps should be present and successful
        assertNotNull(response.getProcessingSteps());
        assertEquals(3, response.getProcessingSteps().size());
        
        // Check basic validation step
        ClientOnboardingResponse.StepResult basicValidation = response.getProcessingSteps().get("basicValidation");
        assertNotNull(basicValidation);
        assertEquals("basicValidation", basicValidation.getStepName());
        assertEquals("SUCCESS", basicValidation.getStatus());
        assertEquals("Basic field validation passed", basicValidation.getMessage());
        assertNotNull(basicValidation.getTimestamp());
        assertNotNull(basicValidation.getDurationMs());
        
        // Check business validation step
        ClientOnboardingResponse.StepResult businessValidation = response.getProcessingSteps().get("businessValidation");
        assertNotNull(businessValidation);
        assertEquals("businessValidation", businessValidation.getStepName());
        assertEquals("SUCCESS", businessValidation.getStatus());
        assertEquals("Business rules validation passed", businessValidation.getMessage());
        assertNotNull(businessValidation.getTimestamp());
        assertNotNull(businessValidation.getDurationMs());
        
        // Check data integrity validation step
        ClientOnboardingResponse.StepResult dataIntegrityValidation = response.getProcessingSteps().get("dataIntegrityValidation");
        assertNotNull(dataIntegrityValidation);
        assertEquals("dataIntegrityValidation", dataIntegrityValidation.getStepName());
        assertEquals("SUCCESS", dataIntegrityValidation.getStatus());
        assertEquals("Data integrity validation passed", dataIntegrityValidation.getMessage());
        assertNotNull(dataIntegrityValidation.getTimestamp());
        assertNotNull(dataIntegrityValidation.getDurationMs());
    }

    @Test
    void testValidateRequest_CorrelationId() {
        // Given: A request with correlation ID
        String correlationId = "TEST_CORRELATION_123";
        validRequest.setCorrelationId(correlationId);
        
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenAnswer(invocation -> {
                    BeanPropertyBindingResult result = invocation.getArgument(1);
                    // No errors added - validation passes
                    return null;
                });

        // When: Validate the request
        ClientOnboardingResponse response = validationService.validateRequest(validRequest);

        // Then: Correlation ID should be preserved
        assertNotNull(response);
        assertEquals(correlationId, response.getCorrelationId());
    }

    @Test
    void testValidateRequest_Timing() {
        // Given: A valid request
        when(validator.validate(any(ClientOnboardingRequest.class), any(BeanPropertyBindingResult.class)))
                .thenAnswer(invocation -> {
                    BeanPropertyBindingResult result = invocation.getArgument(1);
                    // No errors added - validation passes
                    return null;
                });

        // When: Validate the request
        long startTime = System.currentTimeMillis();
        ClientOnboardingResponse response = validationService.validateRequest(validRequest);
        long endTime = System.currentTimeMillis();

        // Then: Response should include timing information
        assertNotNull(response);
        assertNotNull(response.getProcessingSteps());
        
        // Check that duration is reasonable
        for (ClientOnboardingResponse.StepResult step : response.getProcessingSteps().values()) {
            assertNotNull(step.getDurationMs());
            assertTrue(step.getDurationMs() >= 0);
            assertTrue(step.getDurationMs() < (endTime - startTime) + 100); // Allow some margin
        }
    }

    private ClientOnboardingRequest createValidRequest() {
        ClientOnboardingRequest request = new ClientOnboardingRequest();
        request.setClientId("VALID_CLIENT_123");
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
        
        request.setRequestTimestamp(java.time.LocalDateTime.now());
        request.setCorrelationId("TEST_CORRELATION_123");
        
        return request;
    }

    private ClientOnboardingRequest createInvalidRequest() {
        ClientOnboardingRequest request = new ClientOnboardingRequest();
        request.setClientId("INVALID_CLIENT_456");
        request.setFirstName(""); // Invalid - empty first name
        request.setLastName(""); // Invalid - empty last name
        request.setEmail("invalid-email"); // Invalid email format
        request.setPhoneNumber("123"); // Invalid phone number
        request.setDocumentType(""); // Invalid - empty document type
        request.setDocumentNumber(""); // Invalid - empty document number
        
        // Invalid address
        ClientOnboardingRequest.Address address = new ClientOnboardingRequest.Address();
        address.setStreet(""); // Invalid - empty street
        address.setCity(""); // Invalid - empty city
        address.setState(""); // Invalid - empty state
        address.setPostalCode(""); // Invalid - empty postal code
        address.setCountry(""); // Invalid - empty country
        request.setAddress(address);
        
        request.setRequestTimestamp(java.time.LocalDateTime.now());
        request.setCorrelationId("TEST_CORRELATION_456");
        
        return request;
    }
}
