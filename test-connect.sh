#!/bin/bash

# CONNECT方法测试脚本

echo "==================================="
echo "测试HTTP代理服务器的CONNECT功能"
echo "==================================="
echo ""

# 启动代理服务器在后台
echo "1. 启动代理服务器..."
java -jar target/http-proxy-server-1.0.0.jar 127.0.0.1 8888 proxy.example.com 3128 testuser testpass > proxy.log 2>&1 &
PROXY_PID=$!

# 等待服务器启动
sleep 3

echo "代理服务器已启动 (PID: $PROXY_PID)，监听端口 8888"
echo ""

# 测试CONNECT请求（模拟）
echo "2. 测试CONNECT请求..."
echo ""

# 使用netcat发送CONNECT请求
echo "发送CONNECT请求到代理服务器："
echo "CONNECT www.google.com:443 HTTP/1.1"
echo ""

# 创建临时测试文件
cat << EOF > /tmp/connect_test.txt
CONNECT www.google.com:443 HTTP/1.1
Host: www.google.com:443
Proxy-Connection: keep-alive

EOF

echo "3. 发送CONNECT请求到本地代理："
timeout 5s nc localhost 8888 < /tmp/connect_test.txt || echo "测试完成 (可能会超时，这是正常的)"

echo ""
echo "4. 检查代理服务器日志："
echo "最后10行日志："
tail -10 proxy.log

echo ""
echo "5. 清理..."
# 停止代理服务器
kill $PROXY_PID 2>/dev/null
rm -f /tmp/connect_test.txt

echo "测试完成！"
echo ""
echo "注意："
echo "- CONNECT方法用于建立到HTTPS服务器的隧道连接"
echo "- 代理服务器会返回 '200 Connection established' 响应"
echo "- 然后进入隧道模式，透明转发数据"
echo "- 实际使用时，浏览器会自动处理CONNECT协议"