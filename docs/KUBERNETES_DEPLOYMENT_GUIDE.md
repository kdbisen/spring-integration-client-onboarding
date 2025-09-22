# 🚀 Kubernetes/OpenShift Deployment Guide

## 🎯 Overview

This guide provides comprehensive instructions for deploying the Spring Integration Client Onboarding service using Kubernetes ConfigMaps and Helm charts.

## 📋 Prerequisites

### **Required Tools**
```bash
# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Install Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Install Docker (for building images)
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
```

### **Required Access**
- Kubernetes cluster access (minikube, EKS, GKE, AKS, or OpenShift)
- Docker registry access
- Helm repository access

## 🏗️ Architecture Components

### **ConfigMaps**
- **Application Configuration**: Environment-specific settings
- **Logback Configuration**: Logging configuration
- **Nginx Configuration**: Reverse proxy settings

### **Helm Chart Components**
- **Deployment**: Application pods with health checks
- **Service**: Internal service exposure
- **Ingress**: External access with TLS
- **ConfigMap**: Configuration management
- **Secrets**: Sensitive data management
- **HPA**: Horizontal Pod Autoscaling
- **PDB**: Pod Disruption Budget
- **NetworkPolicy**: Security policies

## 🚀 Deployment Methods

### **Method 1: Using ConfigMaps (Direct)**

#### **Step 1: Create ConfigMaps**
```bash
# Apply ConfigMaps
kubectl apply -f k8s/configmap.yaml

# Verify ConfigMaps
kubectl get configmaps
kubectl describe configmap client-onboarding-config
```

#### **Step 2: Create Secrets**
```bash
# Create database secret
kubectl create secret generic client-onboarding-db-secret \
  --from-literal=username=onboarding_user \
  --from-literal=password=onboarding_pass

# Create Fenergo secret
kubectl create secret generic client-onboarding-fenergo-secret \
  --from-literal=clientId=your-fenergo-client-id \
  --from-literal=clientSecret=your-fenergo-client-secret \
  --from-literal=tenantId=your-fenergo-tenant-id

# Create Apigee secret
kubectl create secret generic client-onboarding-apigee-secret \
  --from-literal=clientId=your-apigee-client-id \
  --from-literal=clientSecret=your-apigee-client-secret
```

#### **Step 3: Deploy Application**
```bash
# Create deployment
kubectl create deployment client-onboarding \
  --image=adyanta/client-onboarding:1.0.0 \
  --port=8080

# Create service
kubectl expose deployment client-onboarding \
  --type=ClusterIP \
  --port=8080 \
  --target-port=8080

# Create ingress
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: client-onboarding-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - client-onboarding.adyanta.com
    secretName: client-onboarding-tls
  rules:
  - host: client-onboarding.adyanta.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: client-onboarding
            port:
              number: 8080
EOF
```

### **Method 2: Using Helm Charts (Recommended)**

#### **Step 1: Build and Push Docker Image**
```bash
# Build image
docker build -t adyanta/client-onboarding:1.0.0 .

# Push to registry
docker push adyanta/client-onboarding:1.0.0
```

#### **Step 2: Install Dependencies**
```bash
# Add Helm repositories
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts

# Update repositories
helm repo update
```

#### **Step 3: Deploy with Helm**
```bash
# Install the chart
helm install client-onboarding ./helm/client-onboarding \
  --namespace default \
  --create-namespace \
  --set image.tag=1.0.0 \
  --set ingress.enabled=true \
  --set ingress.hosts[0].host=client-onboarding.adyanta.com \
  --set mysql.enabled=true \
  --set redis.enabled=true \
  --set kafka.enabled=true \
  --set prometheus.enabled=true \
  --set grafana.enabled=true

# Verify deployment
helm list
kubectl get pods
kubectl get services
kubectl get ingress
```

## 🔧 Configuration Management

### **Environment-Specific Values**

#### **Development Environment**
```bash
# Create dev-values.yaml
cat > dev-values.yaml <<EOF
env:
  SPRING_PROFILES_ACTIVE: "dev"
  LOG_LEVEL_ROOT: "DEBUG"
  LOG_LEVEL_APP: "DEBUG"
  DATABASE_URL: "jdbc:mysql://mysql-dev:3306/client_onboarding"
  FENERGO_API_URL: "https://fenergo-dev.adyanta.com/api/v1"
  APIGEE_API_URL: "https://api-dev.adyanta.com"

mysql:
  enabled: true
  auth:
    database: "client_onboarding"
    username: "onboarding_user"
    password: "onboarding_pass"

redis:
  enabled: true
  auth:
    enabled: false

kafka:
  enabled: true
  auth:
    clientProtocol: "PLAINTEXT"
EOF

# Deploy with dev values
helm install client-onboarding-dev ./helm/client-onboarding \
  --namespace dev \
  --create-namespace \
  --values dev-values.yaml
```

#### **Production Environment**
```bash
# Create prod-values.yaml
cat > prod-values.yaml <<EOF
env:
  SPRING_PROFILES_ACTIVE: "prod"
  LOG_LEVEL_ROOT: "INFO"
  LOG_LEVEL_APP: "INFO"
  DATABASE_URL: "jdbc:mysql://mysql-prod:3306/client_onboarding"
  FENERGO_API_URL: "https://fenergo.adyanta.com/api/v1"
  APIGEE_API_URL: "https://api.adyanta.com"

resources:
  limits:
    cpu: 2000m
    memory: 4Gi
  requests:
    cpu: 1000m
    memory: 2Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
  targetCPUUtilizationPercentage: 60
  targetMemoryUtilizationPercentage: 70

mysql:
  enabled: true
  auth:
    database: "client_onboarding"
    username: "onboarding_user"
    password: "onboarding_pass"
  primary:
    persistence:
      enabled: true
      size: 100Gi
    resources:
      limits:
        cpu: 2000m
        memory: 4Gi
      requests:
        cpu: 1000m
        memory: 2Gi

redis:
  enabled: true
  auth:
    enabled: true
    password: "redis-password"
  master:
    persistence:
      enabled: true
      size: 50Gi
    resources:
      limits:
        cpu: 1000m
        memory: 2Gi
      requests:
        cpu: 500m
        memory: 1Gi

kafka:
  enabled: true
  auth:
    clientProtocol: "SASL_SSL"
  controller:
    replicaCount: 3
  brokers: 3
  persistence:
    enabled: true
    size: 100Gi
  resources:
    limits:
      cpu: 2000m
      memory: 4Gi
    requests:
      cpu: 1000m
      memory: 2Gi
EOF

# Deploy with prod values
helm install client-onboarding-prod ./helm/client-onboarding \
  --namespace prod \
  --create-namespace \
  --values prod-values.yaml
```

## 🔐 Security Configuration

### **Secrets Management**

#### **Using Kubernetes Secrets**
```bash
# Create secrets
kubectl create secret generic client-onboarding-db-secret \
  --from-literal=username=onboarding_user \
  --from-literal=password=onboarding_pass

kubectl create secret generic client-onboarding-fenergo-secret \
  --from-literal=clientId=your-fenergo-client-id \
  --from-literal=clientSecret=your-fenergo-client-secret \
  --from-literal=tenantId=your-fenergo-tenant-id

kubectl create secret generic client-onboarding-apigee-secret \
  --from-literal=clientId=your-apigee-client-id \
  --from-literal=clientSecret=your-apigee-client-secret
```

#### **Using External Secret Management**
```bash
# Install External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets-system \
  --create-namespace

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

# Create ExternalSecret
kubectl apply -f - <<EOF
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: client-onboarding-secrets
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-manager
    kind: SecretStore
  target:
    name: client-onboarding-secrets
    creationPolicy: Owner
  data:
  - secretKey: database-password
    remoteRef:
      key: client-onboarding/database
      property: password
  - secretKey: fenergo-client-secret
    remoteRef:
      key: client-onboarding/fenergo
      property: client-secret
EOF
```

### **Network Policies**
```bash
# Apply network policy
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: client-onboarding-netpol
spec:
  podSelector:
    matchLabels:
      app: client-onboarding
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to: []
    ports:
    - protocol: TCP
      port: 3306  # MySQL
    - protocol: TCP
      port: 6379  # Redis
    - protocol: TCP
      port: 9092 # Kafka
    - protocol: TCP
      port: 443  # HTTPS
    - protocol: TCP
      port: 80   # HTTP
EOF
```

## 📊 Monitoring and Observability

### **Prometheus Integration**
```bash
# Install Prometheus
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.enabled=true \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false

# Create ServiceMonitor
kubectl apply -f - <<EOF
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: client-onboarding-monitor
  labels:
    app: client-onboarding
spec:
  selector:
    matchLabels:
      app: client-onboarding
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
EOF
```

### **Grafana Dashboards**
```bash
# Create dashboard configmap
kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: client-onboarding-dashboard
  namespace: monitoring
data:
  dashboard.json: |
    {
      "dashboard": {
        "title": "Client Onboarding Service",
        "panels": [
          {
            "title": "Request Rate",
            "type": "graph",
            "targets": [
              {
                "expr": "rate(http_requests_total[5m])",
                "legendFormat": "{{method}} {{uri}}"
              }
            ]
          },
          {
            "title": "Response Time",
            "type": "graph",
            "targets": [
              {
                "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
                "legendFormat": "95th percentile"
              }
            ]
          }
        ]
      }
    }
EOF
```

## 🔄 CI/CD Integration

### **GitHub Actions Workflow**
```yaml
# .github/workflows/deploy.yml
name: Deploy to Kubernetes

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2
    
    - name: Login to Docker Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ secrets.DOCKER_REGISTRY }}
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v3
      with:
        context: .
        push: true
        tags: |
          ${{ secrets.DOCKER_REGISTRY }}/client-onboarding:${{ github.sha }}
          ${{ secrets.DOCKER_REGISTRY }}/client-onboarding:latest
    
    - name: Install Helm
      uses: azure/setup-helm@v3
      with:
        version: '3.12.0'
    
    - name: Deploy to Kubernetes
      run: |
        helm upgrade --install client-onboarding ./helm/client-onboarding \
          --namespace ${{ env.NAMESPACE }} \
          --create-namespace \
          --set image.tag=${{ github.sha }} \
          --set image.registry=${{ secrets.DOCKER_REGISTRY }}
      env:
        KUBECONFIG: ${{ secrets.KUBECONFIG }}
        NAMESPACE: ${{ github.ref == 'refs/heads/main' && 'prod' || 'dev' }}
```

## 🚨 Troubleshooting

### **Common Issues**

#### **Pod Not Starting**
```bash
# Check pod status
kubectl get pods
kubectl describe pod <pod-name>
kubectl logs <pod-name>

# Check events
kubectl get events --sort-by=.metadata.creationTimestamp
```

#### **Configuration Issues**
```bash
# Check ConfigMap
kubectl get configmap client-onboarding-config -o yaml

# Check secrets
kubectl get secrets
kubectl describe secret client-onboarding-db-secret
```

#### **Service Connectivity**
```bash
# Check service
kubectl get services
kubectl describe service client-onboarding

# Test connectivity
kubectl run test-pod --image=busybox --rm -it --restart=Never -- nslookup client-onboarding
```

#### **Ingress Issues**
```bash
# Check ingress
kubectl get ingress
kubectl describe ingress client-onboarding-ingress

# Check ingress controller
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx <ingress-controller-pod>
```

### **Debug Commands**
```bash
# Port forward for local testing
kubectl port-forward service/client-onboarding 8080:8080

# Access logs
kubectl logs -f deployment/client-onboarding

# Execute commands in pod
kubectl exec -it <pod-name> -- /bin/bash

# Check resource usage
kubectl top pods
kubectl top nodes
```

## 📚 Additional Resources

- **Kubernetes Documentation**: https://kubernetes.io/docs/
- **Helm Documentation**: https://helm.sh/docs/
- **OpenShift Documentation**: https://docs.openshift.com/
- **ConfigMap Best Practices**: https://kubernetes.io/docs/concepts/configuration/configmap/
- **Secrets Management**: https://kubernetes.io/docs/concepts/configuration/secret/

## 🎉 Conclusion

This deployment guide provides comprehensive instructions for deploying the Spring Integration Client Onboarding service using Kubernetes ConfigMaps and Helm charts. The solution includes:

- **Environment-specific configuration** management
- **Security** with secrets and network policies
- **Monitoring** with Prometheus and Grafana
- **Scalability** with horizontal pod autoscaling
- **High availability** with pod disruption budgets
- **CI/CD integration** with GitHub Actions

The deployment is production-ready and follows Kubernetes best practices for security, monitoring, and scalability.
