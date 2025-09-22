# 🔐 JKS Certificate Quick Reference

## 🎯 **Quick Setup for JKS Files**

### **1. 📁 Prepare JKS Files**
```bash
# Create directory and copy JKS files
mkdir -p mongodb-jks
cp /path/to/your/keystore.jks mongodb-jks/
cp /path/to/your/truststore.jks mongodb-jks/
```

### **2. 🚀 Create Kubernetes Secrets**
```bash
# Run the setup script
./scripts/setup-mongodb-jks.sh

# Or manually create secrets
kubectl create secret generic mongodb-keystore-secret \
  --from-file=keystore.jks=./mongodb-jks/keystore.jks \
  --from-file=truststore.jks=./mongodb-jks/truststore.jks \
  --from-literal=keystore-password=your-keystore-password \
  --from-literal=truststore-password=your-truststore-password
```

### **3. 🔧 Deploy with Helm**
```bash
# Deploy with MongoDB JKS support
helm install client-onboarding ./helm/client-onboarding \
  --namespace default \
  --set mongodb.enabled=true
```

## 📋 **JKS File Types**

| File | Purpose | Contains |
|------|---------|----------|
| **keystore.jks** | Client Authentication | Client certificates, private keys |
| **truststore.jks** | Server Validation | CA certificates, trusted certificates |

## 🔐 **Password Management**

### **Environment Variables**
```yaml
env:
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
```

### **Volume Mounts**
```yaml
volumeMounts:
  - name: mongodb-keystore-volume
    mountPath: /app/keystore
    readOnly: true

volumes:
  - name: mongodb-keystore-volume
    secret:
      secretName: mongodb-keystore-secret
      defaultMode: 0400
```

## 🔍 **Verification Commands**

### **Check JKS Files in Pod**
```bash
# List mounted files
kubectl exec -it <pod-name> -- ls -la /app/keystore/

# Verify keystore
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/keystore.jks -storepass $MONGODB_KEYSTORE_PASSWORD

# Verify truststore
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/truststore.jks -storepass $MONGODB_TRUSTSTORE_PASSWORD
```

### **Check Secrets**
```bash
# List secrets
kubectl get secrets | grep mongodb

# Describe secret
kubectl describe secret mongodb-keystore-secret

# Check secret data
kubectl get secret mongodb-keystore-secret -o yaml
```

## 🚨 **Common Issues & Solutions**

### **Issue: JKS File Not Found**
```bash
# Check if files are mounted
kubectl exec -it <pod-name> -- ls -la /app/keystore/
kubectl exec -it <pod-name> -- file /app/keystore/keystore.jks
```

### **Issue: Wrong Password**
```bash
# Test keystore password
kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/keystore.jks -storepass $MONGODB_KEYSTORE_PASSWORD
```

### **Issue: Permission Denied**
```bash
# Check file permissions
kubectl exec -it <pod-name> -- ls -la /app/keystore/
kubectl exec -it <pod-name> -- chmod 400 /app/keystore/*.jks
```

## 🔄 **JKS Rotation**

### **Update JKS Files**
```bash
# Update secret with new JKS files
kubectl create secret generic mongodb-keystore-secret \
  --from-file=keystore.jks=new-keystore.jks \
  --from-file=truststore.jks=new-truststore.jks \
  --from-literal=keystore-password=new-password \
  --from-literal=truststore-password=new-password \
  --dry-run=client -o yaml | kubectl apply -f -

# Restart pods
kubectl rollout restart deployment/client-onboarding
```

## 📚 **Spring Boot Configuration**

### **application.yml**
```yaml
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

### **Java Configuration**
```java
@Configuration
public class MongoConfig {
    
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
        
        settingsBuilder.applyToSslSettings(sslBuilder -> {
            sslBuilder.enabled(true)
                .invalidHostNameAllowed(true)
                .context(createSSLContext());
        });
        
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

## 🎉 **Summary**

This quick reference provides everything you need to:

1. **Store JKS files** securely in Kubernetes Secrets
2. **Mount JKS files** in application pods
3. **Configure Spring Boot** to use JKS files for MongoDB SSL
4. **Verify** JKS files are working correctly
5. **Rotate** JKS files when needed

The solution follows Kubernetes security best practices and ensures your MongoDB connections are secure with proper SSL/TLS authentication.
