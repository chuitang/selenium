package dev.selenium.cookiemanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置管理类
 * 负责加载和管理所有配置参数
 */
public class Config {
    private final Properties properties;
    
    // 网站相关配置
    private final String targetUrl;
    private final String loginUrl;
    private final String usernameField;
    private final String passwordField;
    private final String username;
    private final String password;
    private final String loginButtonSelector;
    private final String validationUrl;
    private final String validationSelector;
    
    // 数据库配置
    private final String databaseUrl;
    private final String databaseUsername;
    private final String databasePassword;
    
    // Cookie检查配置
    private final long cookieCheckIntervalMinutes;
    private final int maxRetryAttempts;
    private final long retryDelaySeconds;
    
    // WebDriver配置
    private final String webDriverPath;
    private final boolean headlessMode;
    private final int pageLoadTimeoutSeconds;
    private final int implicitWaitSeconds;
    
    public Config() {
        this.properties = new Properties();
        loadDefaultProperties();
        loadFromFile();
        
        // 初始化所有字段
        this.targetUrl = getPropertySafe("target.url");
        this.loginUrl = getPropertySafe("login.url");
        this.usernameField = getProperty("login.username.field", "username");
        this.passwordField = getProperty("login.password.field", "password");
        this.username = getPropertySafe("login.username");
        this.password = getPropertySafe("login.password");
        this.loginButtonSelector = getProperty("login.button.selector", "input[type='submit']");
        this.validationUrl = getProperty("validation.url", null);
        this.validationSelector = getProperty("validation.selector", null);
        
        this.databaseUrl = getProperty("database.url");
        this.databaseUsername = getProperty("database.username");
        this.databasePassword = getProperty("database.password");
        
        this.cookieCheckIntervalMinutes = Long.parseLong(getProperty("cookie.check.interval.minutes"));
        this.maxRetryAttempts = Integer.parseInt(getProperty("cookie.max.retry.attempts"));
        this.retryDelaySeconds = Long.parseLong(getProperty("cookie.retry.delay.seconds"));
        
        this.webDriverPath = getProperty("webdriver.path", null);
        this.headlessMode = Boolean.parseBoolean(getProperty("webdriver.headless"));
        this.pageLoadTimeoutSeconds = Integer.parseInt(getProperty("webdriver.page.load.timeout.seconds"));
        this.implicitWaitSeconds = Integer.parseInt(getProperty("webdriver.implicit.wait.seconds"));
    }
    
    public Config(String configFilePath) {
        this.properties = new Properties();
        loadDefaultProperties();
        loadFromFile(configFilePath);
        
        // 初始化所有字段
        this.targetUrl = getPropertySafe("target.url");
        this.loginUrl = getPropertySafe("login.url");
        this.usernameField = getProperty("login.username.field", "username");
        this.passwordField = getProperty("login.password.field", "password");
        this.username = getPropertySafe("login.username");
        this.password = getPropertySafe("login.password");
        this.loginButtonSelector = getProperty("login.button.selector", "input[type='submit']");
        this.validationUrl = getProperty("validation.url", null);
        this.validationSelector = getProperty("validation.selector", null);
        
        this.databaseUrl = getProperty("database.url");
        this.databaseUsername = getProperty("database.username");
        this.databasePassword = getProperty("database.password");
        
        this.cookieCheckIntervalMinutes = Long.parseLong(getProperty("cookie.check.interval.minutes"));
        this.maxRetryAttempts = Integer.parseInt(getProperty("cookie.max.retry.attempts"));
        this.retryDelaySeconds = Long.parseLong(getProperty("cookie.retry.delay.seconds"));
        
        this.webDriverPath = getProperty("webdriver.path", null);
        this.headlessMode = Boolean.parseBoolean(getProperty("webdriver.headless"));
        this.pageLoadTimeoutSeconds = Integer.parseInt(getProperty("webdriver.page.load.timeout.seconds"));
        this.implicitWaitSeconds = Integer.parseInt(getProperty("webdriver.implicit.wait.seconds"));
    }
    
    private void loadDefaultProperties() {
        // 设置默认值
        properties.setProperty("cookie.check.interval.minutes", "30");
        properties.setProperty("cookie.max.retry.attempts", "3");
        properties.setProperty("cookie.retry.delay.seconds", "10");
        properties.setProperty("webdriver.headless", "true");
        properties.setProperty("webdriver.page.load.timeout.seconds", "30");
        properties.setProperty("webdriver.implicit.wait.seconds", "10");
        
        // 数据库默认配置 (H2内存数据库)
        properties.setProperty("database.url", "jdbc:h2:./cookiedb;AUTO_SERVER=TRUE");
        properties.setProperty("database.username", "sa");
        properties.setProperty("database.password", "");
    }
    
    private void loadFromFile() {
        loadFromFile("config.properties");
    }
    
    private void loadFromFile(String filename) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
                System.out.println("配置文件已加载: " + filename);
            } else {
                System.out.println("配置文件未找到: " + filename + "，使用默认配置");
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
        }
    }
    
    public Config(Properties properties) {
        this.properties = new Properties();
        loadDefaultProperties();
        this.properties.putAll(properties);
        
        // 初始化所有字段
        this.targetUrl = getPropertySafe("target.url");
        this.loginUrl = getPropertySafe("login.url");
        this.usernameField = getProperty("login.username.field", "username");
        this.passwordField = getProperty("login.password.field", "password");
        this.username = getPropertySafe("login.username");
        this.password = getPropertySafe("login.password");
        this.loginButtonSelector = getProperty("login.button.selector", "input[type='submit']");
        this.validationUrl = getProperty("validation.url", null);
        this.validationSelector = getProperty("validation.selector", null);
        
        this.databaseUrl = getProperty("database.url");
        this.databaseUsername = getProperty("database.username");
        this.databasePassword = getProperty("database.password");
        
        this.cookieCheckIntervalMinutes = Long.parseLong(getProperty("cookie.check.interval.minutes"));
        this.maxRetryAttempts = Integer.parseInt(getProperty("cookie.max.retry.attempts"));
        this.retryDelaySeconds = Long.parseLong(getProperty("cookie.retry.delay.seconds"));
        
        this.webDriverPath = getProperty("webdriver.path", null);
        this.headlessMode = Boolean.parseBoolean(getProperty("webdriver.headless"));
        this.pageLoadTimeoutSeconds = Integer.parseInt(getProperty("webdriver.page.load.timeout.seconds"));
        this.implicitWaitSeconds = Integer.parseInt(getProperty("webdriver.implicit.wait.seconds"));
    }
    
    private String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("缺少必需的配置项: " + key);
        }
        return value;
    }
    
    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    private String getPropertySafe(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("缺少必需的配置项: " + key);
        }
        return value;
    }
    
    // Getters
    public String getTargetUrl() { return targetUrl; }
    public String getLoginUrl() { return loginUrl; }
    public String getUsernameField() { return usernameField; }
    public String getPasswordField() { return passwordField; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getLoginButtonSelector() { return loginButtonSelector; }
    public String getValidationUrl() { return validationUrl; }
    public String getValidationSelector() { return validationSelector; }
    
    public String getDatabaseUrl() { return databaseUrl; }
    public String getDatabaseUsername() { return databaseUsername; }
    public String getDatabasePassword() { return databasePassword; }
    
    public long getCookieCheckIntervalMinutes() { return cookieCheckIntervalMinutes; }
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public long getRetryDelaySeconds() { return retryDelaySeconds; }
    
    public String getWebDriverPath() { return webDriverPath; }
    public boolean isHeadlessMode() { return headlessMode; }
    public int getPageLoadTimeoutSeconds() { return pageLoadTimeoutSeconds; }
    public int getImplicitWaitSeconds() { return implicitWaitSeconds; }
    
    /**
     * 验证配置的完整性
     */
    public void validate() {
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("target.url 不能为空");
        }
        if (loginUrl == null || loginUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("login.url 不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("login.username 不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("login.password 不能为空");
        }
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("database.url 不能为空");
        }
        if (cookieCheckIntervalMinutes <= 0) {
            throw new IllegalArgumentException("cookie.check.interval.minutes 必须大于0");
        }
    }
    
    @Override
    public String toString() {
        return "Config{" +
                "targetUrl='" + targetUrl + '\'' +
                ", loginUrl='" + loginUrl + '\'' +
                ", usernameField='" + usernameField + '\'' +
                ", passwordField='" + passwordField + '\'' +
                ", username='" + username + '\'' +
                ", databaseUrl='" + databaseUrl + '\'' +
                ", cookieCheckIntervalMinutes=" + cookieCheckIntervalMinutes +
                ", headlessMode=" + headlessMode +
                '}';
    }
}