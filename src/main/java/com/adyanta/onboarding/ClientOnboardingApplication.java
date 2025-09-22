package com.adyanta.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for Client Onboarding Service
 * Demonstrates Enterprise Integration Patterns using Spring Integration
 */
@SpringBootApplication
@IntegrationComponentScan
@EnableAsync
public class ClientOnboardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientOnboardingApplication.class, args);
    }
}
