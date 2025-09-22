package com.adyanta.onboarding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;

import java.time.Duration;

/**
 * Apigee Configuration
 * Configures RestTemplate and HTTP clients for Apigee integration
 */
@Configuration
public class ApigeeConfig {

    @Value("${apigee.base.url:https://your-apigee-instance.apigee.net}")
    private String apigeeBaseUrl;

    @Value("${apigee.client.id:your-client-id}")
    private String apigeeClientId;

    @Value("${apigee.client.secret:your-client-secret}")
    private String apigeeClientSecret;

    @Value("${apigee.timeout:30000}")
    private int apigeeTimeout;

    @Bean
    public RestTemplate restTemplate() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(apigeeTimeout))
                        .setResponseTimeout(Timeout.ofMilliseconds(apigeeTimeout))
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofMillis(apigeeTimeout));

        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add Apigee interceptor for automatic token management
        restTemplate.getInterceptors().add(new ApigeeTokenInterceptor(apigeeClientId, apigeeClientSecret));
        
        return restTemplate;
    }

    @Bean
    public ApigeeTokenService apigeeTokenService() {
        return new ApigeeTokenService(apigeeBaseUrl, apigeeClientId, apigeeClientSecret);
    }

    @Bean
    public RestTemplate jwtRestTemplate() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(apigeeTimeout))
                        .setResponseTimeout(Timeout.ofMilliseconds(apigeeTimeout))
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofMillis(apigeeTimeout));

        RestTemplate restTemplate = new RestTemplate(factory);
        
        return restTemplate;
    }
}
