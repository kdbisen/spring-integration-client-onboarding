# 🔐 MongoDB Certificate Management Guide

## 🎯 Overview

This guide explains how to manage MongoDB keystore certificates in Kubernetes/OpenShift for secure MongoDB connections in the Spring Integration Client Onboarding service.

## 📋 Certificate Types

### **1. Keystore Certificates**
- **keystore.jks**: Java KeyStore containing client certificates
- **keystore.p12**: PKCS12 format keystore
- **truststore.jks**: TrustStore containing CA certificates
- **Passwords**: Keystore and truststore passwords

### **2. SSL Certificates**
- **mongodb-cert.pem**: MongoDB server certificate
- **mongodb-key.pem**: MongoDB private key
- **ca-cert.pem**: Certificate Authority certificate

## 🚀 Implementation Methods

### **Method 1: Direct Kubernetes Secrets (Recommended for Development)**

#### **Step 1: Prepare Certificates**
```bash
# Create directory for certificates
mkdir -p mongodb-certs
cd mongodb-certs

# Copy your certificates here
cp /path/to/your/keystore.jks .
cp /path/to/your/truststore.jks .
cp /path/to/your/mongodb-cert.pem .
cp /path/to/your/mongodb-key.pem .
cp /path/to/your/ca-cert.pem .
```

#### **Step 2: Encode Certificates**
```bash
# Encode keystore files to base64
base64 -i keystore.jks | tr -d '\n' > keystore.jks.b64
base64 -i truststore.jks | tr -d '\n' > truststore.jks.b64
base64 -i mongodb-cert.pem | tr -d '\n' > mongodb-cert.pem.b64
base64 -i mongodb-key.pem | tr -d '\n' > mongodb-key.pem.b64
base64 -i ca-cert.pem | tr -d '\n' > ca-cert.pem.b64

# Encode passwords
echo -n "your-keystore-password" | base64
echo -n "your-truststore-password" | base64
echo -n "your-mongodb-password" | base64
```

#### **Step 3: Create Secrets**
```bash
# Create keystore secret
kubectl create secret generic mongodb-keystore-secret \
  --from-file=keystore.jks=keystore.jks \
  --from-file=truststore.jks=truststore.jks \
  --from-literal=keystore-password=your-keystore-password \
  --from-literal=truststore-password=your-truststore-password

# Create SSL secret
kubectl create secret generic mongodb-ssl-secret \
  --from-file=mongodb-cert.pem=mongodb-cert.pem \
  --from-file=mongodb-key.pem=mongodb-key.pem \
  --from-file=ca-cert.pem=ca-cert.pem \
  --from-literal=mongodb-password=your-mongodb-password

# Create connection secret
kubectl create secret generic mongodb-connection-secret \
  --from-literal=mongodb-uri="mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true" \
  --from-literal=mongodb-database="client_onboarding" \
  --from-literal=mongodb-username="onboarding_user" \
  --from-literal=mongodb-password="onboarding_pass" \
  --from-literal=mongodb-auth-database="admin" \
  --from-literal=mongodb-ssl-enabled="true" \
  --from-literal=mongodb-ssl-invalid-hostname-allowed="true"
```

### **Method 2: Using Helm Values (Recommended for Production)**

#### **Step 1: Create Certificate Values File**
```bash
# Create mongodb-values.yaml
cat > mongodb-values.yaml <<EOF
mongodb:
  enabled: true
  keystore:
    jks: |
      # Paste base64 encoded keystore.jks content here
    p12: |
      # Paste base64 encoded keystore.p12 content here
    truststore: |
      # Paste base64 encoded truststore.jks content here
    password: "your-keystore-password"
    truststorePassword: "your-truststore-password"
  ssl:
    cert: |
      # Paste base64 encoded mongodb-cert.pem content here
    key: |
      # Paste base64 encoded mongodb-key.pem content here
    ca: |
      # Paste base64 encoded ca-cert.pem content here
  connection:
    uri: "mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true"
  auth:
    database: "client_onboarding"
    username: "onboarding_user"
    password: "onboarding_pass"
    authDatabase: "admin"
EOF
```

#### **Step 2: Deploy with Helm**
```bash
# Deploy with MongoDB certificates
helm install client-onboarding ./helm/client-onboarding \
  --namespace default \
  --create-namespace \
  --values mongodb-values.yaml \
  --set mongodb.enabled=true
```

### **Method 3: Using External Secret Management**

#### **Step 1: Install External Secrets Operator**
```bash
# Install External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets-system \
  --create-namespace
```

#### **Step 2: Create SecretStore**
```bash
# Create SecretStore for AWS Secrets Manager
kubectl apply -f - <<EOF
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: aws-secrets-manager
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-west-2
      auth:
        secretRef:
          accessKeyID:
            name: aws-credentials
            key: access-key-id
          secretAccessKey:
            name: aws-credentials
            key: secret-access-key
EOF
```

#### **Step 3: Create ExternalSecret**
```bash
# Create ExternalSecret for MongoDB certificates
kubectl apply -f - <<EOF
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: mongodb-certificates
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-manager
    kind: SecretStore
  target:
    name: mongodb-keystore-secret
    creationPolicy: Owner
  data:
  - secretKey: keystore.jks
    remoteRef:
      key: mongodb/keystore
      property: jks
  - secretKey: truststore.jks
    remoteRef:
      key: mongodb/truststore
      property: jks
  - secretKey: keystore-password
    remoteRef:
      key: mongodb/passwords
      property: keystore-password
EOF
```

## 🔧 Spring Boot Configuration

### **Application Properties**
```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      database: ${MONGODB_DATABASE}
      username: ${MONGODB_USERNAME}
      password: ${MONGODB_PASSWORD}
      ssl:
        enabled: ${MONGODB_SSL_ENABLED:false}
        invalid-hostname-allowed: ${MONGODB_SSL_INVALID_HOSTNAME_ALLOWED:false}
        keystore:
          path: /app/keystore/keystore.jks
          password: ${MONGODB_KEYSTORE_PASSWORD}
        truststore:
          path: /app/keystore/truststore.jks
          password: ${MONGODB_TRUSTSTORE_PASSWORD}
        certificate:
          path: /app/ssl/mongodb-cert.pem
        key:
          path: /app/ssl/mongodb-key.pem
        ca-certificate:
          path: /app/ssl/ca-cert.pem
```

### **Java Configuration**
```java
@Configuration
@EnableMongoRepositories
public class MongoConfig {
    
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
    @Value("${spring.data.mongodb.ssl.enabled:false}")
    private boolean sslEnabled;
    
    @Value("${spring.data.mongodb.ssl.keystore.path}")
    private String keystorePath;
    
    @Value("${spring.data.mongodb.ssl.keystore.password}")
    private String keystorePassword;
    
    @Value("${spring.data.mongodb.ssl.truststore.path}")
    private String truststorePath;
    
    @Value("${spring.data.mongodb.ssl.truststore.password}")
    private String truststorePassword;
    
    @Bean
    public MongoClient mongoClient() {
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoUri));
        
        if (sslEnabled) {
            // Configure SSL with keystore and truststore
            settingsBuilder.applyToSslSettings(sslBuilder -> {
                sslBuilder.enabled(true)
                    .invalidHostNameAllowed(true)
                    .context(createSSLContext());
            });
        }
        
        return MongoClients.create(settingsBuilder.build());
    }
    
    private SSLContext createSSLContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
            
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), 
                          trustManagerFactory.getTrustManagers(), 
                          new SecureRandom());
            
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }
}
```

## 🔍 Verification and Testing

### **Check Secrets**
```bash
# List all MongoDB secrets
kubectl get secrets | grep mongodb

# Describe keystore secret
kubectl describe secret mongodb-keystore-secret

# Check secret contents (base64 encoded)
kubectl get secret mongodb-keystore-secret -o yaml
```

### **Check Pod Mounts**
```bash
# Check if certificates are mounted
kubectl exec -it <pod-name> -- ls -la /app/keystore/
kubectl exec -it <pod-name> -- ls -la /app/ssl/

# Verify certificate files
kubectl exec -it <pod-name> -- file /app/keystore/keystore.jks
kubectl exec -it <pod-name> -- file /app/ssl/mongodb-cert.pem
```

### **Test MongoDB Connection**
```bash
# Test connection from pod
kubectl exec -it <pod-name> -- java -cp /app/lib/* com.adyanta.onboarding.MongoConnectionTest

# Check application logs
kubectl logs <pod-name> | grep -i mongodb
```

## 🚨 Troubleshooting

### **Common Issues**

#### **Certificate Not Found**
```bash
# Check if secret exists
kubectl get secret mongodb-keystore-secret

# Check if files are mounted
kubectl exec -it <pod-name> -- ls -la /app/keystore/
```

#### **SSL Handshake Failed**
```bash
# Check certificate validity
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/keystore.jks

# Check truststore
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/truststore.jks
```

#### **Permission Denied**
```bash
# Check file permissions
kubectl exec -it <pod-name> -- ls -la /app/keystore/
kubectl exec -it <pod-name> -- ls -la /app/ssl/

# Fix permissions if needed
kubectl exec -it <pod-name> -- chmod 400 /app/keystore/*.jks
kubectl exec -it <pod-name> -- chmod 400 /app/ssl/*.pem
```

### **Debug Commands**
```bash
# Check environment variables
kubectl exec -it <pod-name> -- env | grep MONGODB

# Check application configuration
kubectl exec -it <pod-name> -- cat /app/config/application.yml

# Test SSL connection
kubectl exec -it <pod-name> -- openssl s_client -connect mongodb-service:27017 -cert /app/ssl/mongodb-cert.pem -key /app/ssl/mongodb-key.pem
```

## 🔄 Certificate Rotation

### **Automated Rotation**
```bash
# Create certificate rotation script
cat > rotate-certificates.sh <<EOF
#!/bin/bash
# Download new certificates
aws s3 cp s3://certificates-bucket/mongodb/keystore.jks /tmp/keystore.jks
aws s3 cp s3://certificates-bucket/mongodb/truststore.jks /tmp/truststore.jks

# Update secrets
kubectl create secret generic mongodb-keystore-secret \
  --from-file=keystore.jks=/tmp/keystore.jks \
  --from-file=truststore.jks=/tmp/truststore.jks \
  --from-literal=keystore-password=new-password \
  --from-literal=truststore-password=new-password \
  --dry-run=client -o yaml | kubectl apply -f -

# Restart pods to pick up new certificates
kubectl rollout restart deployment/client-onboarding
EOF

chmod +x rotate-certificates.sh
```

### **Manual Rotation**
```bash
# Update secret with new certificates
kubectl create secret generic mongodb-keystore-secret \
  --from-file=keystore.jks=new-keystore.jks \
  --from-file=truststore.jks=new-truststore.jks \
  --from-literal=keystore-password=new-password \
  --from-literal=truststore-password=new-password \
  --dry-run=client -o yaml | kubectl apply -f -

# Restart deployment
kubectl rollout restart deployment/client-onboarding

# Check rollout status
kubectl rollout status deployment/client-onboarding
```

## 📚 Best Practices

### **Security**
1. **Use Kubernetes Secrets**: Never store certificates in ConfigMaps
2. **Set Proper Permissions**: Use `defaultMode: 0400` for certificate files
3. **Rotate Regularly**: Implement automated certificate rotation
4. **Use External Secrets**: For production, use external secret management

### **Management**
1. **Version Control**: Keep certificate templates in version control
2. **Documentation**: Document certificate purposes and rotation procedures
3. **Monitoring**: Monitor certificate expiration dates
4. **Backup**: Keep secure backups of certificates

### **Development**
1. **Local Testing**: Use local keystores for development
2. **Environment Separation**: Use different certificates per environment
3. **CI/CD Integration**: Automate certificate deployment
4. **Testing**: Include certificate validation in tests

## 🎉 Conclusion

This guide provides comprehensive instructions for managing MongoDB keystore certificates in Kubernetes/OpenShift. The solution includes:

- **Multiple deployment methods** (direct secrets, Helm values, external secrets)
- **Spring Boot integration** with SSL configuration
- **Security best practices** for certificate management
- **Troubleshooting guides** for common issues
- **Certificate rotation** procedures

The implementation ensures secure MongoDB connections while maintaining operational flexibility and following Kubernetes best practices.
