package com.example.proxy;

/**
 * 代理服务器配置类
 * 用于配置本地代理服务器和下游认证代理的相关参数
 */
public class ProxyConfig {
    
    // 本地代理服务器配置
    private final String localHost;
    private final int localPort;
    
    // 下游认证代理配置
    private final String downstreamHost;
    private final int downstreamPort;
    private final String username;
    private final String password;
    
    // 连接超时配置
    private final int connectTimeout;
    private final int socketTimeout;
    
    public ProxyConfig(String localHost, int localPort, 
                      String downstreamHost, int downstreamPort,
                      String username, String password) {
        this(localHost, localPort, downstreamHost, downstreamPort, 
             username, password, 30000, 60000);
    }
    
    public ProxyConfig(String localHost, int localPort, 
                      String downstreamHost, int downstreamPort,
                      String username, String password,
                      int connectTimeout, int socketTimeout) {
        this.localHost = localHost;
        this.localPort = localPort;
        this.downstreamHost = downstreamHost;
        this.downstreamPort = downstreamPort;
        this.username = username;
        this.password = password;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }
    
    public String getLocalHost() {
        return localHost;
    }
    
    public int getLocalPort() {
        return localPort;
    }
    
    public String getDownstreamHost() {
        return downstreamHost;
    }
    
    public int getDownstreamPort() {
        return downstreamPort;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public int getSocketTimeout() {
        return socketTimeout;
    }
    
    @Override
    public String toString() {
        return "ProxyConfig{" +
                "localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                ", downstreamHost='" + downstreamHost + '\'' +
                ", downstreamPort=" + downstreamPort +
                ", username='" + username + '\'' +
                ", password='***'" +
                ", connectTimeout=" + connectTimeout +
                ", socketTimeout=" + socketTimeout +
                '}';
    }
}