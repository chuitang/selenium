package dev.selenium.cookiemanager;

import dev.selenium.cookiemanager.config.Config;
import dev.selenium.cookiemanager.database.CookieDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cookie管理工具测试类
 */
public class CookieManagerTest {
    
    private Config testConfig;
    private CookieDatabase testDatabase;
    
    @BeforeEach
    public void setUp() {
        // 创建测试配置
        Properties props = new Properties();
        props.setProperty("target.url", "https://httpbin.org/");
        props.setProperty("login.url", "https://httpbin.org/forms/post");
        props.setProperty("login.username", "test_user");
        props.setProperty("login.password", "test_password");
        props.setProperty("database.url", "jdbc:h2:mem:testdb");
        props.setProperty("database.username", "sa");
        props.setProperty("database.password", "");
        props.setProperty("cookie.check.interval.minutes", "1");
        props.setProperty("webdriver.headless", "true");
        
        testConfig = new Config(props);
        testDatabase = new CookieDatabase(
            testConfig.getDatabaseUrl(),
            testConfig.getDatabaseUsername(),
            testConfig.getDatabasePassword()
        );
    }
    
    @AfterEach
    public void tearDown() {
        if (testDatabase != null) {
            testDatabase.close();
        }
    }
    
    @Test
    public void testConfigCreation() {
        assertNotNull(testConfig);
        assertEquals("https://httpbin.org/", testConfig.getTargetUrl());
        assertEquals("https://httpbin.org/forms/post", testConfig.getLoginUrl());
        assertEquals("test_user", testConfig.getUsername());
        assertEquals("test_password", testConfig.getPassword());
        assertTrue(testConfig.isHeadlessMode());
    }
    
    @Test
    public void testConfigValidation() {
        assertDoesNotThrow(() -> testConfig.validate());
    }
    
    @Test
    public void testDatabaseInitialization() {
        assertDoesNotThrow(() -> testDatabase.initializeDatabase());
        assertTrue(testDatabase.testConnection());
    }
    
    @Test
    public void testCookieSaveAndRetrieve() {
        testDatabase.initializeDatabase();
        
        String testCookie = "sessionid=abc123; csrftoken=xyz789";
        testDatabase.saveCookie(testCookie);
        
        String retrievedCookie = testDatabase.getLatestCookie();
        assertEquals(testCookie, retrievedCookie);
    }
    
    @Test
    public void testCookieHistory() {
        testDatabase.initializeDatabase();
        
        // 保存多个Cookie
        testDatabase.saveCookie("cookie1=value1", "https://example1.com", "测试1");
        testDatabase.saveCookie("cookie2=value2", "https://example2.com", "测试2");
        testDatabase.saveCookie("cookie3=value3", "https://example3.com", "测试3");
        
        // 检查历史记录
        var allCookies = testDatabase.getAllCookies();
        assertEquals(3, allCookies.size());
        
        // 检查活跃Cookie
        var activeCookies = testDatabase.getActiveCookies();
        assertEquals(1, activeCookies.size());
        assertEquals("cookie3=value3", activeCookies.get(0).getCookieValue());
    }
    
    @Test
    public void testCookieValidator() {
        dev.selenium.cookiemanager.validation.CookieValidator validator = 
            new dev.selenium.cookiemanager.validation.CookieValidator(testConfig);
        
        assertNotNull(validator);
        
        // 测试连通性
        boolean connectivity = validator.testConnectivity();
        // 连通性测试可能因网络环境而失败，所以这里只记录结果
        System.out.println("连通性测试结果: " + connectivity);
    }
    
    @Test
    public void testInvalidCookie() {
        dev.selenium.cookiemanager.validation.CookieValidator validator = 
            new dev.selenium.cookiemanager.validation.CookieValidator(testConfig);
        
        // 测试空Cookie
        assertFalse(validator.isValidCookie(null));
        assertFalse(validator.isValidCookie(""));
        assertFalse(validator.isValidCookie("   "));
    }
    
    @Test 
    public void testCookieManagerCreation() {
        // 测试CookieManager是否能正常创建
        assertDoesNotThrow(() -> {
            CookieManager manager = new CookieManager(testConfig);
            assertNotNull(manager);
        });
    }
}