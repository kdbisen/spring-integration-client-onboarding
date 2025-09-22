package com.adyanta.onboarding.exception;

/**
 * Exception for system errors
 */
public class SystemException extends RuntimeException {
    
    private final String errorCode;
    
    public SystemException(String message) {
        super(message);
        this.errorCode = "SYSTEM_ERROR";
    }
    
    public SystemException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SYSTEM_ERROR";
    }
    
    public SystemException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
