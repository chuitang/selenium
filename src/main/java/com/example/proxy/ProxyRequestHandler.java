package com.example.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 代理请求处理器
 * 处理来自客户端的HTTP请求，并转发到需要认证的下游代理服务器
 */
public class ProxyRequestHandler extends SimpleChannelInboundHandler<HttpRequest> {
    
    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestHandler.class);
    
    private final ProxyConfig config;
    private final CloseableHttpClient httpClient;
    private final HttpHost proxyHost;
    
    public ProxyRequestHandler(ProxyConfig config) {
        this.config = config;
        this.proxyHost = new HttpHost(config.getDownstreamHost(), config.getDownstreamPort());
        this.httpClient = createHttpClient();
    }
    
    /**
     * 创建配置了认证信息的HTTP客户端
     */
    private CloseableHttpClient createHttpClient() {
        // 设置认证信息
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(config.getDownstreamHost(), config.getDownstreamPort()),
                new UsernamePasswordCredentials(config.getUsername(), config.getPassword().toCharArray())
        );
        
        // 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(config.getConnectTimeout(), TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(config.getSocketTimeout(), TimeUnit.MILLISECONDS))
                .build();
        
        return HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setProxy(proxyHost)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        logger.debug("收到客户端请求: {} {}", request.method(), request.uri());
        
        try {
            // 转发请求到下游代理
            FullHttpResponse response = forwardRequest(request);
            
            // 发送响应给客户端
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            
        } catch (Exception e) {
            logger.error("处理请求时发生错误: {}", e.getMessage(), e);
            sendErrorResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, 
                            "代理服务器内部错误: " + e.getMessage());
        }
    }
    
    /**
     * 将请求转发到下游认证代理
     */
    private FullHttpResponse forwardRequest(HttpRequest nettyRequest) throws IOException, URISyntaxException {
        HttpMethod method = nettyRequest.method();
        String uri = nettyRequest.uri();
        
        // 创建Apache HttpClient请求
        HttpUriRequest apacheRequest = createApacheRequest(method, uri, nettyRequest);
        
        try (CloseableHttpResponse apacheResponse = httpClient.execute(apacheRequest)) {
            return convertToNettyResponse(apacheResponse);
        }
    }
    
    /**
     * 创建Apache HttpClient请求对象
     */
    private HttpUriRequest createApacheRequest(HttpMethod method, String uri, HttpRequest nettyRequest) 
            throws URISyntaxException {
        URI targetUri = new URI(uri);
        
        HttpUriRequest apacheRequest;
        
        // 根据HTTP方法创建相应的请求对象
        switch (method.name()) {
            case "GET":
                apacheRequest = new HttpGet(targetUri);
                break;
            case "POST":
                HttpPost post = new HttpPost(targetUri);
                if (nettyRequest instanceof FullHttpRequest) {
                    FullHttpRequest fullRequest = (FullHttpRequest) nettyRequest;
                    ByteBuf content = fullRequest.content();
                    if (content.readableBytes() > 0) {
                        byte[] bytes = new byte[content.readableBytes()];
                        content.readBytes(bytes);
                        post.setEntity(new ByteArrayEntity(bytes, null));
                    }
                }
                apacheRequest = post;
                break;
            case "PUT":
                HttpPut put = new HttpPut(targetUri);
                if (nettyRequest instanceof FullHttpRequest) {
                    FullHttpRequest fullRequest = (FullHttpRequest) nettyRequest;
                    ByteBuf content = fullRequest.content();
                    if (content.readableBytes() > 0) {
                        byte[] bytes = new byte[content.readableBytes()];
                        content.readBytes(bytes);
                        put.setEntity(new ByteArrayEntity(bytes, null));
                    }
                }
                apacheRequest = put;
                break;
            case "DELETE":
                apacheRequest = new HttpDelete(targetUri);
                break;
            case "HEAD":
                apacheRequest = new HttpHead(targetUri);
                break;
            case "OPTIONS":
                apacheRequest = new HttpOptions(targetUri);
                break;
            default:
                throw new UnsupportedOperationException("不支持的HTTP方法: " + method.name());
        }
        
        // 复制请求头（排除一些代理相关的头）
        HttpHeaders headers = nettyRequest.headers();
        for (String name : headers.names()) {
            if (!isProxyHeader(name)) {
                for (String value : headers.getAll(name)) {
                    apacheRequest.addHeader(name, value);
                }
            }
        }
        
        return apacheRequest;
    }
    
    /**
     * 检查是否为代理相关的头部，这些头部不应该转发
     */
    private boolean isProxyHeader(String headerName) {
        String lowerCase = headerName.toLowerCase();
        return lowerCase.equals("proxy-connection") ||
               lowerCase.equals("proxy-authorization") ||
               lowerCase.equals("proxy-authenticate") ||
               lowerCase.startsWith("proxy-");
    }
    
    /**
     * 将Apache HttpClient响应转换为Netty响应
     */
    private FullHttpResponse convertToNettyResponse(CloseableHttpResponse apacheResponse) throws IOException {
        // 创建Netty响应
        HttpResponseStatus status = HttpResponseStatus.valueOf(apacheResponse.getCode());
        
        // 读取响应体
        byte[] responseBody = new byte[0];
        HttpEntity entity = apacheResponse.getEntity();
        if (entity != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            entity.writeTo(outputStream);
            responseBody = outputStream.toByteArray();
        }
        
        ByteBuf content = Unpooled.wrappedBuffer(responseBody);
        FullHttpResponse nettyResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, content);
        
        // 复制响应头
        for (Header header : apacheResponse.getHeaders()) {
            nettyResponse.headers().add(header.getName(), header.getValue());
        }
        
        // 设置Content-Length
        nettyResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseBody.length);
        
        return nettyResponse;
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
        ByteBuf content = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, content);
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("连接异常: {}", cause.getMessage(), cause);
        ctx.close();
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("客户端连接已断开");
    }
}