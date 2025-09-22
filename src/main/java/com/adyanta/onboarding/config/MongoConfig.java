package com.adyanta.onboarding.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * MongoDB Configuration with JKS SSL Support
 * Configures MongoDB client with SSL using JKS keystore and truststore files
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.adyanta.onboarding.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/client_onboarding}")
    private String mongoUri;

    @Value("${spring.data.mongodb.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.mongodb.ssl.keystore.path:/app/keystore/keystore.jks}")
    private String keystorePath;

    @Value("${spring.data.mongodb.ssl.keystore.password:}")
    private String keystorePassword;

    @Value("${spring.data.mongodb.ssl.truststore.path:/app/keystore/truststore.jks}")
    private String truststorePath;

    @Value("${spring.data.mongodb.ssl.truststore.password:}")
    private String truststorePassword;

    @Value("${spring.data.mongodb.ssl.invalid-hostname-allowed:false}")
    private boolean invalidHostnameAllowed;

    @Override
    protected String getDatabaseName() {
        return "client_onboarding";
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        logger.info("Configuring MongoDB client with SSL: {}", sslEnabled);
        logger.info("MongoDB URI: {}", mongoUri.replaceAll("password=[^&]*", "password=***"));

        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri));

        if (sslEnabled) {
            logger.info("Configuring SSL with JKS files");
            logger.info("Keystore path: {}", keystorePath);
            logger.info("Truststore path: {}", truststorePath);
            
            settingsBuilder.applyToSslSettings(sslBuilder -> {
                sslBuilder.enabled(true)
                        .invalidHostNameAllowed(invalidHostnameAllowed)
                        .context(createSSLContext());
            });
        }

        MongoClientSettings settings = settingsBuilder.build();
        return MongoClients.create(settings);
    }

    /**
     * Creates SSL context using JKS keystore and truststore files
     */
    private SSLContext createSSLContext() {
        try {
            logger.info("Creating SSL context with JKS files");
            
            // Load keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            logger.info("Loading keystore from: {}", keystorePath);
            keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
            logger.info("Keystore loaded successfully");

            // Load truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            logger.info("Loading truststore from: {}", truststorePath);
            trustStore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
            logger.info("Truststore loaded successfully");

            // Create key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            logger.info("Key manager factory initialized");

            // Create trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            logger.info("Trust manager factory initialized");

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), 
                          trustManagerFactory.getTrustManagers(), 
                          new SecureRandom());
            logger.info("SSL context created successfully");

            return sslContext;
        } catch (Exception e) {
            logger.error("Failed to create SSL context with JKS files", e);
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    /**
     * Health check method to verify MongoDB connection
     */
    public boolean isMongoConnectionHealthy() {
        try {
            MongoClient client = mongoClient();
            client.getDatabase(getDatabaseName()).runCommand(org.bson.Document.parse("{ping: 1}"));
            logger.info("MongoDB connection health check passed");
            return true;
        } catch (Exception e) {
            logger.error("MongoDB connection health check failed", e);
            return false;
        }
    }

    // Getters for health indicator
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public String getMongoDatabaseName() {
        return getDatabaseName();
    }
}
