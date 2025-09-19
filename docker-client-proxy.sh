#!/bin/bash

# Docker client proxy configuration script
# Used to set Docker client environment variables

# Proxy server configuration
PROXY_HOST="proxy.example.com"
PROXY_PORT="8080"
PROXY_USER="your_username"      # Fill in username if authentication is required
PROXY_PASS="your_password"      # Fill in password if authentication is required

# Build proxy URL
if [ -n "$PROXY_USER" ] && [ -n "$PROXY_PASS" ]; then
    HTTP_PROXY="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
    HTTPS_PROXY="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
else
    HTTP_PROXY="http://${PROXY_HOST}:${PROXY_PORT}"
    HTTPS_PROXY="http://${PROXY_HOST}:${PROXY_PORT}"
fi

# Set environment variables
export HTTP_PROXY="$HTTP_PROXY"
export HTTPS_PROXY="$HTTPS_PROXY"
export http_proxy="$HTTP_PROXY"
export https_proxy="$HTTPS_PROXY"
export NO_PROXY="localhost,127.0.0.1,docker-registry.example.com,.corp"
export no_proxy="$NO_PROXY"

echo "Docker client proxy has been configured:"
echo "HTTP_PROXY: $HTTP_PROXY"
echo "HTTPS_PROXY: $HTTPS_PROXY"
echo "NO_PROXY: $NO_PROXY"

# Verify Docker connection
echo "Verifying Docker connection..."
docker version