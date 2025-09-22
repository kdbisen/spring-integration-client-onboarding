package com.adyanta.onboarding.config;

import com.adyanta.onboarding.service.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * JWT Token Interceptor for automatic token injection
 */
@Component
public class JwtTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenInterceptor.class);

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        URI uri = request.getURI();
        String serviceName = extractServiceName(uri);
        
        if (serviceName != null && !isTokenService(uri)) {
            try {
                String bearerToken = jwtTokenService.getBearerToken(serviceName);
                request.getHeaders().set("Authorization", bearerToken);
                
                logger.debug("Added JWT token to request for service: {} at {}", serviceName, uri);
            } catch (Exception e) {
                logger.warn("Failed to add JWT token for service: {} at {}", serviceName, uri, e);
            }
        }
        
        return execution.execute(request, body);
    }

    /**
     * Extract service name from URI
     */
    private String extractServiceName(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return null;
        }
        
        // Extract service name from hostname
        // Examples: kyc-service -> kyc, data-processor-service -> data-processor
        if (host.contains("-service")) {
            return host.substring(0, host.indexOf("-service"));
        }
        
        // Extract service name from subdomain
        // Examples: kyc.api.company.com -> kyc
        if (host.contains(".")) {
            String[] parts = host.split("\\.");
            if (parts.length > 2) {
                return parts[0];
            }
        }
        
        // Default to hostname
        return host;
    }

    /**
     * Check if this is a request to the token service
     */
    private boolean isTokenService(URI uri) {
        String path = uri.getPath();
        return path != null && (path.contains("/oauth/token") || path.contains("/token"));
    }
}
