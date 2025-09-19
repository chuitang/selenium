# Docker代理故障排除指南

## 常见问题及解决方案

### 1. Docker Pull失败

**问题现象:**
```bash
$ docker pull nginx
Error response from daemon: Get https://registry-1.docker.io/v2/: net/http: request canceled while waiting for connection
```

**解决方案:**
```bash
# 检查Docker daemon代理配置
sudo systemctl show --property=Environment docker

# 检查daemon.json配置
sudo cat /etc/docker/daemon.json

# 重启Docker服务
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 2. 容器内无法访问外网

**问题现象:**
```bash
$ docker run -it ubuntu:20.04 bash
root@container:/# curl https://www.google.com
curl: (7) Failed to connect to www.google.com port 443: Connection refused
```

**解决方案:**
```bash
# 运行容器时添加代理环境变量
docker run -it \
  -e HTTP_PROXY=http://proxy.example.com:8080 \
  -e HTTPS_PROXY=http://proxy.example.com:8080 \
  -e NO_PROXY=localhost,127.0.0.1 \
  ubuntu:20.04 bash
```

### 3. Docker Build失败

**问题现象:**
```bash
$ docker build -t myapp .
Step 2/5 : RUN apt-get update
 ---> Running in abc123def456
Err:1 http://archive.ubuntu.com/ubuntu focal InRelease
  Temporary failure resolving 'archive.ubuntu.com'
```

**解决方案:**
```bash
# 使用构建参数传递代理
docker build \
  --build-arg HTTP_PROXY=http://proxy.example.com:8080 \
  --build-arg HTTPS_PROXY=http://proxy.example.com:8080 \
  --build-arg NO_PROXY=localhost,127.0.0.1 \
  -t myapp .
```

### 4. Docker Compose服务无法访问外网

**问题现象:**
```yaml
# docker-compose.yml中的服务无法访问外网
```

**解决方案:**
```yaml
version: '3.8'
services:
  webapp:
    image: nginx:alpine
    environment:
      - HTTP_PROXY=http://proxy.example.com:8080
      - HTTPS_PROXY=http://proxy.example.com:8080
      - NO_PROXY=localhost,127.0.0.1
```

### 5. 代理认证失败

**问题现象:**
```bash
Error response from daemon: Get https://registry-1.docker.io/v2/: Proxy Authentication Required
```

**解决方案:**
```bash
# 确保代理URL包含认证信息
HTTP_PROXY=http://username:password@proxy.example.com:8080
HTTPS_PROXY=http://username:password@proxy.example.com:8080

# 注意特殊字符需要URL编码
# 例如: password@123 应该编码为 password%40123
```

## 诊断命令

### 检查Docker daemon代理配置
```bash
# 查看systemd环境变量
sudo systemctl show --property=Environment docker

# 查看daemon.json
sudo cat /etc/docker/daemon.json

# 查看Docker info
docker info | grep -i proxy
```

### 检查网络连通性
```bash
# 测试代理服务器连通性
curl -I --proxy http://proxy.example.com:8080 https://www.google.com

# 测试容器内网络
docker run --rm -it \
  -e HTTP_PROXY=http://proxy.example.com:8080 \
  -e HTTPS_PROXY=http://proxy.example.com:8080 \
  alpine:latest sh -c "apk add curl && curl -I https://www.google.com"
```

### 查看Docker日志
```bash
# 查看Docker daemon日志
sudo journalctl -u docker.service -f

# 查看容器日志
docker logs <container_name>
```

## 配置验证清单

- [ ] Docker daemon代理配置正确
- [ ] Docker客户端代理配置正确
- [ ] 环境变量设置正确
- [ ] 代理服务器可访问
- [ ] 代理认证信息正确
- [ ] NO_PROXY配置合理
- [ ] Docker服务已重启
- [ ] 防火墙规则允许代理连接

## 最佳实践

1. **统一配置管理**: 使用环境变量文件统一管理代理配置
2. **安全考虑**: 避免在命令行中直接暴露密码
3. **网络隔离**: 合理配置NO_PROXY避免不必要的代理
4. **监控日志**: 定期检查Docker和代理日志
5. **测试验证**: 配置后及时验证各项功能

## 参考文档

- [Docker官方代理配置文档](https://docs.docker.com/config/daemon/systemd/#httphttps-proxy)
- [Docker Compose网络配置](https://docs.docker.com/compose/networking/)
- [Dockerfile最佳实践](https://docs.docker.com/develop/dev-best-practices/)