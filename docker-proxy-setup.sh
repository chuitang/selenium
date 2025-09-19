#!/bin/bash

# Docker代理完整配置脚本
# 支持Ubuntu/CentOS/RHEL等Linux发行版

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
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

# 检查是否为root用户
check_root() {
    if [[ $EUID -ne 0 ]]; then
        print_error "此脚本需要root权限运行"
        exit 1
    fi
}

# 获取代理配置
get_proxy_config() {
    echo "请输入代理服务器配置:"
    read -p "代理服务器地址: " PROXY_HOST
    read -p "代理服务器端口: " PROXY_PORT
    read -p "用户名 (可选): " PROXY_USER
    read -s -p "密码 (可选): " PROXY_PASS
    echo ""
    read -p "不使用代理的地址 (默认: localhost,127.0.0.1,.corp): " NO_PROXY_INPUT
    
    NO_PROXY=${NO_PROXY_INPUT:-"localhost,127.0.0.1,.corp"}
    
    # 构建代理URL
    if [[ -n "$PROXY_USER" && -n "$PROXY_PASS" ]]; then
        PROXY_URL="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"
    else
        PROXY_URL="http://${PROXY_HOST}:${PROXY_PORT}"
    fi
    
    print_info "代理配置: $PROXY_URL"
}

# 配置Docker daemon代理 (systemd)
configure_daemon_systemd() {
    print_info "配置Docker daemon代理 (systemd)..."
    
    # 创建systemd目录
    mkdir -p /etc/systemd/system/docker.service.d
    
    # 创建代理配置文件
    cat > /etc/systemd/system/docker.service.d/http-proxy.conf << EOF
[Service]
Environment="HTTP_PROXY=$PROXY_URL"
Environment="HTTPS_PROXY=$PROXY_URL"
Environment="NO_PROXY=$NO_PROXY"
EOF
    
    print_success "Docker daemon systemd代理配置已创建"
}

# 配置Docker daemon代理 (daemon.json)
configure_daemon_json() {
    print_info "配置Docker daemon代理 (daemon.json)..."
    
    # 备份现有配置
    if [[ -f /etc/docker/daemon.json ]]; then
        cp /etc/docker/daemon.json /etc/docker/daemon.json.backup
        print_info "已备份现有daemon.json"
    fi
    
    # 创建或更新daemon.json
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
    
    print_success "Docker daemon.json代理配置已创建"
}

# 配置Docker客户端代理
configure_client_proxy() {
    print_info "配置Docker客户端代理..."
    
    # 为当前用户配置
    if [[ -n "$SUDO_USER" ]]; then
        USER_HOME=$(eval echo ~$SUDO_USER)
        USER_NAME=$SUDO_USER
    else
        USER_HOME=$HOME
        USER_NAME=$(whoami)
    fi
    
    # 创建.docker目录
    mkdir -p "$USER_HOME/.docker"
    
    # 创建config.json
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
    
    # 设置正确的所有者
    if [[ -n "$SUDO_USER" ]]; then
        chown -R $SUDO_USER:$SUDO_USER "$USER_HOME/.docker"
    fi
    
    print_success "Docker客户端代理配置已创建"
}

# 创建环境变量脚本
create_env_script() {
    print_info "创建环境变量脚本..."
    
    cat > /etc/profile.d/docker-proxy.sh << EOF
#!/bin/bash
# Docker代理环境变量
export HTTP_PROXY="$PROXY_URL"
export HTTPS_PROXY="$PROXY_URL"
export NO_PROXY="$NO_PROXY"
export http_proxy="$PROXY_URL"
export https_proxy="$PROXY_URL"
export no_proxy="$NO_PROXY"
EOF
    
    chmod +x /etc/profile.d/docker-proxy.sh
    print_success "环境变量脚本已创建"
}

# 重启Docker服务
restart_docker() {
    print_info "重启Docker服务..."
    
    systemctl daemon-reload
    systemctl restart docker
    
    if systemctl is-active --quiet docker; then
        print_success "Docker服务重启成功"
    else
        print_error "Docker服务重启失败"
        return 1
    fi
}

# 验证配置
verify_configuration() {
    print_info "验证Docker代理配置..."
    
    # 检查Docker daemon状态
    if ! systemctl is-active --quiet docker; then
        print_error "Docker服务未运行"
        return 1
    fi
    
    # 测试Docker pull
    print_info "测试Docker pull..."
    if docker pull hello-world:latest > /dev/null 2>&1; then
        print_success "Docker pull测试成功"
    else
        print_warning "Docker pull测试失败，请检查代理配置"
    fi
    
    # 测试容器内网络访问
    print_info "测试容器内网络访问..."
    if docker run --rm \
        -e HTTP_PROXY="$PROXY_URL" \
        -e HTTPS_PROXY="$PROXY_URL" \
        -e NO_PROXY="$NO_PROXY" \
        alpine:latest \
        sh -c "apk add --no-cache curl > /dev/null 2>&1 && curl -s https://httpbin.org/ip" > /dev/null 2>&1; then
        print_success "容器内网络访问测试成功"
    else
        print_warning "容器内网络访问测试失败"
    fi
}

# 显示配置信息
show_configuration() {
    print_info "Docker代理配置信息:"
    echo "代理URL: $PROXY_URL"
    echo "不代理地址: $NO_PROXY"
    echo ""
    echo "配置文件位置:"
    echo "- Systemd: /etc/systemd/system/docker.service.d/http-proxy.conf"
    echo "- Daemon: /etc/docker/daemon.json"
    echo "- Client: ~/.docker/config.json"
    echo "- Environment: /etc/profile.d/docker-proxy.sh"
}

# 主函数
main() {
    print_info "Docker代理配置脚本"
    echo "===================="
    
    check_root
    get_proxy_config
    
    echo ""
    print_info "开始配置Docker代理..."
    
    configure_daemon_systemd
    configure_daemon_json
    configure_client_proxy
    create_env_script
    
    restart_docker
    verify_configuration
    
    echo ""
    show_configuration
    
    echo ""
    print_success "Docker代理配置完成！"
    print_info "请重新登录或运行 'source /etc/profile.d/docker-proxy.sh' 来加载环境变量"
}

# 运行主函数
main "$@"