#!/bin/bash

# Docker容器运行时代理配置示例

# 代理服务器配置
PROXY_HOST="proxy.example.com"
PROXY_PORT="8080"
PROXY_USER="your_username"      # 如果需要认证
PROXY_PASS="your_password"      # 如果需要认证

# 构建代理URL
if [ -n "$PROXY_USER" ] && [ -n "$PROXY_PASS" ]; then
    PROXY_URL="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
else
    PROXY_URL="http://${PROXY_HOST}:${PROXY_PORT}"
fi

echo "=== Docker容器运行时代理配置示例 ==="

# 方法1: 使用-e参数设置环境变量
echo "方法1: 使用-e参数"
docker run -it --rm \
    -e HTTP_PROXY="$PROXY_URL" \
    -e HTTPS_PROXY="$PROXY_URL" \
    -e NO_PROXY="localhost,127.0.0.1,.corp" \
    ubuntu:20.04 bash -c "env | grep -i proxy"

echo ""

# 方法2: 使用--env-file参数
echo "方法2: 使用--env-file参数"
cat > proxy.env << EOF
HTTP_PROXY=$PROXY_URL
HTTPS_PROXY=$PROXY_URL
NO_PROXY=localhost,127.0.0.1,.corp
EOF

docker run -it --rm \
    --env-file proxy.env \
    ubuntu:20.04 bash -c "env | grep -i proxy"

echo ""

# 方法3: 在容器内测试网络连接
echo "方法3: 测试容器内网络连接"
docker run -it --rm \
    -e HTTP_PROXY="$PROXY_URL" \
    -e HTTPS_PROXY="$PROXY_URL" \
    -e NO_PROXY="localhost,127.0.0.1,.corp" \
    ubuntu:20.04 bash -c "
        apt-get update -qq && 
        apt-get install -y curl -qq && 
        echo '测试通过代理访问外网:' && 
        curl -s https://httpbin.org/ip
    "

echo ""

# 方法4: 运行需要网络访问的应用
echo "方法4: 运行需要网络访问的应用示例"
docker run -it --rm \
    -e HTTP_PROXY="$PROXY_URL" \
    -e HTTPS_PROXY="$PROXY_URL" \
    -e NO_PROXY="localhost,127.0.0.1,.corp" \
    python:3.9-slim bash -c "
        pip install --quiet requests && 
        python -c 'import requests; print(\"代理测试:\", requests.get(\"https://httpbin.org/ip\").json())'
    "

# 清理临时文件
rm -f proxy.env