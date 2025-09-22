package com.adyanta.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * JWT Token model for caching
 */
public class JwtToken {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("scope")
    private String scope;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    // Additional fields for caching
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime cacheExpiresAt;
    private String serviceName;
    private boolean isRenewing;
    
    // Constructors
    public JwtToken() {
        this.issuedAt = LocalDateTime.now();
    }
    
    public JwtToken(String accessToken, String tokenType, Long expiresIn) {
        this();
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
        // Cache expires 20 seconds before actual expiration
        this.cacheExpiresAt = this.expiresAt.minusSeconds(20);
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        this.expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
        this.cacheExpiresAt = this.expiresAt.minusSeconds(20);
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        this.cacheExpiresAt = expiresAt.minusSeconds(20);
    }
    
    public LocalDateTime getCacheExpiresAt() {
        return cacheExpiresAt;
    }
    
    public void setCacheExpiresAt(LocalDateTime cacheExpiresAt) {
        this.cacheExpiresAt = cacheExpiresAt;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public boolean isRenewing() {
        return isRenewing;
    }
    
    public void setRenewing(boolean renewing) {
        isRenewing = renewing;
    }
    
    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isCacheExpired() {
        return LocalDateTime.now().isAfter(cacheExpiresAt);
    }
    
    public boolean needsRenewal() {
        return isCacheExpired() && !isRenewing;
    }
    
    public long getSecondsUntilExpiry() {
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }
    
    public long getSecondsUntilCacheExpiry() {
        return java.time.Duration.between(LocalDateTime.now(), cacheExpiresAt).getSeconds();
    }
    
    public String getBearerToken() {
        return tokenType != null ? tokenType + " " + accessToken : accessToken;
    }
    
    @Override
    public String toString() {
        return "JwtToken{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", scope='" + scope + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                ", cacheExpiresAt=" + cacheExpiresAt +
                ", serviceName='" + serviceName + '\'' +
                ", isRenewing=" + isRenewing +
                '}';
    }
}
