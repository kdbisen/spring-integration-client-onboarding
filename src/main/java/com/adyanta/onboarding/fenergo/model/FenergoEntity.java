package com.adyanta.onboarding.fenergo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Fenergo Legal Entity model
 * Represents a client/customer entity in Fenergo
 */
public class FenergoEntity {
    
    @JsonProperty("entityId")
    private String entityId;
    
    @NotBlank(message = "Entity name is required")
    @JsonProperty("entityName")
    private String entityName;
    
    @JsonProperty("entityType")
    private EntityType entityType;
    
    @JsonProperty("status")
    private EntityStatus status;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("lastModifiedDate")
    private LocalDateTime lastModifiedDate;
    
    @JsonProperty("personalInformation")
    private PersonalInformation personalInformation;
    
    @JsonProperty("corporateInformation")
    private CorporateInformation corporateInformation;
    
    @JsonProperty("addresses")
    private List<Address> addresses;
    
    @JsonProperty("ownership")
    private List<Ownership> ownership;
    
    @JsonProperty("complianceData")
    private ComplianceData complianceData;
    
    @JsonProperty("kycData")
    private KycData kycData;
    
    @JsonProperty("additionalData")
    private Map<String, Object> additionalData;
    
    @JsonProperty("correlationId")
    private String correlationId;

    // Constructors
    public FenergoEntity() {
        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
        this.status = EntityStatus.DRAFT;
        this.version = "1.0";
    }

    // Getters and Setters
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public PersonalInformation getPersonalInformation() {
        return personalInformation;
    }

    public void setPersonalInformation(PersonalInformation personalInformation) {
        this.personalInformation = personalInformation;
    }

    public CorporateInformation getCorporateInformation() {
        return corporateInformation;
    }

    public void setCorporateInformation(CorporateInformation corporateInformation) {
        this.corporateInformation = corporateInformation;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Ownership> getOwnership() {
        return ownership;
    }

    public void setOwnership(List<Ownership> ownership) {
        this.ownership = ownership;
    }

    public ComplianceData getComplianceData() {
        return complianceData;
    }

    public void setComplianceData(ComplianceData complianceData) {
        this.complianceData = complianceData;
    }

    public KycData getKycData() {
        return kycData;
    }

    public void setKycData(KycData kycData) {
        this.kycData = kycData;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    // Enums
    public enum EntityType {
        INDIVIDUAL,
        CORPORATE,
        PARTNERSHIP,
        TRUST,
        FOUNDATION
    }

    public enum EntityStatus {
        DRAFT,
        PENDING_VERIFICATION,
        VERIFIED,
        REJECTED,
        SUSPENDED,
        CLOSED
    }

    // Inner Classes
    public static class PersonalInformation {
        @JsonProperty("firstName")
        private String firstName;
        
        @JsonProperty("lastName")
        private String lastName;
        
        @JsonProperty("middleName")
        private String middleName;
        
        @JsonProperty("dateOfBirth")
        private LocalDateTime dateOfBirth;
        
        @JsonProperty("gender")
        private String gender;
        
        @JsonProperty("nationality")
        private String nationality;
        
        @JsonProperty("maritalStatus")
        private String maritalStatus;
        
        @JsonProperty("occupation")
        private String occupation;

        // Getters and Setters
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

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public LocalDateTime getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDateTime dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public String getMaritalStatus() {
            return maritalStatus;
        }

        public void setMaritalStatus(String maritalStatus) {
            this.maritalStatus = maritalStatus;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }
    }

    public static class CorporateInformation {
        @JsonProperty("legalName")
        private String legalName;
        
        @JsonProperty("tradingName")
        private String tradingName;
        
        @JsonProperty("registrationNumber")
        private String registrationNumber;
        
        @JsonProperty("incorporationDate")
        private LocalDateTime incorporationDate;
        
        @JsonProperty("incorporationCountry")
        private String incorporationCountry;
        
        @JsonProperty("businessType")
        private String businessType;
        
        @JsonProperty("industrySector")
        private String industrySector;
        
        @JsonProperty("authorizedCapital")
        private Double authorizedCapital;
        
        @JsonProperty("paidUpCapital")
        private Double paidUpCapital;

        // Getters and Setters
        public String getLegalName() {
            return legalName;
        }

        public void setLegalName(String legalName) {
            this.legalName = legalName;
        }

        public String getTradingName() {
            return tradingName;
        }

        public void setTradingName(String tradingName) {
            this.tradingName = tradingName;
        }

        public String getRegistrationNumber() {
            return registrationNumber;
        }

        public void setRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
        }

        public LocalDateTime getIncorporationDate() {
            return incorporationDate;
        }

        public void setIncorporationDate(LocalDateTime incorporationDate) {
            this.incorporationDate = incorporationDate;
        }

        public String getIncorporationCountry() {
            return incorporationCountry;
        }

        public void setIncorporationCountry(String incorporationCountry) {
            this.incorporationCountry = incorporationCountry;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getIndustrySector() {
            return industrySector;
        }

        public void setIndustrySector(String industrySector) {
            this.industrySector = industrySector;
        }

        public Double getAuthorizedCapital() {
            return authorizedCapital;
        }

        public void setAuthorizedCapital(Double authorizedCapital) {
            this.authorizedCapital = authorizedCapital;
        }

        public Double getPaidUpCapital() {
            return paidUpCapital;
        }

        public void setPaidUpCapital(Double paidUpCapital) {
            this.paidUpCapital = paidUpCapital;
        }
    }

    public static class Address {
        @JsonProperty("addressType")
        private String addressType;
        
        @JsonProperty("street")
        private String street;
        
        @JsonProperty("city")
        private String city;
        
        @JsonProperty("state")
        private String state;
        
        @JsonProperty("postalCode")
        private String postalCode;
        
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("isPrimary")
        private Boolean isPrimary;

        // Getters and Setters
        public String getAddressType() {
            return addressType;
        }

        public void setAddressType(String addressType) {
            this.addressType = addressType;
        }

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

        public Boolean getIsPrimary() {
            return isPrimary;
        }

        public void setIsPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
        }
    }

    public static class Ownership {
        @JsonProperty("ownerType")
        private String ownerType;
        
        @JsonProperty("ownerName")
        private String ownerName;
        
        @JsonProperty("ownershipPercentage")
        private Double ownershipPercentage;
        
        @JsonProperty("isBeneficialOwner")
        private Boolean isBeneficialOwner;

        // Getters and Setters
        public String getOwnerType() {
            return ownerType;
        }

        public void setOwnerType(String ownerType) {
            this.ownerType = ownerType;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public Double getOwnershipPercentage() {
            return ownershipPercentage;
        }

        public void setOwnershipPercentage(Double ownershipPercentage) {
            this.ownershipPercentage = ownershipPercentage;
        }

        public Boolean getIsBeneficialOwner() {
            return isBeneficialOwner;
        }

        public void setIsBeneficialOwner(Boolean isBeneficialOwner) {
            this.isBeneficialOwner = isBeneficialOwner;
        }
    }

    public static class ComplianceData {
        @JsonProperty("riskRating")
        private String riskRating;
        
        @JsonProperty("complianceStatus")
        private String complianceStatus;
        
        @JsonProperty("amlCheck")
        private Boolean amlCheck;
        
        @JsonProperty("sanctionsCheck")
        private Boolean sanctionsCheck;
        
        @JsonProperty("pepCheck")
        private Boolean pepCheck;

        // Getters and Setters
        public String getRiskRating() {
            return riskRating;
        }

        public void setRiskRating(String riskRating) {
            this.riskRating = riskRating;
        }

        public String getComplianceStatus() {
            return complianceStatus;
        }

        public void setComplianceStatus(String complianceStatus) {
            this.complianceStatus = complianceStatus;
        }

        public Boolean getAmlCheck() {
            return amlCheck;
        }

        public void setAmlCheck(Boolean amlCheck) {
            this.amlCheck = amlCheck;
        }

        public Boolean getSanctionsCheck() {
            return sanctionsCheck;
        }

        public void setSanctionsCheck(Boolean sanctionsCheck) {
            this.sanctionsCheck = sanctionsCheck;
        }

        public Boolean getPepCheck() {
            return pepCheck;
        }

        public void setPepCheck(Boolean pepCheck) {
            this.pepCheck = pepCheck;
        }
    }

    public static class KycData {
        @JsonProperty("kycStatus")
        private String kycStatus;
        
        @JsonProperty("kycLevel")
        private String kycLevel;
        
        @JsonProperty("verificationDate")
        private LocalDateTime verificationDate;
        
        @JsonProperty("expiryDate")
        private LocalDateTime expiryDate;
        
        @JsonProperty("documentsVerified")
        private List<String> documentsVerified;

        // Getters and Setters
        public String getKycStatus() {
            return kycStatus;
        }

        public void setKycStatus(String kycStatus) {
            this.kycStatus = kycStatus;
        }

        public String getKycLevel() {
            return kycLevel;
        }

        public void setKycLevel(String kycLevel) {
            this.kycLevel = kycLevel;
        }

        public LocalDateTime getVerificationDate() {
            return verificationDate;
        }

        public void setVerificationDate(LocalDateTime verificationDate) {
            this.verificationDate = verificationDate;
        }

        public LocalDateTime getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDateTime expiryDate) {
            this.expiryDate = expiryDate;
        }

        public List<String> getDocumentsVerified() {
            return documentsVerified;
        }

        public void setDocumentsVerified(List<String> documentsVerified) {
            this.documentsVerified = documentsVerified;
        }
    }
}
