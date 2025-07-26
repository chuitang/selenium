package dev.selenium.cookiemanager.validation;

import dev.selenium.cookiemanager.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Cookie验证类
 * 负责验证Cookie的有效性和尝试刷新Cookie
 */
public class CookieValidator {
    private static final Logger logger = LoggerFactory.getLogger(CookieValidator.class);
    
    private final Config config;
    private final HttpClient httpClient;
    
    public CookieValidator(Config config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
    
    /**
     * 验证Cookie是否有效
     */
    public boolean isValidCookie(String cookie) {
        if (cookie == null || cookie.trim().isEmpty()) {
            logger.warn("Cookie为空或null，无效");
            return false;
        }
        
        try {
            logger.info("验证Cookie有效性...");
            
            // 确定验证URL
            String validationUrl = config.getValidationUrl() != null ? 
                                 config.getValidationUrl() : config.getTargetUrl();
            
            // 构建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(validationUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Cookie", cookie)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .GET()
                    .build();
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 分析响应
            return analyzeResponse(response, validationUrl);
            
        } catch (Exception e) {
            logger.error("验证Cookie时发生错误", e);
            return false;
        }
    }
    
    /**
     * 尝试通过旧Cookie刷新获取新Cookie
     */
    public String tryRefreshCookie(String oldCookie) {
        if (oldCookie == null || oldCookie.trim().isEmpty()) {
            logger.warn("旧Cookie为空，无法刷新");
            return null;
        }
        
        try {
            logger.info("尝试刷新Cookie...");
            
            String targetUrl = config.getTargetUrl();
            
            // 使用旧Cookie访问目标网站
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Cookie", oldCookie)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 检查是否有新的Set-Cookie头
            String newCookie = extractNewCookiesFromResponse(response, oldCookie);
            
            if (newCookie != null) {
                logger.info("成功从响应中提取到新Cookie");
                return newCookie;
            } else {
                logger.warn("响应中没有找到新的Cookie");
                return null;
            }
            
        } catch (Exception e) {
            logger.error("刷新Cookie时发生错误", e);
            return null;
        }
    }
    
    /**
     * 分析HTTP响应判断Cookie是否有效
     */
    private boolean analyzeResponse(HttpResponse<String> response, String url) {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        logger.info("验证请求响应: HTTP {}, URL: {}", statusCode, url);
        
        // 检查HTTP状态码
        if (statusCode == 401 || statusCode == 403) {
            logger.warn("收到认证失败状态码: {}", statusCode);
            return false;
        }
        
        if (statusCode >= 400) {
            logger.warn("收到错误状态码: {}", statusCode);
            return false;
        }
        
        // 检查是否被重定向到登录页面
        if (isRedirectToLogin(response)) {
            logger.warn("响应被重定向到登录页面，Cookie可能已失效");
            return false;
        }
        
        // 检查响应内容中的关键词
        if (containsLoginIndicators(responseBody)) {
            logger.warn("响应内容包含登录相关关键词，Cookie可能已失效");
            return false;
        }
        
        // 如果有配置验证选择器，检查特定元素是否存在
        if (config.getValidationSelector() != null) {
            if (!containsValidationElement(responseBody)) {
                logger.warn("响应中未找到验证元素，Cookie可能已失效");
                return false;
            }
        }
        
        logger.info("Cookie验证通过");
        return true;
    }
    
    /**
     * 检查是否被重定向到登录页面
     */
    private boolean isRedirectToLogin(HttpResponse<String> response) {
        // 检查最终URL是否包含登录相关关键词
        String finalUrl = response.uri().toString().toLowerCase();
        return finalUrl.contains("login") || 
               finalUrl.contains("signin") || 
               finalUrl.contains("auth") || 
               finalUrl.contains("登录");
    }
    
    /**
     * 检查响应内容是否包含登录指示器
     */
    private boolean containsLoginIndicators(String responseBody) {
        if (responseBody == null) {
            return false;
        }
        
        String lowerBody = responseBody.toLowerCase();
        return lowerBody.contains("login") ||
               lowerBody.contains("signin") ||
               lowerBody.contains("username") ||
               lowerBody.contains("password") ||
               lowerBody.contains("登录") ||
               lowerBody.contains("用户名") ||
               lowerBody.contains("密码") ||
               lowerBody.contains("please log in") ||
               lowerBody.contains("session expired") ||
               lowerBody.contains("unauthorized");
    }
    
    /**
     * 检查响应中是否包含验证元素
     */
    private boolean containsValidationElement(String responseBody) {
        String validationSelector = config.getValidationSelector();
        if (validationSelector == null || responseBody == null) {
            return true; // 如果没有配置验证选择器，则认为通过
        }
        
        // 简单的文本包含检查（在实际应用中可能需要更复杂的HTML解析）
        return responseBody.contains(validationSelector);
    }
    
    /**
     * 从HTTP响应中提取新的Cookie
     */
    private String extractNewCookiesFromResponse(HttpResponse<String> response, String oldCookie) {
        // 获取所有Set-Cookie头
        var setCookieHeaders = response.headers().allValues("set-cookie");
        
        if (setCookieHeaders.isEmpty()) {
            logger.debug("响应中没有Set-Cookie头");
            return null;
        }
        
        // 解析旧Cookie为Map便于比较
        var oldCookieMap = parseCookieString(oldCookie);
        
        StringBuilder newCookieBuilder = new StringBuilder();
        boolean hasNewCookie = false;
        
        // 处理Set-Cookie头
        for (String setCookieHeader : setCookieHeaders) {
            String[] cookieParts = setCookieHeader.split(";");
            if (cookieParts.length > 0) {
                String[] nameValue = cookieParts[0].split("=", 2);
                if (nameValue.length == 2) {
                    String cookieName = nameValue[0].trim();
                    String cookieValue = nameValue[1].trim();
                    
                    // 检查是否是新的或更新的Cookie
                    if (!cookieValue.equals(oldCookieMap.get(cookieName))) {
                        if (newCookieBuilder.length() > 0) {
                            newCookieBuilder.append("; ");
                        }
                        newCookieBuilder.append(cookieName).append("=").append(cookieValue);
                        hasNewCookie = true;
                    }
                }
            }
        }
        
        // 如果没有新Cookie，返回null
        if (!hasNewCookie) {
            return null;
        }
        
        // 合并旧Cookie中仍然有效的部分
        for (var entry : oldCookieMap.entrySet()) {
            String cookieName = entry.getKey();
            String cookieValue = entry.getValue();
            
            // 如果这个Cookie没有被新的覆盖，则保留
            if (!newCookieBuilder.toString().contains(cookieName + "=")) {
                if (newCookieBuilder.length() > 0) {
                    newCookieBuilder.append("; ");
                }
                newCookieBuilder.append(cookieName).append("=").append(cookieValue);
            }
        }
        
        return newCookieBuilder.toString();
    }
    
    /**
     * 解析Cookie字符串为Map
     */
    private java.util.Map<String, String> parseCookieString(String cookieString) {
        if (cookieString == null || cookieString.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        return Arrays.stream(cookieString.split(";"))
                .map(String::trim)
                .filter(cookie -> cookie.contains("="))
                .map(cookie -> cookie.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                    parts -> parts[0].trim(),
                    parts -> parts[1].trim(),
                    (existing, replacement) -> replacement // 如果有重复的key，使用新值
                ));
    }
    
    /**
     * 执行简单的连通性测试
     */
    public boolean testConnectivity() {
        try {
            String testUrl = config.getTargetUrl();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(testUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            boolean success = response.statusCode() < 500; // 5xx表示服务器错误
            logger.info("连通性测试结果: {}, 状态码: {}", success ? "成功" : "失败", response.statusCode());
            
            return success;
            
        } catch (Exception e) {
            logger.error("连通性测试失败", e);
            return false;
        }
    }
}