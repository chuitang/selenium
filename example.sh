#!/bin/bash

# HTTP代理服务器使用示例

echo "==================================="
echo "HTTP代理服务器使用示例"
echo "==================================="
echo ""

# 示例1：使用默认配置启动代理服务器（演示用）
echo "示例1：使用默认配置启动代理服务器"
echo "java -jar target/http-proxy-server-1.0.0.jar"
echo ""

# 示例2：使用自定义配置启动代理服务器
echo "示例2：使用自定义配置启动代理服务器"
echo "java -jar target/http-proxy-server-1.0.0.jar 0.0.0.0 8080 proxy.company.com 3128 myuser mypass"
echo ""

# 示例3：在后台运行代理服务器
echo "示例3：在后台运行代理服务器"
echo "nohup java -jar target/http-proxy-server-1.0.0.jar 0.0.0.0 8080 proxy.company.com 3128 myuser mypass > proxy.log 2>&1 &"
echo ""

# 示例4：客户端使用代理的方法
echo "示例4：客户端使用代理的方法"
echo ""
echo "4.1 使用curl测试代理："
echo "curl -x http://localhost:8080 http://example.com"
echo ""
echo "4.2 配置浏览器代理："
echo "HTTP代理: localhost"
echo "端口: 8080"
echo "用户名: 无需设置"
echo "密码: 无需设置"
echo ""
echo "4.3 使用Chrome启动时指定代理："
echo "chrome --proxy-server=\"http://localhost:8080\""
echo ""

# 示例5：查看和停止代理服务器
echo "示例5：查看和停止代理服务器"
echo ""
echo "5.1 查看运行的代理服务器进程："
echo "ps aux | grep http-proxy-server"
echo ""
echo "5.2 停止代理服务器："
echo "pkill -f http-proxy-server"
echo ""

echo "==================================="
echo "配置说明："
echo "==================================="
echo "参数1: 本地监听IP地址 (如: 0.0.0.0)"
echo "参数2: 本地监听端口 (如: 8080)"
echo "参数3: 下游代理服务器地址 (如: proxy.company.com)"
echo "参数4: 下游代理服务器端口 (如: 3128)"
echo "参数5: 下游代理认证用户名"
echo "参数6: 下游代理认证密码"
echo ""
echo "注意：客户端连接到本代理服务器时无需提供用户名密码"
echo "     代理服务器会自动处理与下游代理的认证"