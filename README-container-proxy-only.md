# Container-Only Proxy Configuration

This solution provides proxy configuration **ONLY for Docker containers**, without modifying Docker daemon, Docker client, or host system settings.

## 🎯 What This Solution Does

✅ **Affects ONLY containers:**
- Sets proxy environment variables inside containers
- Provides helper functions for easy container proxy management
- Creates Docker Compose examples with container proxy

❌ **Does NOT modify:**
- Docker daemon configuration
- Docker client configuration
- Host system proxy settings
- System environment variables
- Network configuration on host

## 📁 Files Overview

| File | Purpose |
|------|---------|
| `container-proxy-only.sh` | Main configuration script |
| `container-proxy.env` | Environment variables for containers |
| `docker-proxy-helpers.sh` | Helper functions for easy usage |
| `docker-compose-container-proxy.yml` | Docker Compose example |
| `container-proxy-examples.sh` | Usage examples and demonstrations |

## 🚀 Quick Start

### Step 1: Configure Proxy
```bash
./container-proxy-only.sh
```

This will prompt for proxy settings and create necessary files.

### Step 2: Use with Containers

**Method 1: Using environment file**
```bash
docker run --env-file container-proxy.env -it ubuntu:20.04 bash
```

**Method 2: Using helper functions**
```bash
source docker-proxy-helpers.sh
docker_run_with_proxy -it ubuntu:20.04 bash
```

**Method 3: Manual environment variables**
```bash
docker run -e HTTP_PROXY=http://proxy:8080 -e HTTPS_PROXY=http://proxy:8080 -it ubuntu:20.04 bash
```

## 🛠️ Helper Functions

After running the configuration script, you'll have these helper functions:

```bash
# Load helper functions
source docker-proxy-helpers.sh

# Run container with proxy
docker_run_with_proxy -it ubuntu:20.04 bash

# Execute command in running container with proxy
docker_exec_with_proxy my-container curl https://google.com

# Test proxy configuration
test_container_proxy

# Show current proxy settings
show_container_proxy
```

## 🐳 Docker Compose Usage

Use the generated Docker Compose file:

```bash
docker-compose -f docker-compose-container-proxy.yml up
```

Or add to your existing `docker-compose.yml`:

```yaml
services:
  my-service:
    image: my-app:latest
    env_file:
      - container-proxy.env
```

## 📋 Usage Examples

### Basic Container with Proxy
```bash
docker run --rm --env-file container-proxy.env alpine:latest env | grep -i proxy
```

### Interactive Container
```bash
docker run -it --rm --env-file container-proxy.env ubuntu:20.04 bash
# Inside container, proxy environment variables are set
```

### Python Application
```bash
docker run --rm --env-file container-proxy.env python:3.9-slim python -c "
import requests
print('My IP:', requests.get('https://httpbin.org/ip').json())
"
```

### Node.js Application
```bash
docker run --rm --env-file container-proxy.env node:16-alpine node -e "
const https = require('https');
console.log('HTTP_PROXY:', process.env.HTTP_PROXY);
"
```

### Build-time Proxy
```bash
docker build \
  --build-arg HTTP_PROXY=http://proxy:8080 \
  --build-arg HTTPS_PROXY=http://proxy:8080 \
  -t my-app .
```

## 🔧 Environment Variables

The following environment variables are set in containers:

```bash
HTTP_PROXY=http://proxy.example.com:8080
HTTPS_PROXY=http://proxy.example.com:8080
NO_PROXY=localhost,127.0.0.1,.corp
http_proxy=http://proxy.example.com:8080
https_proxy=http://proxy.example.com:8080
no_proxy=localhost,127.0.0.1,.corp
```

## 🧪 Testing

Test your proxy configuration:

```bash
# Run the examples script
./container-proxy-examples.sh

# Or test manually
source docker-proxy-helpers.sh
test_container_proxy
```

## 📝 Dockerfile Integration

For applications that need proxy during build:

```dockerfile
FROM ubuntu:20.04

# Accept proxy build arguments
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY

# Set environment variables
ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV NO_PROXY=${NO_PROXY}

# Install packages (will use proxy)
RUN apt-get update && apt-get install -y curl

# Your application
COPY . /app
WORKDIR /app
CMD ["./start.sh"]
```

Build with proxy:
```bash
docker build \
  --build-arg HTTP_PROXY=http://proxy:8080 \
  --build-arg HTTPS_PROXY=http://proxy:8080 \
  --build-arg NO_PROXY=localhost,127.0.0.1 \
  -t my-app .
```

## 🔍 Troubleshooting

### Check Environment Variables in Container
```bash
docker run --rm --env-file container-proxy.env alpine:latest env | grep -i proxy
```

### Test Network Connectivity
```bash
docker run --rm --env-file container-proxy.env alpine:latest sh -c '
  apk add --no-cache curl
  curl -v https://httpbin.org/ip
'
```

### Debug Proxy Issues
```bash
docker run --rm --env-file container-proxy.env alpine:latest sh -c '
  echo "Proxy settings:"
  env | grep -i proxy
  echo ""
  echo "Testing connectivity..."
  nc -zv proxy.example.com 8080
'
```

## ⚠️ Important Notes

1. **Container-only**: This solution only affects containers, not the host system
2. **No persistence**: Container proxy settings are not persistent across container restarts
3. **Application support**: Applications must respect HTTP_PROXY/HTTPS_PROXY environment variables
4. **Network access**: Containers must be able to reach the proxy server
5. **Authentication**: Proxy credentials are passed as environment variables (consider security implications)

## 🔒 Security Considerations

- Proxy credentials are stored in plain text in environment files
- Consider using Docker secrets for sensitive proxy credentials
- Limit access to `container-proxy.env` file
- Use `.dockerignore` to avoid copying proxy files into images

## 📚 Additional Resources

- [Docker Environment Variables Documentation](https://docs.docker.com/engine/reference/commandline/run/#set-environment-variables--e---env---env-file)
- [Docker Compose Environment Files](https://docs.docker.com/compose/environment-variables/)
- [HTTP Proxy Environment Variables Standard](https://about.gitlab.com/blog/2021/01/27/we-need-to-talk-no-proxy/)