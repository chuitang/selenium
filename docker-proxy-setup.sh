#!/bin/bash

# Docker proxy complete configuration script
# Supports Ubuntu/CentOS/RHEL and other Linux distributions

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored messages
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

# Check if running as root user
check_root() {
    if [[ $EUID -ne 0 ]]; then
        print_error "This script requires root privileges to run"
        exit 1
    fi
}

# Get proxy configuration
get_proxy_config() {
    echo "Please enter proxy server configuration:"
    read -p "Proxy server address: " PROXY_HOST
    read -p "Proxy server port: " PROXY_PORT
    read -p "Username (optional): " PROXY_USER
    read -s -p "Password (optional): " PROXY_PASS
    echo ""
    read -p "No proxy addresses (default: localhost,127.0.0.1,.corp): " NO_PROXY_INPUT
    
    NO_PROXY=${NO_PROXY_INPUT:-"localhost,127.0.0.1,.corp"}
    
    # Build proxy URL
    if [[ -n "$PROXY_USER" && -n "$PROXY_PASS" ]]; then
        PROXY_URL="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
    else
        PROXY_URL="http://${PROXY_HOST}:${PROXY_PORT}"
    fi
    
    print_info "Proxy configuration: $PROXY_URL"
}

# Configure Docker daemon proxy (systemd)
configure_daemon_systemd() {
    print_info "Configuring Docker daemon proxy (systemd)..."
    
    # Create systemd directory
    mkdir -p /etc/systemd/system/docker.service.d
    
    # Create proxy configuration file
    cat > /etc/systemd/system/docker.service.d/http-proxy.conf << EOF
[Service]
Environment="HTTP_PROXY=$PROXY_URL"
Environment="HTTPS_PROXY=$PROXY_URL"
Environment="NO_PROXY=$NO_PROXY"
EOF
    
    print_success "Docker daemon systemd proxy configuration created"
}

# Configure Docker daemon proxy (daemon.json)
configure_daemon_json() {
    print_info "Configuring Docker daemon proxy (daemon.json)..."
    
    # Backup existing configuration
    if [[ -f /etc/docker/daemon.json ]]; then
        cp /etc/docker/daemon.json /etc/docker/daemon.json.backup
        print_info "Existing daemon.json backed up"
    fi
    
    # Create or update daemon.json
    mkdir -p /etc/docker
    
    cat > /etc/docker/daemon.json << EOF
{
  "proxies": {
    "default": {
      "httpProxy": "$PROXY_URL",
      "httpsProxy": "$PROXY_URL",
      "noProxy": "$NO_PROXY"
    }
  }
}
EOF
    
    print_success "Docker daemon.json proxy configuration created"
}

# Configure Docker client proxy
configure_client_proxy() {
    print_info "Configuring Docker client proxy..."
    
    # Configure for current user
    if [[ -n "$SUDO_USER" ]]; then
        USER_HOME=$(eval echo ~$SUDO_USER)
        USER_NAME=$SUDO_USER
    else
        USER_HOME=$HOME
        USER_NAME=$(whoami)
    fi
    
    # Create .docker directory
    mkdir -p "$USER_HOME/.docker"
    
    # Create config.json
    cat > "$USER_HOME/.docker/config.json" << EOF
{
  "proxies": {
    "default": {
      "httpProxy": "$PROXY_URL",
      "httpsProxy": "$PROXY_URL",
      "noProxy": "$NO_PROXY"
    }
  }
}
EOF
    
    # Set correct ownership
    if [[ -n "$SUDO_USER" ]]; then
        chown -R $SUDO_USER:$SUDO_USER "$USER_HOME/.docker"
    fi
    
    print_success "Docker client proxy configuration created"
}

# Create environment variable script
create_env_script() {
    print_info "Creating environment variable script..."
    
    cat > /etc/profile.d/docker-proxy.sh << EOF
#!/bin/bash
# Docker proxy environment variables
export HTTP_PROXY="$PROXY_URL"
export HTTPS_PROXY="$PROXY_URL"
export NO_PROXY="$NO_PROXY"
export http_proxy="$PROXY_URL"
export https_proxy="$PROXY_URL"
export no_proxy="$NO_PROXY"
EOF
    
    chmod +x /etc/profile.d/docker-proxy.sh
    print_success "Environment variable script created"
}

# Restart Docker service
restart_docker() {
    print_info "Restarting Docker service..."
    
    systemctl daemon-reload
    systemctl restart docker
    
    if systemctl is-active --quiet docker; then
        print_success "Docker service restarted successfully"
    else
        print_error "Docker service restart failed"
        return 1
    fi
}

# Verify configuration
verify_configuration() {
    print_info "Verifying Docker proxy configuration..."
    
    # Check Docker daemon status
    if ! systemctl is-active --quiet docker; then
        print_error "Docker service is not running"
        return 1
    fi
    
    # Test Docker pull
    print_info "Testing Docker pull..."
    if docker pull hello-world:latest > /dev/null 2>&1; then
        print_success "Docker pull test successful"
    else
        print_warning "Docker pull test failed, please check proxy configuration"
    fi
    
    # Test container network access
    print_info "Testing container network access..."
    if docker run --rm \
        -e HTTP_PROXY="$PROXY_URL" \
        -e HTTPS_PROXY="$PROXY_URL" \
        -e NO_PROXY="$NO_PROXY" \
        alpine:latest \
        sh -c "apk add --no-cache curl > /dev/null 2>&1 && curl -s https://httpbin.org/ip" > /dev/null 2>&1; then
        print_success "Container network access test successful"
    else
        print_warning "Container network access test failed"
    fi
}

# Show configuration information
show_configuration() {
    print_info "Docker proxy configuration information:"
    echo "Proxy URL: $PROXY_URL"
    echo "No proxy addresses: $NO_PROXY"
    echo ""
    echo "Configuration file locations:"
    echo "- Systemd: /etc/systemd/system/docker.service.d/http-proxy.conf"
    echo "- Daemon: /etc/docker/daemon.json"
    echo "- Client: ~/.docker/config.json"
    echo "- Environment: /etc/profile.d/docker-proxy.sh"
}

# Main function
main() {
    print_info "Docker Proxy Configuration Script"
    echo "=================================="
    
    check_root
    get_proxy_config
    
    echo ""
    print_info "Starting Docker proxy configuration..."
    
    configure_daemon_systemd
    configure_daemon_json
    configure_client_proxy
    create_env_script
    
    restart_docker
    verify_configuration
    
    echo ""
    show_configuration
    
    echo ""
    print_success "Docker proxy configuration completed!"
    print_info "Please re-login or run 'source /etc/profile.d/docker-proxy.sh' to load environment variables"
}

# Run main function
main "$@"