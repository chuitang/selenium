#!/bin/bash

# Docker客户端代理配置脚本
# 用于设置Docker客户端环境变量

# 代理服务器配置
PROXY_HOST="proxy.example.com"
PROXY_PORT="8080"
PROXY_USER="your_username"      # 如果需要认证，填写用户名
PROXY_PASS="your_password"      # 如果需要认证，填写密码

# 构建代理URL
if [ -n "$PROXY_USER" ] && [ -n "$PROXY_PASS" ]; then
    HTTP_PROXY="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
    HTTPS_PROXY="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
else
    HTTP_PROXY="http://${PROXY_HOST}:${PROXY_PORT}"
    HTTPS_PROXY="http://${PROXY_HOST}:${PROXY_PORT}"
fi

# 设置环境变量
export HTTP_PROXY="$HTTP_PROXY"
export HTTPS_PROXY="$HTTPS_PROXY"
export http_proxy="$HTTP_PROXY"
export https_proxy="$HTTPS_PROXY"
export NO_PROXY="localhost,127.0.0.1,docker-registry.example.com,.corp"
export no_proxy="$NO_PROXY"

echo "Docker客户端代理已设置:"
echo "HTTP_PROXY: $HTTP_PROXY"
echo "HTTPS_PROXY: $HTTPS_PROXY"
echo "NO_PROXY: $NO_PROXY"

# 验证Docker连接
echo "正在验证Docker连接..."
docker version