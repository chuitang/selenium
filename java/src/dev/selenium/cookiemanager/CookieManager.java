package dev.selenium.cookiemanager;

import dev.selenium.cookiemanager.config.Config;
import dev.selenium.cookiemanager.database.CookieDatabase;
import dev.selenium.cookiemanager.login.AutoLogin;
import dev.selenium.cookiemanager.validation.CookieValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 主要的Cookie管理工具类
 * 负责协调自动登录、Cookie验证和数据库操作
 */
public class CookieManager {
    private static final Logger logger = LoggerFactory.getLogger(CookieManager.class);
    
    private final Config config;
    private final CookieDatabase database;
    private final AutoLogin autoLogin;
    private final CookieValidator validator;
    private final ScheduledExecutorService scheduler;
    
    public CookieManager(Config config) {
        this.config = config;
        this.database = new CookieDatabase(config.getDatabaseUrl(), 
                                         config.getDatabaseUsername(), 
                                         config.getDatabasePassword());
        this.autoLogin = new AutoLogin(config);
        this.validator = new CookieValidator(config);
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // 初始化数据库
        database.initializeDatabase();
    }
    
    /**
     * 启动Cookie管理服务
     */
    public void start() {
        logger.info("启动Cookie管理服务...");
        
        // 检查是否存在有效的Cookie，如果没有则立即执行登录
        String existingCookie = database.getLatestCookie();
        if (existingCookie == null || !validator.isValidCookie(existingCookie)) {
            logger.info("未找到有效Cookie，执行初始登录...");
            performLogin();
        }
        
        // 启动定时检查任务
        long checkInterval = config.getCookieCheckIntervalMinutes();
        scheduler.scheduleAtFixedRate(this::checkAndRefreshCookie, 
                                    checkInterval, 
                                    checkInterval, 
                                    TimeUnit.MINUTES);
        
        logger.info("Cookie管理服务已启动，检查间隔: {} 分钟", checkInterval);
    }
    
    /**
     * 停止Cookie管理服务
     */
    public void stop() {
        logger.info("停止Cookie管理服务...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        database.close();
        logger.info("Cookie管理服务已停止");
    }
    
    /**
     * 检查并刷新Cookie的核心逻辑
     */
    private void checkAndRefreshCookie() {
        try {
            logger.info("开始检查Cookie有效性...");
            
            String currentCookie = database.getLatestCookie();
            if (currentCookie == null) {
                logger.warn("数据库中没有找到Cookie，执行登录...");
                performLogin();
                return;
            }
            
            // 验证当前Cookie是否有效
            if (validator.isValidCookie(currentCookie)) {
                logger.info("当前Cookie仍然有效");
                return;
            }
            
            logger.warn("当前Cookie已失效，尝试刷新...");
            
            // 尝试携带旧Cookie访问网站获取新Cookie
            String newCookie = validator.tryRefreshCookie(currentCookie);
            
            if (newCookie != null && validator.isValidCookie(newCookie)) {
                logger.info("成功通过刷新获取新Cookie");
                database.saveCookie(newCookie);
            } else {
                logger.warn("Cookie刷新失败，执行重新登录...");
                performLogin();
            }
            
        } catch (Exception e) {
            logger.error("检查Cookie过程中发生错误", e);
            // 发生错误时尝试重新登录
            try {
                performLogin();
            } catch (Exception loginException) {
                logger.error("重新登录也失败了", loginException);
            }
        }
    }
    
    /**
     * 执行登录操作
     */
    private void performLogin() {
        try {
            logger.info("开始执行登录操作...");
            String newCookie = autoLogin.login();
            
            if (newCookie != null && validator.isValidCookie(newCookie)) {
                database.saveCookie(newCookie);
                logger.info("登录成功，新Cookie已保存到数据库");
            } else {
                logger.error("登录失败或获取的Cookie无效");
                throw new RuntimeException("登录失败");
            }
        } catch (Exception e) {
            logger.error("登录过程中发生错误", e);
            throw new RuntimeException("登录失败", e);
        }
    }
    
    /**
     * 获取当前有效的Cookie
     */
    public String getCurrentCookie() {
        return database.getLatestCookie();
    }
    
    /**
     * 手动触发Cookie检查
     */
    public void forceCheckCookie() {
        logger.info("手动触发Cookie检查...");
        checkAndRefreshCookie();
    }
    
    /**
     * 手动触发登录
     */
    public void forceLogin() {
        logger.info("手动触发登录...");
        performLogin();
    }
}