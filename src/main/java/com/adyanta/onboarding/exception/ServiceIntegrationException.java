package com.adyanta.onboarding.exception;

/**
 * Exception for service integration errors
 */
public class ServiceIntegrationException extends RuntimeException {
    
    private final String serviceName;
    private final String errorCode;
    
    public ServiceIntegrationException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = "SERVICE_INTEGRATION_ERROR";
    }
    
    public ServiceIntegrationException(String serviceName, String message, String errorCode) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
    }
    
    public ServiceIntegrationException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = "SERVICE_INTEGRATION_ERROR";
    }
    
    public ServiceIntegrationException(String serviceName, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
