# CONNECT方法支持说明

## 概述

本HTTP代理服务器现已完全支持CONNECT方法，可以处理HTTPS代理请求和建立TCP隧道连接。

## CONNECT方法工作原理

### 1. 客户端发起CONNECT请求
```http
CONNECT www.google.com:443 HTTP/1.1
Host: www.google.com:443
Proxy-Connection: keep-alive
```

### 2. 代理服务器响应
```http
HTTP/1.1 200 Connection established
Connection: keep-alive
```

### 3. 建立隧道
- 代理服务器移除HTTP编解码器
- 切换到TCP隧道模式
- 透明转发客户端和目标服务器之间的数据

## 功能特性

### ✅ 支持的功能
- **HTTPS代理**: 完整支持HTTPS网站访问
- **TCP隧道**: 建立双向TCP数据隧道
- **下游认证**: 自动处理下游代理的用户名密码认证
- **连接复用**: 支持长连接和连接复用
- **错误处理**: 完善的连接错误处理和恢复

### 🔧 技术实现
- **动态管道切换**: 从HTTP模式切换到隧道模式
- **异步处理**: 基于Netty的异步事件驱动架构
- **内存管理**: 正确的ByteBuf内存管理和释放
- **连接生命周期**: 完整的连接建立、维护和关闭流程

## 使用示例

### 1. 浏览器配置
```
HTTP代理: localhost:8080
HTTPS代理: localhost:8080
用户名: (留空)
密码: (留空)
```

### 2. curl命令
```bash
# 访问HTTPS网站
curl -v -x http://localhost:8080 https://www.google.com

# 查看详细连接过程
curl -v --proxy-header "Proxy-Connection: keep-alive" \
     -x http://localhost:8080 https://httpbin.org/get
```

### 3. 程序配置
```java
// Java HTTP客户端配置
Proxy proxy = new Proxy(Proxy.Type.HTTP, 
    new InetSocketAddress("localhost", 8080));

// Python requests配置
proxies = {
    'http': 'http://localhost:8080',
    'https': 'http://localhost:8080'
}
```

## 协议流程

### HTTP请求流程
```
客户端 -> [HTTP请求] -> 代理服务器 -> [认证+转发] -> 下游代理 -> 目标服务器
客户端 <- [HTTP响应] <- 代理服务器 <- [响应转发] <- 下游代理 <- 目标服务器
```

### CONNECT请求流程
```
1. 客户端发送CONNECT请求
2. 代理服务器建立到下游代理的连接
3. 代理服务器向下游代理发送认证的CONNECT请求
4. 下游代理建立到目标服务器的连接
5. 代理服务器返回"200 Connection established"
6. 进入隧道模式，透明转发所有数据
```

## 日志示例

### 启动日志
```
INFO  com.example.proxy.HttpProxyServer -- HTTP代理服务器已启动，监听地址: 0.0.0.0:8080
```

### CONNECT处理日志
```
DEBUG com.example.proxy.ProxyRequestHandler -- 收到客户端请求: CONNECT www.google.com:443
DEBUG com.example.proxy.ProxyRequestHandler -- 处理CONNECT请求: www.google.com:443
INFO  com.example.proxy.ProxyRequestHandler -- 建立CONNECT隧道: www.google.com:443
DEBUG com.example.proxy.TunnelHandler -- 成功连接到下游代理: proxy.example.com:3128
DEBUG com.example.proxy.TunnelHandler -- 发送CONNECT请求到下游代理
DEBUG com.example.proxy.TunnelHandler -- 下游代理隧道建立成功
```

## 故障排除

### 常见问题

#### 1. CONNECT请求失败
**症状**: 浏览器显示代理连接错误
**可能原因**:
- 下游代理认证失败
- 目标服务器不可达
- 网络连接问题

**解决方案**:
- 检查下游代理的用户名密码
- 验证目标服务器地址和端口
- 查看代理服务器日志

#### 2. HTTPS网站无法访问
**症状**: HTTP网站正常，HTTPS网站访问失败
**可能原因**:
- CONNECT请求被阻止
- 隧道建立失败
- SSL/TLS握手问题

**解决方案**:
- 确保代理服务器支持CONNECT方法
- 检查防火墙设置
- 启用调试日志查看详细错误

#### 3. 连接断开
**症状**: 连接建立后很快断开
**可能原因**:
- 下游代理超时设置
- 网络不稳定
- 资源泄漏

**解决方案**:
- 调整超时参数
- 检查网络稳定性
- 监控内存使用情况

### 调试技巧

#### 1. 启用详细日志
```xml
<!-- logback.xml -->
<logger name="com.example.proxy" level="DEBUG"/>
<logger name="io.netty" level="DEBUG"/>
```

#### 2. 使用tcpdump监控网络
```bash
# 监控代理服务器端口
sudo tcpdump -i any port 8080 -A

# 监控下游代理连接
sudo tcpdump -i any host proxy.example.com -A
```

#### 3. curl详细输出
```bash
curl -v --trace-ascii trace.log -x http://localhost:8080 https://www.google.com
```

## 性能优化

### 1. 连接池配置
- 调整Netty线程池大小
- 优化下游代理连接复用
- 配置合适的超时时间

### 2. 内存管理
- 正确释放ByteBuf
- 避免内存泄漏
- 监控GC性能

### 3. 网络优化
- 启用TCP_NODELAY
- 配置合适的缓冲区大小
- 使用零拷贝技术

## 安全考虑

### 1. 认证安全
- 下游代理认证信息加密存储
- 避免明文传输密码
- 定期更换认证凭据

### 2. 访问控制
- 限制可连接的目标主机
- 实施IP白名单机制
- 监控异常连接

### 3. 日志安全
- 避免记录敏感信息
- 安全存储日志文件
- 定期清理历史日志