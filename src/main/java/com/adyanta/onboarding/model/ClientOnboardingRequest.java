package com.adyanta.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Client onboarding request model
 */
public class ClientOnboardingRequest {
    
    @NotBlank(message = "Client ID is required")
    @JsonProperty("clientId")
    private String clientId;
    
    @NotBlank(message = "First name is required")
    @JsonProperty("firstName")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @JsonProperty("lastName")
    private String lastName;
    
    @Email(message = "Valid email is required")
    @JsonProperty("email")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Valid phone number is required")
    @JsonProperty("phoneNumber")
    private String phoneNumber;
    
    @NotBlank(message = "Document type is required")
    @JsonProperty("documentType")
    private String documentType;
    
    @NotBlank(message = "Document number is required")
    @JsonProperty("documentNumber")
    private String documentNumber;
    
    @NotNull(message = "Address is required")
    @JsonProperty("address")
    private Address address;
    
    @JsonProperty("additionalData")
    private Map<String, Object> additionalData;
    
    @JsonProperty("requestTimestamp")
    private LocalDateTime requestTimestamp;
    
    @JsonProperty("correlationId")
    private String correlationId;

    // Constructors
    public ClientOnboardingRequest() {
        this.requestTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public static class Address {
        @NotBlank(message = "Street is required")
        private String street;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Postal code is required")
        private String postalCode;
        
        @NotBlank(message = "Country is required")
        private String country;

        // Constructors
        public Address() {}

        // Getters and Setters
        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
