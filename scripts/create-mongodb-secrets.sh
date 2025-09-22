#!/bin/bash

# MongoDB Certificate Management Script
# This script helps create Kubernetes secrets for MongoDB certificates

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
NAMESPACE="default"
SECRET_NAME_PREFIX="mongodb"
CERT_DIR="./mongodb-certs"

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
    echo "  -d, --cert-dir DIR          Certificate directory (default: ./mongodb-certs)"
    echo "  -p, --prefix PREFIX         Secret name prefix (default: mongodb)"
    echo "  -h, --help                  Show this help message"
    echo ""
    echo "Example:"
    echo "  $0 -n production -d /path/to/certs"
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
    
    # Check if certificate directory exists
    if [ ! -d "$CERT_DIR" ]; then
        print_error "Certificate directory '$CERT_DIR' does not exist"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Function to validate certificate files
validate_certificates() {
    print_info "Validating certificate files..."
    
    local missing_files=()
    
    # Check for required files
    local required_files=(
        "keystore.jks"
        "truststore.jks"
        "mongodb-cert.pem"
        "mongodb-key.pem"
        "ca-cert.pem"
    )
    
    for file in "${required_files[@]}"; do
        if [ ! -f "$CERT_DIR/$file" ]; then
            missing_files+=("$file")
        fi
    done
    
    if [ ${#missing_files[@]} -gt 0 ]; then
        print_error "Missing required certificate files:"
        for file in "${missing_files[@]}"; do
            echo "  - $file"
        done
        exit 1
    fi
    
    # Validate keystore files
    if ! keytool -list -keystore "$CERT_DIR/keystore.jks" -storepass dummy &> /dev/null; then
        print_warning "keystore.jks validation failed (password may be required)"
    fi
    
    if ! keytool -list -keystore "$CERT_DIR/truststore.jks" -storepass dummy &> /dev/null; then
        print_warning "truststore.jks validation failed (password may be required)"
    fi
    
    # Validate PEM files
    if ! openssl x509 -in "$CERT_DIR/mongodb-cert.pem" -text -noout &> /dev/null; then
        print_error "mongodb-cert.pem is not a valid certificate"
        exit 1
    fi
    
    if ! openssl rsa -in "$CERT_DIR/mongodb-key.pem" -check -noout &> /dev/null; then
        print_error "mongodb-key.pem is not a valid private key"
        exit 1
    fi
    
    print_success "Certificate validation passed"
}

# Function to prompt for passwords
get_passwords() {
    print_info "Please provide the following passwords:"
    
    read -s -p "Keystore password: " KEYSTORE_PASSWORD
    echo ""
    
    read -s -p "Truststore password: " TRUSTSTORE_PASSWORD
    echo ""
    
    read -s -p "MongoDB password: " MONGODB_PASSWORD
    echo ""
    
    read -p "MongoDB URI (default: mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true): " MONGODB_URI
    MONGODB_URI=${MONGODB_URI:-"mongodb://mongodb-service:27017/client_onboarding?ssl=true&sslInvalidHostNameAllowed=true"}
    
    read -p "MongoDB database (default: client_onboarding): " MONGODB_DATABASE
    MONGODB_DATABASE=${MONGODB_DATABASE:-"client_onboarding"}
    
    read -p "MongoDB username (default: onboarding_user): " MONGODB_USERNAME
    MONGODB_USERNAME=${MONGODB_USERNAME:-"onboarding_user"}
}

# Function to create keystore secret
create_keystore_secret() {
    print_info "Creating keystore secret..."
    
    kubectl create secret generic "${SECRET_NAME_PREFIX}-keystore-secret" \
        --namespace="$NAMESPACE" \
        --from-file=keystore.jks="$CERT_DIR/keystore.jks" \
        --from-file=truststore.jks="$CERT_DIR/truststore.jks" \
        --from-literal=keystore-password="$KEYSTORE_PASSWORD" \
        --from-literal=truststore-password="$TRUSTSTORE_PASSWORD" \
        --dry-run=client -o yaml | kubectl apply -f -
    
    print_success "Keystore secret created: ${SECRET_NAME_PREFIX}-keystore-secret"
}

# Function to create SSL secret
create_ssl_secret() {
    print_info "Creating SSL secret..."
    
    kubectl create secret generic "${SECRET_NAME_PREFIX}-ssl-secret" \
        --namespace="$NAMESPACE" \
        --from-file=mongodb-cert.pem="$CERT_DIR/mongodb-cert.pem" \
        --from-file=mongodb-key.pem="$CERT_DIR/mongodb-key.pem" \
        --from-file=ca-cert.pem="$CERT_DIR/ca-cert.pem" \
        --from-literal=mongodb-password="$MONGODB_PASSWORD" \
        --dry-run=client -o yaml | kubectl apply -f -
    
    print_success "SSL secret created: ${SECRET_NAME_PREFIX}-ssl-secret"
}

# Function to create connection secret
create_connection_secret() {
    print_info "Creating connection secret..."
    
    kubectl create secret generic "${SECRET_NAME_PREFIX}-connection-secret" \
        --namespace="$NAMESPACE" \
        --from-literal=mongodb-uri="$MONGODB_URI" \
        --from-literal=mongodb-database="$MONGODB_DATABASE" \
        --from-literal=mongodb-username="$MONGODB_USERNAME" \
        --from-literal=mongodb-password="$MONGODB_PASSWORD" \
        --from-literal=mongodb-auth-database="admin" \
        --from-literal=mongodb-ssl-enabled="true" \
        --from-literal=mongodb-ssl-invalid-hostname-allowed="true" \
        --dry-run=client -o yaml | kubectl apply -f -
    
    print_success "Connection secret created: ${SECRET_NAME_PREFIX}-connection-secret"
}

# Function to verify secrets
verify_secrets() {
    print_info "Verifying created secrets..."
    
    local secrets=(
        "${SECRET_NAME_PREFIX}-keystore-secret"
        "${SECRET_NAME_PREFIX}-ssl-secret"
        "${SECRET_NAME_PREFIX}-connection-secret"
    )
    
    for secret in "${secrets[@]}"; do
        if kubectl get secret "$secret" --namespace="$NAMESPACE" &> /dev/null; then
            print_success "Secret '$secret' exists"
        else
            print_error "Secret '$secret' not found"
            exit 1
        fi
    done
    
    print_success "All secrets verified successfully"
}

# Function to show next steps
show_next_steps() {
    print_info "Next steps:"
    echo ""
    echo "1. Update your Helm values to reference these secrets:"
    echo "   secrets:"
    echo "     mongodb:"
    echo "       keystore:"
    echo "         name: \"${SECRET_NAME_PREFIX}-keystore-secret\""
    echo "       ssl:"
    echo "         name: \"${SECRET_NAME_PREFIX}-ssl-secret\""
    echo "       connection:"
    echo "         name: \"${SECRET_NAME_PREFIX}-connection-secret\""
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
}

# Main function
main() {
    echo "MongoDB Certificate Management Script"
    echo "===================================="
    echo ""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            -d|--cert-dir)
                CERT_DIR="$2"
                shift 2
                ;;
            -p|--prefix)
                SECRET_NAME_PREFIX="$2"
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
    
    # Validate certificates
    validate_certificates
    
    # Get passwords
    get_passwords
    
    # Create secrets
    create_keystore_secret
    create_ssl_secret
    create_connection_secret
    
    # Verify secrets
    verify_secrets
    
    # Show next steps
    show_next_steps
    
    print_success "MongoDB certificate secrets created successfully!"
}

# Run main function
main "$@"
