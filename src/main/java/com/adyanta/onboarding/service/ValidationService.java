package com.adyanta.onboarding.service;

import com.adyanta.onboarding.model.ClientOnboardingRequest;
import com.adyanta.onboarding.model.ClientOnboardingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Validation Service for Client Onboarding
 * Validates incoming requests and performs business rule validation
 */
@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    
    private final Validator validator;

    public ValidationService(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validates the client onboarding request
     */
    public ClientOnboardingResponse validateRequest(ClientOnboardingRequest request) {
        logger.info("Starting validation for client: {}", request.getClientId());
        
        long startTime = System.currentTimeMillis();
        ClientOnboardingResponse response = new ClientOnboardingResponse();
        response.setClientId(request.getClientId());
        response.setCorrelationId(request.getCorrelationId());
        response.setStatus(ClientOnboardingResponse.OnboardingStatus.IN_PROGRESS);
        
        Map<String, ClientOnboardingResponse.StepResult> steps = new HashMap<>();
        
        try {
            // Step 1: Basic validation
            ClientOnboardingResponse.StepResult basicValidation = validateBasicFields(request);
            steps.put("basicValidation", basicValidation);
            
            if (!"SUCCESS".equals(basicValidation.getStatus())) {
                response.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
                response.setMessage("Basic validation failed");
                response.setProcessingSteps(steps);
                return response;
            }
            
            // Step 2: Business rule validation
            ClientOnboardingResponse.StepResult businessValidation = validateBusinessRules(request);
            steps.put("businessValidation", businessValidation);
            
            if (!"SUCCESS".equals(businessValidation.getStatus())) {
                response.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
                response.setMessage("Business validation failed");
                response.setProcessingSteps(steps);
                return response;
            }
            
            // Step 3: Data integrity validation
            ClientOnboardingResponse.StepResult dataIntegrityValidation = validateDataIntegrity(request);
            steps.put("dataIntegrityValidation", dataIntegrityValidation);
            
            if (!"SUCCESS".equals(dataIntegrityValidation.getStatus())) {
                response.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
                response.setMessage("Data integrity validation failed");
                response.setProcessingSteps(steps);
                return response;
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Validation completed successfully for client: {} in {}ms", 
                request.getClientId(), duration);
            
            response.setStatus(ClientOnboardingResponse.OnboardingStatus.COMPLETED);
            response.setMessage("Validation completed successfully");
            response.setProcessingSteps(steps);
            
        } catch (Exception e) {
            logger.error("Validation failed for client: {}", request.getClientId(), e);
            response.setStatus(ClientOnboardingResponse.OnboardingStatus.FAILED);
            response.setMessage("Validation failed: " + e.getMessage());
            response.setProcessingSteps(steps);
        }
        
        return response;
    }

    private ClientOnboardingResponse.StepResult validateBasicFields(ClientOnboardingRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "clientOnboardingRequest");
            validator.validate(request, errors);
            
            if (errors.hasErrors()) {
                StringBuilder errorMessage = new StringBuilder("Validation errors: ");
                errors.getAllErrors().forEach(error -> 
                    errorMessage.append(error.getDefaultMessage()).append("; "));
                
                return new ClientOnboardingResponse.StepResult(
                    "basicValidation", 
                    "FAILED", 
                    errorMessage.toString()
                );
            }
            
            long duration = System.currentTimeMillis() - startTime;
            ClientOnboardingResponse.StepResult result = new ClientOnboardingResponse.StepResult(
                "basicValidation", 
                "SUCCESS", 
                "Basic field validation passed"
            );
            result.setDurationMs(duration);
            return result;
            
        } catch (Exception e) {
            logger.error("Basic validation failed", e);
            return new ClientOnboardingResponse.StepResult(
                "basicValidation", 
                "FAILED", 
                "Basic validation error: " + e.getMessage()
            );
        }
    }

    private ClientOnboardingResponse.StepResult validateBusinessRules(ClientOnboardingRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Business Rule 1: Check if client ID is unique
            if (isClientIdAlreadyExists(request.getClientId())) {
                return new ClientOnboardingResponse.StepResult(
                    "businessValidation", 
                    "FAILED", 
                    "Client ID already exists"
                );
            }
            
            // Business Rule 2: Check document number format
            if (!isValidDocumentNumber(request.getDocumentType(), request.getDocumentNumber())) {
                return new ClientOnboardingResponse.StepResult(
                    "businessValidation", 
                    "FAILED", 
                    "Invalid document number format"
                );
            }
            
            // Business Rule 3: Check age requirements
            if (!meetsAgeRequirements(request)) {
                return new ClientOnboardingResponse.StepResult(
                    "businessValidation", 
                    "FAILED", 
                    "Client does not meet age requirements"
                );
            }
            
            long duration = System.currentTimeMillis() - startTime;
            ClientOnboardingResponse.StepResult result = new ClientOnboardingResponse.StepResult(
                "businessValidation", 
                "SUCCESS", 
                "Business rules validation passed"
            );
            result.setDurationMs(duration);
            return result;
            
        } catch (Exception e) {
            logger.error("Business validation failed", e);
            return new ClientOnboardingResponse.StepResult(
                "businessValidation", 
                "FAILED", 
                "Business validation error: " + e.getMessage()
            );
        }
    }

    private ClientOnboardingResponse.StepResult validateDataIntegrity(ClientOnboardingRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Data Integrity Check 1: Validate email format and domain
            if (!isValidEmailDomain(request.getEmail())) {
                return new ClientOnboardingResponse.StepResult(
                    "dataIntegrityValidation", 
                    "FAILED", 
                    "Invalid email domain"
                );
            }
            
            // Data Integrity Check 2: Validate phone number format
            if (!isValidPhoneNumber(request.getPhoneNumber())) {
                return new ClientOnboardingResponse.StepResult(
                    "dataIntegrityValidation", 
                    "FAILED", 
                    "Invalid phone number format"
                );
            }
            
            // Data Integrity Check 3: Validate address completeness
            if (!isAddressComplete(request.getAddress())) {
                return new ClientOnboardingResponse.StepResult(
                    "dataIntegrityValidation", 
                    "FAILED", 
                    "Incomplete address information"
                );
            }
            
            long duration = System.currentTimeMillis() - startTime;
            ClientOnboardingResponse.StepResult result = new ClientOnboardingResponse.StepResult(
                "dataIntegrityValidation", 
                "SUCCESS", 
                "Data integrity validation passed"
            );
            result.setDurationMs(duration);
            return result;
            
        } catch (Exception e) {
            logger.error("Data integrity validation failed", e);
            return new ClientOnboardingResponse.StepResult(
                "dataIntegrityValidation", 
                "FAILED", 
                "Data integrity validation error: " + e.getMessage()
            );
        }
    }

    // Helper methods for business rules
    private boolean isClientIdAlreadyExists(String clientId) {
        // Simulate database check
        // In real implementation, this would query the database
        return false;
    }

    private boolean isValidDocumentNumber(String documentType, String documentNumber) {
        // Simulate document number validation based on type
        return documentNumber != null && documentNumber.length() >= 5;
    }

    private boolean meetsAgeRequirements(ClientOnboardingRequest request) {
        // Simulate age validation
        // In real implementation, this would check birth date
        return true;
    }

    private boolean isValidEmailDomain(String email) {
        // Simulate email domain validation
        return email != null && email.contains("@");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Simulate phone number validation
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    private boolean isAddressComplete(ClientOnboardingRequest.Address address) {
        return address != null && 
               address.getStreet() != null && !address.getStreet().trim().isEmpty() &&
               address.getCity() != null && !address.getCity().trim().isEmpty() &&
               address.getState() != null && !address.getState().trim().isEmpty() &&
               address.getPostalCode() != null && !address.getPostalCode().trim().isEmpty() &&
               address.getCountry() != null && !address.getCountry().trim().isEmpty();
    }
}
