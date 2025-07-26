package dev.selenium.cookiemanager.login;

import dev.selenium.cookiemanager.config.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;

/**
 * 自动登录类
 * 负责使用Selenium WebDriver自动执行网站登录
 */
public class AutoLogin {
    private static final Logger logger = LoggerFactory.getLogger(AutoLogin.class);
    
    private final Config config;
    
    public AutoLogin(Config config) {
        this.config = config;
    }
    
    /**
     * 执行自动登录并返回Cookie
     */
    public String login() {
        WebDriver driver = null;
        try {
            logger.info("开始自动登录流程...");
            
            // 创建WebDriver实例
            driver = createWebDriver();
            
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeoutSeconds()));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getImplicitWaitSeconds()));
            
            // 访问登录页面
            logger.info("访问登录页面: {}", config.getLoginUrl());
            driver.get(config.getLoginUrl());
            
            // 等待页面加载完成
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // 查找并填写用户名
            logger.info("查找用户名输入框...");
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
                getElementLocator(config.getUsernameField())
            ));
            usernameField.clear();
            usernameField.sendKeys(config.getUsername());
            logger.info("用户名已填写");
            
            // 查找并填写密码
            logger.info("查找密码输入框...");
            WebElement passwordField = driver.findElement(getElementLocator(config.getPasswordField()));
            passwordField.clear();
            passwordField.sendKeys(config.getPassword());
            logger.info("密码已填写");
            
            // 提交登录表单
            logger.info("提交登录表单...");
            WebElement loginButton = driver.findElement(getElementLocator(config.getLoginButtonSelector()));
            loginButton.click();
            
            // 等待登录完成（可以通过URL变化或特定元素出现来判断）
            waitForLoginCompletion(driver, wait);
            
            // 获取Cookie
            String cookieString = extractCookies(driver);
            
            logger.info("自动登录成功，Cookie已获取");
            return cookieString;
            
        } catch (Exception e) {
            logger.error("自动登录失败", e);
            throw new RuntimeException("自动登录失败", e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("关闭WebDriver时发生异常", e);
                }
            }
        }
    }
    
    /**
     * 创建WebDriver实例
     */
    private WebDriver createWebDriver() {
        try {
            // 使用WebDriverManager自动管理ChromeDriver
            if (config.getWebDriverPath() != null && !config.getWebDriverPath().trim().isEmpty()) {
                // 如果指定了驱动路径，使用指定路径
                System.setProperty("webdriver.chrome.driver", config.getWebDriverPath());
                logger.info("使用指定的ChromeDriver路径: {}", config.getWebDriverPath());
            } else {
                // 自动下载和管理ChromeDriver
                WebDriverManager.chromedriver().setup();
                logger.info("使用WebDriverManager自动管理ChromeDriver");
            }
            
            ChromeOptions options = new ChromeOptions();
            
            // 设置Chrome选项
            if (config.isHeadlessMode()) {
                options.addArguments("--headless");
                logger.info("使用无头模式启动Chrome");
            }
            
            // 添加其他Chrome选项以提高稳定性
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            
            // 设置实验性选项
            options.setExperimentalOption("useAutomationExtension", false);
            options.setExperimentalOption("excludeSwitches", java.util.Arrays.asList("enable-automation"));
            
            WebDriver driver = new ChromeDriver(options);
            
            // 执行JavaScript来隐藏webdriver属性
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
            );
            
            logger.info("Chrome WebDriver已创建");
            return driver;
            
        } catch (Exception e) {
            logger.error("创建WebDriver失败，请确保Chrome浏览器已正确安装", e);
            throw new RuntimeException("创建WebDriver失败", e);
        }
    }
    
    /**
     * 根据选择器字符串创建By定位器
     */
    private By getElementLocator(String selector) {
        if (selector.startsWith("#")) {
            // ID选择器
            return By.id(selector.substring(1));
        } else if (selector.startsWith(".")) {
            // Class选择器
            return By.className(selector.substring(1));
        } else if (selector.startsWith("//")) {
            // XPath选择器
            return By.xpath(selector);
        } else if (selector.contains("[") && selector.contains("]")) {
            // CSS选择器
            return By.cssSelector(selector);
        } else {
            // 默认作为name属性
            return By.name(selector);
        }
    }
    
    /**
     * 等待登录完成
     */
    private void waitForLoginCompletion(WebDriver driver, WebDriverWait wait) {
        try {
            // 等待URL变化（通常登录成功后会跳转到其他页面）
            String loginUrl = config.getLoginUrl();
            wait.until(driver1 -> !driver1.getCurrentUrl().equals(loginUrl));
            
            logger.info("登录完成，当前URL: {}", driver.getCurrentUrl());
            
            // 额外的等待时间确保页面完全加载
            Thread.sleep(2000);
            
        } catch (Exception e) {
            logger.warn("等待登录完成时发生异常: {}", e.getMessage());
            // 如果URL没有变化，尝试等待一段时间
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 提取所有Cookie并格式化为字符串
     */
    private String extractCookies(WebDriver driver) {
        try {
            Set<org.openqa.selenium.Cookie> cookies = driver.manage().getCookies();
            
            if (cookies.isEmpty()) {
                logger.warn("没有找到任何Cookie");
                return null;
            }
            
            StringBuilder cookieBuilder = new StringBuilder();
            for (org.openqa.selenium.Cookie cookie : cookies) {
                if (cookieBuilder.length() > 0) {
                    cookieBuilder.append("; ");
                }
                cookieBuilder.append(cookie.getName()).append("=").append(cookie.getValue());
            }
            
            String cookieString = cookieBuilder.toString();
            logger.info("提取到 {} 个Cookie，总长度: {} 字符", cookies.size(), cookieString.length());
            
            return cookieString;
            
        } catch (Exception e) {
            logger.error("提取Cookie失败", e);
            throw new RuntimeException("提取Cookie失败", e);
        }
    }
    
    /**
     * 手动登录验证（用于测试）
     */
    public boolean validateLoginCredentials() {
        WebDriver driver = null;
        try {
            logger.info("验证登录凭据...");
            
            driver = createWebDriver();
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeoutSeconds()));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getImplicitWaitSeconds()));
            
            driver.get(config.getLoginUrl());
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // 尝试找到登录元素
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
                getElementLocator(config.getUsernameField())
            ));
            WebElement passwordField = driver.findElement(getElementLocator(config.getPasswordField()));
            WebElement loginButton = driver.findElement(getElementLocator(config.getLoginButtonSelector()));
            
            logger.info("登录页面元素验证成功");
            return true;
            
        } catch (Exception e) {
            logger.error("登录凭据验证失败", e);
            return false;
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("关闭WebDriver时发生异常", e);
                }
            }
        }
    }
}