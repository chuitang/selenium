package com.example.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP代理服务器
 * 使用Netty实现高性能的HTTP代理服务器，接收客户端请求并转发到下游认证代理
 */
public class HttpProxyServer {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpProxyServer.class);
    
    private final ProxyConfig config;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    public HttpProxyServer(ProxyConfig config) {
        this.config = config;
    }
    
    /**
     * 启动代理服务器
     */
    public void start() throws InterruptedException {
        logger.info("启动HTTP代理服务器: {}", config);
        
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast("decoder", new HttpRequestDecoder())
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("handler", new ProxyRequestHandler(config));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            // 绑定端口并启动服务器
            serverChannel = bootstrap.bind(config.getLocalHost(), config.getLocalPort()).sync().channel();
            logger.info("HTTP代理服务器已启动，监听地址: {}:{}", 
                       config.getLocalHost(), config.getLocalPort());
            
            // 等待服务器关闭
            serverChannel.closeFuture().sync();
            
        } finally {
            shutdown();
        }
    }
    
    /**
     * 关闭代理服务器
     */
    public void shutdown() {
        logger.info("正在关闭HTTP代理服务器...");
        
        if (serverChannel != null) {
            serverChannel.close();
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        
        logger.info("HTTP代理服务器已关闭");
    }
    
    /**
     * 获取代理配置
     */
    public ProxyConfig getConfig() {
        return config;
    }
}