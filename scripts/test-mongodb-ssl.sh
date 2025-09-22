#!/bin/bash

# MongoDB SSL Testing Script
# This script tests MongoDB SSL connections with JKS files

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
BASE_URL="http://localhost:8080/api/v1/mongo"
NAMESPACE="default"
POD_NAME=""

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
    echo "  -u, --url URL              Base URL for API (default: http://localhost:8080/api/v1/mongo)"
    echo "  -n, --namespace NAMESPACE  Kubernetes namespace (default: default)"
    echo "  -p, --pod POD_NAME         Pod name for kubectl commands"
    echo "  -h, --help                 Show this help message"
    echo ""
    echo "Example:"
    echo "  $0 -u http://localhost:8080/api/v1/mongo -n default -p client-onboarding-123"
    echo ""
}

# Function to test API endpoint
test_endpoint() {
    local endpoint=$1
    local description=$2
    
    print_info "Testing $description..."
    
    local response=$(curl -s -w "%{http_code}" "$BASE_URL$endpoint")
    local http_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$http_code" -eq 200 ]; then
        print_success "$description: SUCCESS"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        print_error "$description: FAILED (HTTP $http_code)"
        echo "$body"
    fi
    
    echo ""
}

# Function to test JKS files in pod
test_jks_files() {
    if [ -z "$POD_NAME" ]; then
        print_warning "Pod name not provided, skipping JKS file tests"
        return
    fi
    
    print_info "Testing JKS files in pod: $POD_NAME"
    
    # Check if JKS files exist
    print_info "Checking JKS files existence..."
    kubectl exec -it "$POD_NAME" --namespace="$NAMESPACE" -- ls -la /app/keystore/ || print_error "Failed to list JKS files"
    
    # Check keystore
    print_info "Checking keystore file..."
    kubectl exec -it "$POD_NAME" --namespace="$NAMESPACE" -- file /app/keystore/keystore.jks || print_error "Failed to check keystore"
    
    # Check truststore
    print_info "Checking truststore file..."
    kubectl exec -it "$POD_NAME" --namespace="$NAMESPACE" -- file /app/keystore/truststore.jks || print_error "Failed to check truststore"
    
    echo ""
}

# Function to test environment variables
test_env_vars() {
    if [ -z "$POD_NAME" ]; then
        print_warning "Pod name not provided, skipping environment variable tests"
        return
    fi
    
    print_info "Testing environment variables in pod: $POD_NAME"
    
    # Check MongoDB environment variables
    kubectl exec -it "$POD_NAME" --namespace="$NAMESPACE" -- env | grep MONGODB || print_warning "No MongoDB environment variables found"
    
    echo ""
}

# Function to run comprehensive tests
run_comprehensive_tests() {
    print_info "Running comprehensive MongoDB SSL tests..."
    echo "=============================================="
    echo ""
    
    # Test configuration
    test_endpoint "/config" "MongoDB Configuration"
    
    # Test JKS files
    test_endpoint "/check-jks-files" "JKS Files Status"
    test_endpoint "/test-jks-integrity" "JKS Files Integrity"
    
    # Test connections
    test_endpoint "/test-connection" "MongoDB Connection"
    test_endpoint "/test-basic" "Basic Connection"
    test_endpoint "/test-ssl" "SSL Connection"
    
    # Test operations
    test_endpoint "/test-write" "Write Operations"
    test_endpoint "/test-read" "Read Operations"
    test_endpoint "/test-auth" "Authentication"
    
    # Run comprehensive test
    test_endpoint "/test-comprehensive" "Comprehensive Test Suite"
    
    # Get connection info
    test_endpoint "/connection-info" "Connection Information"
    
    # Test JKS files in pod
    test_jks_files
    
    # Test environment variables
    test_env_vars
    
    print_success "Comprehensive MongoDB SSL tests completed!"
}

# Function to run quick tests
run_quick_tests() {
    print_info "Running quick MongoDB SSL tests..."
    echo "====================================="
    echo ""
    
    # Test configuration
    test_endpoint "/config" "MongoDB Configuration"
    
    # Test JKS files
    test_endpoint "/check-jks-files" "JKS Files Status"
    
    # Test SSL connection
    test_endpoint "/test-ssl" "SSL Connection"
    
    print_success "Quick MongoDB SSL tests completed!"
}

# Main function
main() {
    echo "MongoDB SSL Testing Script"
    echo "=========================="
    echo ""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -u|--url)
                BASE_URL="$2"
                shift 2
                ;;
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            -p|--pod)
                POD_NAME="$2"
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
    
    print_info "Testing MongoDB SSL connection"
    print_info "Base URL: $BASE_URL"
    print_info "Namespace: $NAMESPACE"
    if [ -n "$POD_NAME" ]; then
        print_info "Pod Name: $POD_NAME"
    fi
    echo ""
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_warning "jq is not installed. JSON output will not be formatted."
    fi
    
    # Check if kubectl is available
    if [ -n "$POD_NAME" ] && ! command -v kubectl &> /dev/null; then
        print_warning "kubectl is not installed. Pod tests will be skipped."
        POD_NAME=""
    fi
    
    # Run tests
    if [ "$1" = "quick" ]; then
        run_quick_tests
    else
        run_comprehensive_tests
    fi
}

# Run main function
main "$@"
