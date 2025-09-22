# 🔐 JKS Certificate Management Guide

## 🎯 Overview

This guide explains how to properly manage JKS (Java KeyStore) files and their passwords for MongoDB SSL connections in Kubernetes/OpenShift.

## 📋 JKS File Types

### **1. Keystore.jks**
- Contains **client certificates** and **private keys**
- Used for **client authentication** (mutual TLS)
- Password required for access

### **2. Truststore.jks**
- Contains **CA certificates** and **trusted certificates**
- Used for **server certificate validation**
- Password required for access

## 🚀 Step-by-Step Implementation

### **Step 1: Prepare JKS Files**

```bash
# Create directory for JKS files
mkdir -p mongodb-jks
cd mongodb-jks

# Copy your JKS files here
cp /path/to/your/keystore.jks .
cp /path/to/your/truststore.jks .

# Verify JKS files (you'll need passwords)
keytool -list -keystore keystore.jks
keytool -list -keystore truststore.jks
```

### **Step 2: Create Kubernetes Secrets**

#### **Method 1: Using kubectl (Direct)**
```bash
# Create keystore secret with JKS files
kubectl create secret generic mongodb-keystore-secret \
  --from-file=keystore.jks=./keystore.jks \
  --from-file=truststore.jks=./truststore.jks \
  --from-literal=keystore-password=your-keystore-password \
  --from-literal=truststore-password=your-truststore-password

# Verify secret creation
kubectl get secret mongodb-keystore-secret -o yaml
```

#### **Method 2: Using YAML File**
```yaml
# mongodb-jks-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: mongodb-keystore-secret
  namespace: default
type: Opaque
data:
  # Base64 encoded JKS files
  keystore.jks: <base64-encoded-keystore-jks>
  truststore.jks: <base64-encoded-truststore-jks>
  # Base64 encoded passwords
  keystore-password: <base64-encoded-keystore-password>
  truststore-password: <base64-encoded-truststore-password>
```

### **Step 3: Encode JKS Files to Base64**

```bash
# Encode JKS files to base64
base64 -i keystore.jks | tr -d '\n' > keystore.jks.b64
base64 -i truststore.jks | tr -d '\n' > truststore.jks.b64

# Encode passwords to base64
echo -n "your-keystore-password" | base64
echo -n "your-truststore-password" | base64

# Create secret with base64 encoded data
kubectl apply -f mongodb-jks-secret.yaml
```

### **Step 4: Mount JKS Files in Pod**

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: client-onboarding
spec:
  template:
    spec:
      containers:
      - name: client-onboarding
        image: adyanta/client-onboarding:1.0.0
        env:
        # JKS passwords as environment variables
        - name: MONGODB_KEYSTORE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mongodb-keystore-secret
              key: keystore-password
        - name: MONGODB_TRUSTSTORE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mongodb-keystore-secret
              key: truststore-password
        volumeMounts:
        # Mount JKS files
        - name: mongodb-keystore-volume
          mountPath: /app/keystore
          readOnly: true
      volumes:
      - name: mongodb-keystore-volume
        secret:
          secretName: mongodb-keystore-secret
          defaultMode: 0400  # Read-only for owner
```

### **Step 5: Spring Boot Configuration**

```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true
      ssl:
        enabled: true
        keystore:
          path: /app/keystore/keystore.jks
          password: ${MONGODB_KEYSTORE_PASSWORD}
        truststore:
          path: /app/keystore/truststore.jks
          password: ${MONGODB_TRUSTSTORE_PASSWORD}
```

### **Step 6: Java Configuration for JKS**

```java
@Configuration
@EnableMongoRepositories
public class MongoConfig {
    
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
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
        
        // Configure SSL with JKS files
        settingsBuilder.applyToSslSettings(sslBuilder -> {
            sslBuilder.enabled(true)
                .invalidHostNameAllowed(true)
                .context(createSSLContext());
        });
        
        return MongoClients.create(settingsBuilder.build());
    }
    
    private SSLContext createSSLContext() {
        try {
            // Load keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
            
            // Load truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
            
            // Create key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            
            // Create trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            
            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), 
                          trustManagerFactory.getTrustManagers(), 
                          new SecureRandom());
            
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context with JKS files", e);
        }
    }
}
```

## 🔧 Practical Example

### **Complete Setup Script**

```bash
#!/bin/bash
# setup-mongodb-jks.sh

set -e

# Configuration
NAMESPACE="default"
SECRET_NAME="mongodb-keystore-secret"
JKS_DIR="./mongodb-jks"

echo "Setting up MongoDB JKS certificates..."

# Check if JKS files exist
if [ ! -f "$JKS_DIR/keystore.jks" ]; then
    echo "Error: keystore.jks not found in $JKS_DIR"
    exit 1
fi

if [ ! -f "$JKS_DIR/truststore.jks" ]; then
    echo "Error: truststore.jks not found in $JKS_DIR"
    exit 1
fi

# Get passwords
read -s -p "Enter keystore password: " KEYSTORE_PASSWORD
echo ""
read -s -p "Enter truststore password: " TRUSTSTORE_PASSWORD
echo ""

# Create secret
kubectl create secret generic $SECRET_NAME \
  --namespace=$NAMESPACE \
  --from-file=keystore.jks="$JKS_DIR/keystore.jks" \
  --from-file=truststore.jks="$JKS_DIR/truststore.jks" \
  --from-literal=keystore-password="$KEYSTORE_PASSWORD" \
  --from-literal=truststore-password="$TRUSTSTORE_PASSWORD"

echo "Secret created: $SECRET_NAME"

# Verify secret
kubectl get secret $SECRET_NAME -n $NAMESPACE
```

### **Helm Values for JKS**

```yaml
# values.yaml
mongodb:
  enabled: true
  ssl:
    enabled: true
    keystore:
      path: /app/keystore/keystore.jks
      password: ${MONGODB_KEYSTORE_PASSWORD}
    truststore:
      path: /app/keystore/truststore.jks
      password: ${MONGODB_TRUSTSTORE_PASSWORD}

secrets:
  mongodb:
    keystore:
      name: "mongodb-keystore-secret"
```

## 🔍 Verification

### **Check JKS Files in Pod**

```bash
# List mounted JKS files
kubectl exec -it <pod-name> -- ls -la /app/keystore/

# Verify JKS file integrity
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/keystore.jks -storepass $MONGODB_KEYSTORE_PASSWORD

# Check truststore
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/truststore.jks -storepass $MONGODB_TRUSTSTORE_PASSWORD
```

### **Test MongoDB Connection**

```bash
# Test SSL connection
kubectl exec -it <pod-name> -- java -cp /app/lib/* com.adyanta.onboarding.MongoConnectionTest

# Check application logs
kubectl logs <pod-name> | grep -i mongodb
```

## 🚨 Troubleshooting

### **Common JKS Issues**

#### **1. JKS File Not Found**
```bash
# Check if files are mounted
kubectl exec -it <pod-name> -- ls -la /app/keystore/
kubectl exec -it <pod-name> -- file /app/keystore/keystore.jks
```

#### **2. Wrong Password**
```bash
# Test keystore password
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/keystore.jks -storepass $MONGODB_KEYSTORE_PASSWORD
```

#### **3. Permission Issues**
```bash
# Check file permissions
kubectl exec -it <pod-name> -- ls -la /app/keystore/
kubectl exec -it <pod-name> -- chmod 400 /app/keystore/*.jks
```

#### **4. SSL Handshake Failed**
```bash
# Check certificate validity
kubectl exec -it <pod-name> -- keytool -list -v -keystore /app/keystore/keystore.jks -storepass $MONGODB_KEYSTORE_PASSWORD
```

## 🔄 JKS Rotation

### **Update JKS Files**

```bash
# Update secret with new JKS files
kubectl create secret generic mongodb-keystore-secret \
  --from-file=keystore.jks=new-keystore.jks \
  --from-file=truststore.jks=new-truststore.jks \
  --from-literal=keystore-password=new-password \
  --from-literal=truststore-password=new-password \
  --dry-run=client -o yaml | kubectl apply -f -

# Restart pods to pick up new JKS files
kubectl rollout restart deployment/client-onboarding
```

## 📚 Best Practices

### **Security**
1. **Never store JKS passwords in ConfigMaps**
2. **Use Kubernetes Secrets for passwords**
3. **Set proper file permissions (0400)**
4. **Rotate JKS files regularly**

### **Management**
1. **Keep JKS files in version control (encrypted)**
2. **Document JKS purposes and passwords**
3. **Use different JKS files per environment**
4. **Monitor JKS expiration dates**

### **Development**
1. **Use local JKS files for development**
2. **Test JKS files before deployment**
3. **Include JKS validation in CI/CD**
4. **Automate JKS rotation**

## 🎉 Conclusion

This guide provides comprehensive instructions for managing JKS files and their passwords in Kubernetes/OpenShift. The solution ensures:

- **Secure storage** of JKS files and passwords
- **Proper mounting** in application pods
- **Spring Boot integration** with SSL configuration
- **Troubleshooting guides** for common issues
- **Best practices** for JKS management

The implementation follows Kubernetes security best practices while maintaining operational flexibility.
