#!/bin/bash

# MongoDB JKS Certificate Setup Script
# This script helps set up JKS files and their passwords for MongoDB SSL connections

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
NAMESPACE="default"
SECRET_NAME="mongodb-keystore-secret"
JKS_DIR="./mongodb-jks"

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAMESPACE    Kubernetes namespace (default: default)"
    echo "  -d, --jks-dir DIR           JKS files directory (default: ./mongodb-jks)"
    echo "  -s, --secret-name NAME      Secret name (default: mongodb-keystore-secret)"
    echo "  -h, --help                  Show this help message"
    echo ""
    echo "Example:"
    echo "  $0 -n production -d /path/to/jks"
    echo ""
    echo "Required JKS files in the directory:"
    echo "  - keystore.jks (client certificates and private keys)"
    echo "  - truststore.jks (CA certificates and trusted certificates)"
    echo ""
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    # Check if kubectl can connect to cluster
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    # Check if JKS directory exists
    if [ ! -d "$JKS_DIR" ]; then
        print_error "JKS directory '$JKS_DIR' does not exist"
        print_info "Please create the directory and place your JKS files there:"
        print_info "  mkdir -p $JKS_DIR"
        print_info "  cp /path/to/your/keystore.jks $JKS_DIR/"
        print_info "  cp /path/to/your/truststore.jks $JKS_DIR/"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Function to validate JKS files
validate_jks_files() {
    print_info "Validating JKS files..."
    
    local missing_files=()
    
    # Check for required JKS files
    local required_files=(
        "keystore.jks"
        "truststore.jks"
    )
    
    for file in "${required_files[@]}"; do
        if [ ! -f "$JKS_DIR/$file" ]; then
            missing_files+=("$file")
        fi
    done
    
    if [ ${#missing_files[@]} -gt 0 ]; then
        print_error "Missing required JKS files:"
        for file in "${missing_files[@]}"; do
            echo "  - $file"
        done
        print_info "Please place the missing JKS files in: $JKS_DIR"
        exit 1
    fi
    
    # Check if files are valid JKS files
    for file in "${required_files[@]}"; do
        if ! file "$JKS_DIR/$file" | grep -q "Java KeyStore"; then
            print_warning "$file may not be a valid JKS file"
        fi
    done
    
    print_success "JKS files validation passed"
}

# Function to get JKS passwords
get_jks_passwords() {
    print_info "Please provide the JKS passwords:"
    echo ""
    
    read -s -p "Keystore password: " KEYSTORE_PASSWORD
    echo ""
    
    read -s -p "Truststore password: " TRUSTSTORE_PASSWORD
    echo ""
    
    # Verify passwords by testing JKS files
    print_info "Verifying JKS passwords..."
    
    if ! keytool -list -keystore "$JKS_DIR/keystore.jks" -storepass "$KEYSTORE_PASSWORD" &> /dev/null; then
        print_error "Invalid keystore password"
        exit 1
    fi
    
    if ! keytool -list -keystore "$JKS_DIR/truststore.jks" -storepass "$TRUSTSTORE_PASSWORD" &> /dev/null; then
        print_error "Invalid truststore password"
        exit 1
    fi
    
    print_success "JKS passwords verified"
}

# Function to get MongoDB connection details
get_mongodb_details() {
    print_info "Please provide MongoDB connection details:"
    echo ""
    
    read -p "MongoDB URI (default: mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true): " MONGODB_URI
    MONGODB_URI=${MONGODB_URI:-"mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true"}
    
    read -p "MongoDB database (default: client_onboarding): " MONGODB_DATABASE
    MONGODB_DATABASE=${MONGODB_DATABASE:-"client_onboarding"}
    
    read -p "MongoDB username (default: onboarding_user): " MONGODB_USERNAME
    MONGODB_USERNAME=${MONGODB_USERNAME:-"onboarding_user"}
    
    read -s -p "MongoDB password: " MONGODB_PASSWORD
    echo ""
}

# Function to create JKS secret
create_jks_secret() {
    print_info "Creating JKS secret..."
    
    # Check if secret already exists
    if kubectl get secret "$SECRET_NAME" --namespace="$NAMESPACE" &> /dev/null; then
        print_warning "Secret '$SECRET_NAME' already exists"
        read -p "Do you want to update it? (y/N): " UPDATE_SECRET
        if [[ ! $UPDATE_SECRET =~ ^[Yy]$ ]]; then
            print_info "Skipping secret creation"
            return
        fi
    fi
    
    # Create or update secret
    kubectl create secret generic "$SECRET_NAME" \
        --namespace="$NAMESPACE" \
        --from-file=keystore.jks="$JKS_DIR/keystore.jks" \
        --from-file=truststore.jks="$JKS_DIR/truststore.jks" \
        --from-literal=keystore-password="$KEYSTORE_PASSWORD" \
        --from-literal=truststore-password="$TRUSTSTORE_PASSWORD" \
        --dry-run=client -o yaml | kubectl apply -f -
    
    print_success "JKS secret created: $SECRET_NAME"
}

# Function to create MongoDB connection secret
create_mongodb_connection_secret() {
    print_info "Creating MongoDB connection secret..."
    
    local connection_secret_name="${SECRET_NAME}-connection"
    
    kubectl create secret generic "$connection_secret_name" \
        --namespace="$NAMESPACE" \
        --from-literal=mongodb-uri="$MONGODB_URI" \
        --from-literal=mongodb-database="$MONGODB_DATABASE" \
        --from-literal=mongodb-username="$MONGODB_USERNAME" \
        --from-literal=mongodb-password="$MONGODB_PASSWORD" \
        --from-literal=mongodb-auth-database="admin" \
        --from-literal=mongodb-ssl-enabled="true" \
        --from-literal=mongodb-ssl-invalid-hostname-allowed="true" \
        --dry-run=client -o yaml | kubectl apply -f -
    
    print_success "MongoDB connection secret created: $connection_secret_name"
}

# Function to verify secrets
verify_secrets() {
    print_info "Verifying created secrets..."
    
    local secrets=(
        "$SECRET_NAME"
        "${SECRET_NAME}-connection"
    )
    
    for secret in "${secrets[@]}"; do
        if kubectl get secret "$secret" --namespace="$NAMESPACE" &> /dev/null; then
            print_success "Secret '$secret' exists"
            
            # Show secret details
            echo "  Data keys:"
            kubectl get secret "$secret" --namespace="$NAMESPACE" -o jsonpath='{.data}' | jq -r 'keys[]' | sed 's/^/    - /'
        else
            print_error "Secret '$secret' not found"
            exit 1
        fi
    done
    
    print_success "All secrets verified successfully"
}

# Function to show JKS information
show_jks_info() {
    print_info "JKS files information:"
    echo ""
    
    # Show keystore information
    print_info "Keystore contents:"
    keytool -list -keystore "$JKS_DIR/keystore.jks" -storepass "$KEYSTORE_PASSWORD" | head -20
    echo ""
    
    # Show truststore information
    print_info "Truststore contents:"
    keytool -list -keystore "$JKS_DIR/truststore.jks" -storepass "$TRUSTSTORE_PASSWORD" | head -20
    echo ""
}

# Function to show next steps
show_next_steps() {
    print_info "Next steps:"
    echo ""
    echo "1. Update your Helm values to reference these secrets:"
    echo "   secrets:"
    echo "     mongodb:"
    echo "       keystore:"
    echo "         name: \"$SECRET_NAME\""
    echo "       connection:"
    echo "         name: \"${SECRET_NAME}-connection\""
    echo ""
    echo "2. Deploy your application:"
    echo "   helm install client-onboarding ./helm/client-onboarding \\"
    echo "     --namespace $NAMESPACE \\"
    echo "     --set mongodb.enabled=true"
    echo ""
    echo "3. Verify the deployment:"
    echo "   kubectl get pods -n $NAMESPACE"
    echo "   kubectl logs -f deployment/client-onboarding -n $NAMESPACE"
    echo ""
    echo "4. Test JKS files in the pod:"
    echo "   kubectl exec -it <pod-name> -- ls -la /app/keystore/"
    echo "   kubectl exec -it <pod-name> -- keytool -list -keystore /app/keystore/keystore.jks -storepass \$MONGODB_KEYSTORE_PASSWORD"
    echo ""
}

# Function to create sample deployment YAML
create_sample_deployment() {
    print_info "Creating sample deployment YAML..."
    
    cat > sample-deployment.yaml <<EOF
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
              name: $SECRET_NAME
              key: keystore-password
        - name: MONGODB_TRUSTSTORE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: $SECRET_NAME
              key: truststore-password
        # MongoDB connection details
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: ${SECRET_NAME}-connection
              key: mongodb-uri
        - name: MONGODB_DATABASE
          valueFrom:
            secretKeyRef:
              name: ${SECRET_NAME}-connection
              key: mongodb-database
        - name: MONGODB_USERNAME
          valueFrom:
            secretKeyRef:
              name: ${SECRET_NAME}-connection
              key: mongodb-username
        - name: MONGODB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ${SECRET_NAME}-connection
              key: mongodb-password
        volumeMounts:
        # Mount JKS files
        - name: mongodb-keystore-volume
          mountPath: /app/keystore
          readOnly: true
      volumes:
      - name: mongodb-keystore-volume
        secret:
          secretName: $SECRET_NAME
          defaultMode: 0400  # Read-only for owner
EOF
    
    print_success "Sample deployment YAML created: sample-deployment.yaml"
}

# Main function
main() {
    echo "MongoDB JKS Certificate Setup Script"
    echo "===================================="
    echo ""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            -d|--jks-dir)
                JKS_DIR="$2"
                shift 2
                ;;
            -s|--secret-name)
                SECRET_NAME="$2"
                shift 2
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Check prerequisites
    check_prerequisites
    
    # Validate JKS files
    validate_jks_files
    
    # Get JKS passwords
    get_jks_passwords
    
    # Get MongoDB details
    get_mongodb_details
    
    # Create secrets
    create_jks_secret
    create_mongodb_connection_secret
    
    # Verify secrets
    verify_secrets
    
    # Show JKS information
    show_jks_info
    
    # Create sample deployment
    create_sample_deployment
    
    # Show next steps
    show_next_steps
    
    print_success "MongoDB JKS certificate setup completed successfully!"
}

# Run main function
main "$@"
