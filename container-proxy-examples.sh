#!/bin/bash

# Container-only proxy usage examples
# Demonstrates various ways to use proxy ONLY in containers

# Color output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}Container-Only Proxy Usage Examples${NC}"
echo "===================================="
echo ""

# Check if proxy env file exists
if [[ ! -f "container-proxy.env" ]]; then
    echo -e "${YELLOW}Warning: container-proxy.env not found${NC}"
    echo "Please run './container-proxy-only.sh' first to create the proxy configuration"
    echo ""
    echo "Creating a sample proxy configuration for demonstration..."
    cat > container-proxy.env << 'EOF'
HTTP_PROXY=http://proxy.example.com:8080
HTTPS_PROXY=http://proxy.example.com:8080
NO_PROXY=localhost,127.0.0.1,.corp
http_proxy=http://proxy.example.com:8080
https_proxy=http://proxy.example.com:8080
no_proxy=localhost,127.0.0.1,.corp
EOF
    echo "Sample configuration created: container-proxy.env"
    echo ""
fi

echo -e "${GREEN}Example 1: Basic container with proxy using env file${NC}"
echo "Command:"
echo "docker run --rm --env-file container-proxy.env alpine:latest env | grep -i proxy"
echo ""
echo "Running example..."
docker run --rm --env-file container-proxy.env alpine:latest env | grep -i proxy
echo ""

echo -e "${GREEN}Example 2: Interactive container with proxy${NC}"
echo "Command:"
echo "docker run -it --rm --env-file container-proxy.env ubuntu:20.04 bash"
echo "(This would start an interactive bash session with proxy configured)"
echo ""

echo -e "${GREEN}Example 3: Container with manual proxy environment variables${NC}"
echo "Command:"
echo 'docker run --rm -e HTTP_PROXY=http://proxy:8080 -e HTTPS_PROXY=http://proxy:8080 alpine:latest env | grep -i proxy'
echo ""
echo "Running example..."
docker run --rm -e HTTP_PROXY=http://proxy.example.com:8080 -e HTTPS_PROXY=http://proxy.example.com:8080 alpine:latest env | grep -i proxy
echo ""

echo -e "${GREEN}Example 4: Test network connectivity in container${NC}"
echo "Command:"
echo "docker run --rm --env-file container-proxy.env alpine:latest sh -c 'apk add curl && curl -s https://httpbin.org/ip'"
echo ""
echo "Running example (this will fail if proxy is not reachable, which is expected in demo)..."
docker run --rm --env-file container-proxy.env alpine:latest sh -c 'apk add --no-cache curl >/dev/null 2>&1 && echo "Testing proxy..." && timeout 5 curl -s https://httpbin.org/ip || echo "Proxy test failed (expected if proxy not accessible)"'
echo ""

echo -e "${GREEN}Example 5: Python application with proxy${NC}"
echo "Command:"
echo "docker run --rm --env-file container-proxy.env python:3.9-slim python -c \"import os; print('HTTP_PROXY:', os.getenv('HTTP_PROXY'))\""
echo ""
echo "Running example..."
docker run --rm --env-file container-proxy.env python:3.9-slim python -c "import os; print('HTTP_PROXY:', os.getenv('HTTP_PROXY'))"
echo ""

echo -e "${GREEN}Example 6: Node.js application with proxy${NC}"
echo "Command:"
echo "docker run --rm --env-file container-proxy.env node:16-alpine node -e \"console.log('HTTP_PROXY:', process.env.HTTP_PROXY)\""
echo ""
echo "Running example..."
docker run --rm --env-file container-proxy.env node:16-alpine node -e "console.log('HTTP_PROXY:', process.env.HTTP_PROXY)"
echo ""

echo -e "${GREEN}Example 7: Execute command in running container with proxy${NC}"
echo "Commands:"
echo "# Start a container in background"
echo "docker run -d --name proxy-test --env-file container-proxy.env alpine:latest tail -f /dev/null"
echo ""
echo "# Execute command with proxy environment"
echo "docker exec proxy-test env | grep -i proxy"
echo ""
echo "# Cleanup"
echo "docker rm -f proxy-test"
echo ""
echo "Running example..."
docker run -d --name proxy-test --env-file container-proxy.env alpine:latest tail -f /dev/null >/dev/null
echo "Container started, checking proxy environment..."
docker exec proxy-test env | grep -i proxy
echo "Cleaning up..."
docker rm -f proxy-test >/dev/null
echo ""

echo -e "${GREEN}Example 8: Docker Compose with container proxy${NC}"
echo "File: docker-compose-container-proxy.yml"
echo ""
echo "Command to run:"
echo "docker-compose -f docker-compose-container-proxy.yml up -d"
echo ""
echo "This will start services with proxy configuration from container-proxy.env"
echo ""

echo -e "${GREEN}Example 9: Build-time proxy (Dockerfile)${NC}"
echo "Create a Dockerfile with:"
cat << 'EOF'

FROM ubuntu:20.04

# Use build args for proxy (pass from docker build command)
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY

# Set environment variables
ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV NO_PROXY=${NO_PROXY}

# Install packages (will use proxy if configured)
RUN apt-get update && apt-get install -y curl

# Your application code here
COPY . /app
WORKDIR /app

CMD ["bash"]
EOF

echo ""
echo "Build command:"
echo "docker build --build-arg HTTP_PROXY=http://proxy:8080 --build-arg HTTPS_PROXY=http://proxy:8080 -t my-app ."
echo ""

echo -e "${GREEN}Example 10: Container with custom proxy script${NC}"
echo "Create a container that configures proxy internally:"
echo ""
cat << 'EOF' > temp-proxy-script.sh
#!/bin/bash
# This script runs inside the container

# Set proxy from environment or use default
PROXY_HOST=${PROXY_HOST:-proxy.example.com}
PROXY_PORT=${PROXY_PORT:-8080}

export HTTP_PROXY="http://${PROXY_HOST}:${PROXY_PORT}"
export HTTPS_PROXY="http://${PROXY_HOST}:${PROXY_PORT}"

echo "Proxy configured: $HTTP_PROXY"

# Run your application
exec "$@"
EOF

echo "Command:"
echo "docker run --rm -v \$(pwd)/temp-proxy-script.sh:/proxy-setup.sh -e PROXY_HOST=proxy.example.com alpine:latest sh /proxy-setup.sh env | grep -i proxy"
echo ""
echo "Running example..."
docker run --rm -v $(pwd)/temp-proxy-script.sh:/proxy-setup.sh -e PROXY_HOST=proxy.example.com alpine:latest sh /proxy-setup.sh env | grep -i proxy
rm -f temp-proxy-script.sh
echo ""

echo -e "${BLUE}Summary${NC}"
echo "======="
echo "All these examples demonstrate how to configure proxy ONLY for containers"
echo "without modifying the host system or Docker daemon configuration."
echo ""
echo "Key methods:"
echo "1. --env-file container-proxy.env"
echo "2. -e HTTP_PROXY=... -e HTTPS_PROXY=..."
echo "3. Docker Compose with env_file"
echo "4. Build-time proxy with --build-arg"
echo "5. Custom proxy setup scripts"
echo ""
echo "Remember: These configurations only affect the containers, not the host!"