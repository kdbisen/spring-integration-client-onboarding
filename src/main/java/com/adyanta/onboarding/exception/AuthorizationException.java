package com.adyanta.onboarding.exception;

/**
 * Exception for authorization errors
 */
public class AuthorizationException extends RuntimeException {
    
    private final String errorCode;
    
    public AuthorizationException(String message) {
        super(message);
        this.errorCode = "AUTHORIZATION_ERROR";
    }
    
    public AuthorizationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTHORIZATION_ERROR";
    }
    
    public AuthorizationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
