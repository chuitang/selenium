#!/bin/bash

# Docker container runtime proxy configuration examples

# Proxy server configuration
PROXY_HOST="proxy.example.com"
PROXY_PORT="8080"
PROXY_USER="your_username"      # If authentication is required
PROXY_PASS="your_password"      # If authentication is required

# Build proxy URL
if [ -n "$PROXY_USER" ] && [ -n "$PROXY_PASS" ]; then
    PROXY_URL="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
else
    PROXY_URL="http://${PROXY_HOST}:${PROXY_PORT}"
fi

echo "=== Docker Container Runtime Proxy Configuration Examples ==="

# Method 1: Using -e parameter to set environment variables
echo "Method 1: Using -e parameter"
docker run -it --rm \
    -e HTTP_PROXY="$PROXY_URL" \
    -e HTTPS_PROXY="$PROXY_URL" \
    -e NO_PROXY="localhost,127.0.0.1,.corp" \
    ubuntu:20.04 bash -c "env | grep -i proxy"

echo ""

# Method 2: Using --env-file parameter
echo "Method 2: Using --env-file parameter"
cat > proxy.env << EOF
HTTP_PROXY=$PROXY_URL
HTTPS_PROXY=$PROXY_URL
NO_PROXY=localhost,127.0.0.1,.corp
EOF

docker run -it --rm \
    --env-file proxy.env \
    ubuntu:20.04 bash -c "env | grep -i proxy"

echo ""

# Method 3: Testing network connection inside container
echo "Method 3: Testing network connection inside container"
docker run -it --rm \
    -e HTTP_PROXY="$PROXY_URL" \
    -e HTTPS_PROXY="$PROXY_URL" \
    -e NO_PROXY="localhost,127.0.0.1,.corp" \
    ubuntu:20.04 bash -c "
        apt-get update -qq && 
        apt-get install -y curl -qq && 
        echo 'Testing external network access via proxy:' && 
        curl -s https://httpbin.org/ip
    "

echo ""

# Method 4: Running applications that require network access
echo "Method 4: Running applications that require network access example"
docker run -it --rm \
    -e HTTP_PROXY="$PROXY_URL" \
    -e HTTPS_PROXY="$PROXY_URL" \
    -e NO_PROXY="localhost,127.0.0.1,.corp" \
    python:3.9-slim bash -c "
        pip install --quiet requests && 
        python -c 'import requests; print(\"Proxy test:\", requests.get(\"https://httpbin.org/ip\").json())'
    "

# Clean up temporary files
rm -f proxy.env