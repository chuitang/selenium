# HTTP代理服务器

一个用Java实现的无需认证的HTTP代理服务器，可以将客户端的请求转发到需要用户名密码认证的下游代理服务器。

## 功能特性

- **无需认证**: 客户端连接到本代理服务器时无需提供用户名密码
- **自动认证**: 自动处理与下游代理服务器的用户名密码认证
- **高性能**: 基于Netty实现，支持高并发连接
- **支持多种HTTP方法**: GET, POST, PUT, DELETE, HEAD, OPTIONS
- **HTTPS支持**: 支持CONNECT方法，可代理HTTPS请求
- **透明转发**: 完整转发HTTP请求和响应
- **TCP隧道**: 支持CONNECT隧道模式，适用于HTTPS代理
- **错误处理**: 完善的错误处理和日志记录

## 系统架构

```
客户端 -> 本代理服务器(无认证) -> 下游代理服务器(需认证) -> 目标服务器
```

## 快速开始

### 1. 编译项目

```bash
mvn clean compile
```

### 2. 运行服务器

#### 使用默认配置(演示)
```bash
mvn exec:java -Dexec.mainClass="com.example.proxy.ProxyServerMain"
```

#### 使用自定义配置
```bash
mvn exec:java -Dexec.mainClass="com.example.proxy.ProxyServerMain" \
  -Dexec.args="0.0.0.0 8080 proxy.company.com 3128 myuser mypass"
```

### 3. 打包为可执行JAR

```bash
mvn clean package
java -jar target/http-proxy-server-1.0.0.jar 0.0.0.0 8080 proxy.company.com 3128 myuser mypass
```

## 参数说明

启动代理服务器需要以下6个参数：

| 参数 | 说明 | 示例 |
|------|------|------|
| 本地主机 | 本地代理服务器监听的IP地址 | `0.0.0.0` |
| 本地端口 | 本地代理服务器监听的端口 | `8080` |
| 下游代理主机 | 需要认证的下游代理服务器地址 | `proxy.company.com` |
| 下游代理端口 | 下游代理服务器端口 | `3128` |
| 用户名 | 下游代理服务器的认证用户名 | `myuser` |
| 密码 | 下游代理服务器的认证密码 | `mypass` |

## 客户端配置

启动代理服务器后，将客户端的HTTP代理设置为：

- **代理服务器**: `localhost` (或代理服务器的IP地址)
- **端口**: `8080` (或您配置的端口)
- **用户名**: 无需设置
- **密码**: 无需设置

### 浏览器配置示例

#### Chrome/Edge
```bash
# 启动时指定代理
chrome.exe --proxy-server="http://localhost:8080"
```

#### Firefox
1. 打开设置 > 网络设置
2. 选择"手动代理配置"
3. HTTP代理: `localhost`, 端口: `8080`
4. 无需填写用户名密码

#### curl命令示例
```bash
# HTTP代理
curl -x http://localhost:8080 http://example.com

# HTTPS代理（使用CONNECT方法）
curl -x http://localhost:8080 https://www.google.com

# 指定代理协议
curl --proxy-header "Proxy-Connection: keep-alive" -x http://localhost:8080 https://httpbin.org/get
```

## 配置文件

您也可以通过修改配置类来设置默认参数：

```java
// 在 ProxyServerMain.java 中修改默认配置
return new ProxyConfig(
    "0.0.0.0", 8080,           // 本地监听地址和端口
    "your-proxy.com", 3128,    // 下游代理地址和端口
    "your-username", "your-password"  // 认证信息
);
```

## 日志配置

项目使用SLF4J + Logback进行日志记录。可以通过创建`logback.xml`文件来自定义日志配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.example.proxy" level="DEBUG"/>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

## 故障排除

### 常见问题

1. **端口已被占用**
   - 错误信息: `Address already in use`
   - 解决方案: 更换本地监听端口或停止占用端口的程序

2. **无法连接下游代理**
   - 错误信息: `Connection refused` 或 `Connection timeout`
   - 解决方案: 检查下游代理地址、端口和网络连接

3. **认证失败**
   - 错误信息: `Proxy Authentication Required`
   - 解决方案: 验证下游代理的用户名密码是否正确

4. **SSL/HTTPS问题**
   - 这个代理服务器主要处理HTTP请求，对于HTTPS CONNECT请求需要额外配置

## 技术实现

### 核心组件

- **ProxyConfig**: 配置管理类
- **HttpProxyServer**: 基于Netty的HTTP服务器
- **ProxyRequestHandler**: 请求处理和转发逻辑
- **ProxyServerMain**: 主启动类

### 依赖库

- **Netty**: 高性能异步网络框架
- **Apache HttpClient 5**: HTTP客户端库，用于与下游代理通信
- **SLF4J + Logback**: 日志框架

## 安全注意事项

1. **认证信息保护**: 确保下游代理的用户名密码安全存储
2. **网络安全**: 在生产环境中考虑使用HTTPS和其他安全措施
3. **访问控制**: 根据需要限制哪些客户端可以使用此代理服务器

## 许可证

本项目基于MIT许可证开源。