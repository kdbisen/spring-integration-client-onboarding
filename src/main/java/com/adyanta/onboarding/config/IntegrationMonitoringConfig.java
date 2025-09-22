package com.adyanta.onboarding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.monitor.IntegrationMBeanExporter;
import org.springframework.integration.support.management.IntegrationManagementConfigurer;

/**
 * Integration Monitoring Configuration
 * Enables monitoring and management of Spring Integration components
 */
@Configuration
@EnableIntegration
@EnableIntegrationManagement
public class IntegrationMonitoringConfig {

    /**
     * Enable JMX monitoring for integration components
     */
    @Bean
    public IntegrationMBeanExporter integrationMBeanExporter() {
        IntegrationMBeanExporter exporter = new IntegrationMBeanExporter();
        exporter.setDefaultDomain("com.adyanta.onboarding");
        exporter.setServer(null); // Use default MBean server
        return exporter;
    }

    /**
     * Configure integration management
     */
    @Bean
    public IntegrationManagementConfigurer integrationManagementConfigurer() {
        IntegrationManagementConfigurer configurer = new IntegrationManagementConfigurer();
        configurer.setDefaultLoggingEnabled(true);
        configurer.setDefaultMetricsEnabled(true);
        configurer.setDefaultCountsEnabled(true);
        configurer.setDefaultStatsEnabled(true);
        return configurer;
    }
}
