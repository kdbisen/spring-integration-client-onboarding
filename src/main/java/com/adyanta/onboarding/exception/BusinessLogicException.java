package com.adyanta.onboarding.exception;

/**
 * Exception for business logic errors
 */
public class BusinessLogicException extends RuntimeException {
    
    private final String errorCode;
    
    public BusinessLogicException(String message) {
        super(message);
        this.errorCode = "BUSINESS_LOGIC_ERROR";
    }
    
    public BusinessLogicException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_LOGIC_ERROR";
    }
    
    public BusinessLogicException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
