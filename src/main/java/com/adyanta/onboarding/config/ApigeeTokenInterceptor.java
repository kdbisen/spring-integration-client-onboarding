package com.adyanta.onboarding.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Apigee Token Interceptor
 * Automatically adds OAuth2 token to outgoing requests
 */
public class ApigeeTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApigeeTokenInterceptor.class);

    private final String clientId;
    private final String clientSecret;
    private final ApigeeTokenService tokenService;

    public ApigeeTokenInterceptor(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenService = new ApigeeTokenService("https://your-apigee-instance.apigee.net", clientId, clientSecret);
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        try {
            // Get valid access token
            String accessToken = tokenService.getValidAccessToken();
            
            // Add Authorization header
            request.getHeaders().setBearerAuth(accessToken);
            
            // Add additional headers for Apigee
            request.getHeaders().set("X-Client-ID", clientId);
            request.getHeaders().set("X-Request-Source", "client-onboarding-service");
            
            logger.debug("Added Apigee token to request: {}", request.getURI());
            
        } catch (Exception e) {
            logger.error("Error adding Apigee token to request", e);
            // Continue without token - the service will handle authentication failure
        }

        return execution.execute(request, body);
    }
}
