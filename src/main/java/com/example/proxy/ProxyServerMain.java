package com.example.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP代理服务器主启动类
 * 
 * 该代理服务器的功能:
 * 1. 监听本地端口，接收客户端的HTTP请求
 * 2. 将请求转发到配置的下游代理服务器
 * 3. 自动处理下游代理的用户名密码认证
 * 4. 将下游代理的响应返回给客户端
 * 
 * 使用方法:
 * java -jar proxy-server.jar [本地主机] [本地端口] [下游代理主机] [下游代理端口] [用户名] [密码]
 * 
 * 示例:
 * java -jar proxy-server.jar 0.0.0.0 8080 proxy.example.com 3128 myuser mypass
 */
public class ProxyServerMain {
    
    private static final Logger logger = LoggerFactory.getLogger(ProxyServerMain.class);
    
    public static void main(String[] args) {
        // 解析命令行参数
        ProxyConfig config = parseArguments(args);
        if (config == null) {
            printUsage();
            System.exit(1);
        }
        
        // 创建并启动代理服务器
        HttpProxyServer server = new HttpProxyServer(config);
        
        // 添加JVM关闭钩子，确保服务器优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("收到关闭信号，正在关闭代理服务器...");
            server.shutdown();
        }));
        
        try {
            logger.info("正在启动HTTP代理服务器...");
            server.start();
        } catch (InterruptedException e) {
            logger.error("服务器启动被中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("服务器启动失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 解析命令行参数
     */
    private static ProxyConfig parseArguments(String[] args) {
        if (args.length == 0) {
            // 使用默认配置进行演示
            logger.info("未提供命令行参数，使用默认配置进行演示");
            return new ProxyConfig(
                "0.0.0.0", 8080,           // 本地监听地址和端口
                "proxy.example.com", 3128,  // 下游代理地址和端口
                "username", "password"      // 下游代理认证信息
            );
        }
        
        if (args.length != 6) {
            return null;
        }
        
        try {
            String localHost = args[0];
            int localPort = Integer.parseInt(args[1]);
            String downstreamHost = args[2];
            int downstreamPort = Integer.parseInt(args[3]);
            String username = args[4];
            String password = args[5];
            
            return new ProxyConfig(localHost, localPort, downstreamHost, downstreamPort, username, password);
            
        } catch (NumberFormatException e) {
            logger.error("端口号格式错误: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("HTTP代理服务器");
        System.out.println("================");
        System.out.println();
        System.out.println("功能说明:");
        System.out.println("  - 创建一个无需认证的HTTP代理服务器");
        System.out.println("  - 自动将客户端请求转发到需要认证的下游代理");
        System.out.println("  - 支持HTTP/HTTPS代理转发");
        System.out.println();
        System.out.println("使用方法:");
        System.out.println("  java -jar proxy-server.jar <本地主机> <本地端口> <下游代理主机> <下游代理端口> <用户名> <密码>");
        System.out.println();
        System.out.println("参数说明:");
        System.out.println("  本地主机      - 本地代理服务器监听的IP地址 (如: 0.0.0.0)");
        System.out.println("  本地端口      - 本地代理服务器监听的端口 (如: 8080)");
        System.out.println("  下游代理主机  - 需要认证的下游代理服务器地址 (如: proxy.example.com)");
        System.out.println("  下游代理端口  - 下游代理服务器端口 (如: 3128)");
        System.out.println("  用户名        - 下游代理服务器的认证用户名");
        System.out.println("  密码          - 下游代理服务器的认证密码");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar proxy-server.jar 0.0.0.0 8080 proxy.company.com 3128 myuser mypass");
        System.out.println();
        System.out.println("客户端配置:");
        System.out.println("  将客户端的HTTP代理设置为: localhost:8080 (无需用户名密码)");
    }
}