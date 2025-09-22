#!/bin/bash

# JWT Token Caching Testing Script
# This script tests JWT token caching and automatic renewal

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
BASE_URL="http://localhost:8080/api/v1/jwt"
SERVICE_NAME="kyc"
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
    echo "  -u, --url URL              Base URL for API (default: http://localhost:8080/api/v1/jwt)"
    echo "  -s, --service SERVICE      Service name (default: kyc)"
    echo "  -n, --namespace NAMESPACE Kubernetes namespace (default: default)"
    echo "  -p, --pod POD_NAME         Pod name for kubectl commands"
    echo "  -h, --help                 Show this help message"
    echo ""
    echo "Example:"
    echo "  $0 -u http://localhost:8080/api/v1/jwt -s kyc -n default -p client-onboarding-123"
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

# Function to test token service health
test_token_service_health() {
    print_info "Testing token service health..."
    
    local response=$(curl -s -w "%{http_code}" "$BASE_URL/health")
    local http_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$http_code" -eq 200 ]; then
        local healthy=$(echo "$body" | jq -r '.healthy' 2>/dev/null || echo "false")
        if [ "$healthy" = "true" ]; then
            print_success "Token service health: UP"
        else
            print_warning "Token service health: DOWN"
        fi
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        print_error "Token service health check failed (HTTP $http_code)"
        echo "$body"
    fi
    
    echo ""
}

# Function to test token caching
test_token_caching() {
    print_info "Testing JWT token caching for service: $SERVICE_NAME"
    
    # Get initial token
    print_info "Getting initial token..."
    local response1=$(curl -s "$BASE_URL/token/$SERVICE_NAME")
    local token1=$(echo "$response1" | jq -r '.accessToken' 2>/dev/null || echo "")
    
    if [ -n "$token1" ] && [ "$token1" != "null" ]; then
        print_success "Initial token retrieved successfully"
        
        # Get token again (should be cached)
        print_info "Getting token again (should be cached)..."
        local response2=$(curl -s "$BASE_URL/token/$SERVICE_NAME")
        local token2=$(echo "$response2" | jq -r '.accessToken' 2>/dev/null || echo "")
        
        if [ "$token1" = "$token2" ]; then
            print_success "Token caching working correctly"
        else
            print_warning "Token caching may not be working correctly"
        fi
        
        # Get bearer token
        print_info "Getting bearer token..."
        local bearer_response=$(curl -s "$BASE_URL/bearer/$SERVICE_NAME")
        local bearer_token=$(echo "$bearer_response" | jq -r '.bearerToken' 2>/dev/null || echo "")
        
        if [ -n "$bearer_token" ] && [ "$bearer_token" != "null" ]; then
            print_success "Bearer token retrieved successfully"
            echo "Bearer token: ${bearer_token:0:50}..."
        else
            print_error "Failed to get bearer token"
        fi
        
    else
        print_error "Failed to get initial token"
    fi
    
    echo ""
}

# Function to test token status
test_token_status() {
    print_info "Testing token status for service: $SERVICE_NAME"
    
    local response=$(curl -s "$BASE_URL/status/$SERVICE_NAME")
    local exists=$(echo "$response" | jq -r '.exists' 2>/dev/null || echo "false")
    
    if [ "$exists" = "true" ]; then
        print_success "Token exists in cache"
        
        local expires_in=$(echo "$response" | jq -r '.secondsUntilExpiry' 2>/dev/null || echo "0")
        local cache_expires_in=$(echo "$response" | jq -r '.secondsUntilCacheExpiry' 2>/dev/null || echo "0")
        local needs_renewal=$(echo "$response" | jq -r '.needsRenewal' 2>/dev/null || echo "false")
        local is_renewing=$(echo "$response" | jq -r '.isRenewing' 2>/dev/null || echo "false")
        
        echo "Token expires in: $expires_in seconds"
        echo "Cache expires in: $cache_expires_in seconds"
        echo "Needs renewal: $needs_renewal"
        echo "Is renewing: $is_renewing"
        
    else
        print_warning "Token does not exist in cache"
    fi
    
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
}

# Function to test token refresh
test_token_refresh() {
    print_info "Testing token refresh for service: $SERVICE_NAME"
    
    local response=$(curl -s -X POST "$BASE_URL/refresh/$SERVICE_NAME")
    local message=$(echo "$response" | jq -r '.message' 2>/dev/null || echo "")
    
    if [ "$message" = "Token refreshed successfully" ]; then
        print_success "Token refreshed successfully"
    else
        print_error "Token refresh failed"
    fi
    
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
}

# Function to test cache statistics
test_cache_statistics() {
    print_info "Testing cache statistics..."
    
    local response=$(curl -s "$BASE_URL/statistics")
    local total_tokens=$(echo "$response" | jq -r '.totalTokens' 2>/dev/null || echo "0")
    local active_tokens=$(echo "$response" | jq -r '.activeTokens' 2>/dev/null || echo "0")
    local expired_tokens=$(echo "$response" | jq -r '.expiredTokens' 2>/dev/null || echo "0")
    local renewing_tokens=$(echo "$response" | jq -r '.renewingTokens' 2>/dev/null || echo "0")
    
    print_success "Cache statistics retrieved"
    echo "Total tokens: $total_tokens"
    echo "Active tokens: $active_tokens"
    echo "Expired tokens: $expired_tokens"
    echo "Renewing tokens: $renewing_tokens"
    
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
}

# Function to test all token statuses
test_all_token_statuses() {
    print_info "Testing all token statuses..."
    
    local response=$(curl -s "$BASE_URL/status")
    local total_tokens=$(echo "$response" | jq -r '.totalTokens' 2>/dev/null || echo "0")
    
    print_success "All token statuses retrieved"
    echo "Total cached tokens: $total_tokens"
    
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
}

# Function to test token eviction
test_token_eviction() {
    print_info "Testing token eviction for service: $SERVICE_NAME"
    
    local response=$(curl -s -X DELETE "$BASE_URL/evict/$SERVICE_NAME")
    local message=$(echo "$response" | jq -r '.message' 2>/dev/null || echo "")
    
    if [ "$message" = "Token evicted from cache" ]; then
        print_success "Token evicted successfully"
    else
        print_error "Token eviction failed"
    fi
    
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
}

# Function to run comprehensive tests
run_comprehensive_tests() {
    print_info "Running comprehensive JWT token caching tests..."
    echo "=================================================="
    echo ""
    
    # Test token service health
    test_token_service_health
    
    # Test token caching
    test_token_caching
    
    # Test token status
    test_token_status
    
    # Test cache statistics
    test_cache_statistics
    
    # Test all token statuses
    test_all_token_statuses
    
    # Test token refresh
    test_token_refresh
    
    # Test token eviction
    test_token_eviction
    
    print_success "Comprehensive JWT token caching tests completed!"
}

# Function to run quick tests
run_quick_tests() {
    print_info "Running quick JWT token caching tests..."
    echo "============================================="
    echo ""
    
    # Test token service health
    test_token_service_health
    
    # Test token caching
    test_token_caching
    
    # Test token status
    test_token_status
    
    print_success "Quick JWT token caching tests completed!"
}

# Main function
main() {
    echo "JWT Token Caching Testing Script"
    echo "================================="
    echo ""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -u|--url)
                BASE_URL="$2"
                shift 2
                ;;
            -s|--service)
                SERVICE_NAME="$2"
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
    
    print_info "Testing JWT token caching"
    print_info "Base URL: $BASE_URL"
    print_info "Service Name: $SERVICE_NAME"
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
