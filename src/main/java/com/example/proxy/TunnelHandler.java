package com.example.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * CONNECT隧道处理器
 * 专门处理HTTPS代理的CONNECT请求，建立TCP隧道连接
 */
public class TunnelHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(TunnelHandler.class);
    
    private final ProxyConfig config;
    private Channel outboundChannel;
    private final String targetHost;
    private final int targetPort;
    
    public TunnelHandler(ProxyConfig config, String targetHost, int targetPort) {
        this.config = config;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();
        
        // 创建到下游代理的连接
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new RelayHandler(inboundChannel));
                    }
                })
                .option(ChannelOption.AUTO_READ, false);
        
        ChannelFuture connectFuture = bootstrap.connect(config.getDownstreamHost(), config.getDownstreamPort());
        outboundChannel = connectFuture.channel();
        
        connectFuture.addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    logger.debug("成功连接到下游代理: {}:{}", config.getDownstreamHost(), config.getDownstreamPort());
                    
                    // 发送CONNECT请求到下游代理
                    sendConnectToDownstream();
                    
                    // 启用自动读取
                    inboundChannel.config().setAutoRead(true);
                    outboundChannel.config().setAutoRead(true);
                } else {
                    logger.error("连接下游代理失败: {}:{}", config.getDownstreamHost(), config.getDownstreamPort(), future.cause());
                    
                    // 发送502错误给客户端
                    sendErrorResponse(inboundChannel, HttpResponseStatus.BAD_GATEWAY, "无法连接到下游代理");
                    closeOnFlush(inboundChannel);
                }
            }
        });
    }
    
    /**
     * 向下游代理发送CONNECT请求
     */
    private void sendConnectToDownstream() {
        // 构建CONNECT请求
        String connectRequest = String.format(
            "CONNECT %s:%d HTTP/1.1\r\n" +
            "Host: %s:%d\r\n" +
            "Proxy-Connection: keep-alive\r\n" +
            "User-Agent: Java-HTTP-Proxy/1.0\r\n",
            targetHost, targetPort, targetHost, targetPort
        );
        
        // 如果需要认证，添加认证头
        if (config.getUsername() != null && config.getPassword() != null) {
            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            connectRequest += "Proxy-Authorization: Basic " + encodedAuth + "\r\n";
        }
        
        connectRequest += "\r\n";
        
        logger.debug("发送CONNECT请求到下游代理: {}", connectRequest.trim());
        
        // 发送请求
        outboundChannel.writeAndFlush(Unpooled.wrappedBuffer(connectRequest.getBytes()));
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("隧道连接异常", cause);
        closeOnFlush(ctx.channel());
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(Channel channel, HttpResponseStatus status, String message) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(message.getBytes()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        
        channel.writeAndFlush(response);
    }
    
    /**
     * 关闭通道
     */
    private static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    /**
     * 中继处理器，用于转发数据
     */
    private static class RelayHandler extends ChannelInboundHandlerAdapter {
        private final Channel relayChannel;
        private boolean tunnelEstablished = false;
        
        public RelayHandler(Channel relayChannel) {
            this.relayChannel = relayChannel;
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                
                if (!tunnelEstablished) {
                    // 检查下游代理的CONNECT响应
                    String response = buf.toString(StandardCharsets.UTF_8);
                    logger.debug("下游代理CONNECT响应: {}", response);
                    
                    if (response.contains("200") || response.contains("Connection established")) {
                        tunnelEstablished = true;
                        logger.debug("下游代理隧道建立成功");
                        // 不转发CONNECT响应给客户端，因为我们已经发送了自己的响应
                        buf.release();
                        return;
                    } else {
                        logger.error("下游代理CONNECT失败: {}", response);
                        closeOnFlush(relayChannel);
                        closeOnFlush(ctx.channel());
                        buf.release();
                        return;
                    }
                }
            }
            
            // 转发数据
            if (relayChannel.isActive()) {
                relayChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            ctx.channel().read();
                        } else {
                            future.channel().close();
                        }
                    }
                });
            } else {
                if (msg instanceof ByteBuf) {
                    ((ByteBuf) msg).release();
                }
            }
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            logger.debug("下游连接断开");
            closeOnFlush(relayChannel);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("中继连接异常", cause);
            closeOnFlush(ctx.channel());
        }
    }
}