package dev.selenium.cookiemanager;

import dev.selenium.cookiemanager.config.Config;
import dev.selenium.cookiemanager.database.CookieDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Cookie管理工具主应用程序
 * 提供命令行界面和服务管理功能
 */
public class CookieManagerApplication {
    private static final Logger logger = LoggerFactory.getLogger(CookieManagerApplication.class);
    
    private CookieManager cookieManager;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    Cookie管理工具 v1.0 (JDK 21 + Windows)");
        System.out.println("=================================================");
        
        CookieManagerApplication app = new CookieManagerApplication();
        
        try {
            if (args.length > 0) {
                // 命令行模式
                app.runCommandLine(args);
            } else {
                // 交互模式
                app.runInteractiveMode();
            }
        } catch (Exception e) {
            logger.error("应用程序运行失败", e);
            System.err.println("错误: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * 命令行模式
     */
    private void runCommandLine(String[] args) {
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "start" -> startService(args);
            case "test" -> testConfiguration(args);
            case "login" -> performManualLogin(args);
            case "validate" -> validateCurrentCookie(args);
            case "help" -> printHelp();
            default -> {
                System.err.println("未知命令: " + command);
                printHelp();
                System.exit(1);
            }
        }
    }
    
    /**
     * 交互模式
     */
    private void runInteractiveMode() {
        System.out.println("进入交互模式...");
        System.out.println("请先配置基本参数，或输入 'help' 查看帮助");
        
        Scanner scanner = new Scanner(System.in);
        Config config = null;
        
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            
            try {
                switch (command) {
                    case "config" -> config = configureInteractively(scanner);
                    case "start" -> {
                        if (config == null) {
                            System.out.println("请先使用 'config' 命令配置参数");
                        } else {
                            startServiceInteractive(config);
                        }
                    }
                    case "stop" -> stopService();
                    case "status" -> showStatus();
                    case "test" -> {
                        if (config == null) {
                            System.out.println("请先使用 'config' 命令配置参数");
                        } else {
                            testConfigurationInteractive(config);
                        }
                    }
                    case "login" -> {
                        if (config == null) {
                            System.out.println("请先使用 'config' 命令配置参数");
                        } else {
                            performManualLoginInteractive(config);
                        }
                    }
                    case "validate" -> {
                        if (config == null) {
                            System.out.println("请先使用 'config' 命令配置参数");
                        } else {
                            validateCurrentCookieInteractive(config);
                        }
                    }
                    case "cookies" -> {
                        if (config == null) {
                            System.out.println("请先使用 'config' 命令配置参数");
                        } else {
                            showCookieHistory(config);
                        }
                    }
                    case "help" -> printInteractiveHelp();
                    case "exit", "quit" -> {
                        stopService();
                        System.out.println("再见！");
                        return;
                    }
                    default -> System.out.println("未知命令: " + command + "，输入 'help' 查看帮助");
                }
            } catch (Exception e) {
                logger.error("执行命令失败: " + command, e);
                System.err.println("命令执行失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 交互式配置
     */
    private Config configureInteractively(Scanner scanner) {
        System.out.println("\n=== 配置Cookie管理工具 ===");
        
        Properties props = new Properties();
        
        System.out.print("目标网站URL: ");
        props.setProperty("target.url", scanner.nextLine().trim());
        
        System.out.print("登录页面URL: ");
        props.setProperty("login.url", scanner.nextLine().trim());
        
        System.out.print("用户名: ");
        props.setProperty("login.username", scanner.nextLine().trim());
        
        System.out.print("密码: ");
        props.setProperty("login.password", scanner.nextLine().trim());
        
        System.out.print("用户名输入框选择器 (默认: username): ");
        String usernameField = scanner.nextLine().trim();
        if (!usernameField.isEmpty()) {
            props.setProperty("login.username.field", usernameField);
        }
        
        System.out.print("密码输入框选择器 (默认: password): ");
        String passwordField = scanner.nextLine().trim();
        if (!passwordField.isEmpty()) {
            props.setProperty("login.password.field", passwordField);
        }
        
        System.out.print("登录按钮选择器 (默认: input[type='submit']): ");
        String loginButton = scanner.nextLine().trim();
        if (!loginButton.isEmpty()) {
            props.setProperty("login.button.selector", loginButton);
        }
        
        System.out.print("Cookie检查间隔(分钟，默认30): ");
        String interval = scanner.nextLine().trim();
        if (!interval.isEmpty()) {
            props.setProperty("cookie.check.interval.minutes", interval);
        }
        
        System.out.print("是否使用无头模式? (y/N): ");
        String headless = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(headless) || "yes".equals(headless)) {
            props.setProperty("webdriver.headless", "true");
        } else {
            props.setProperty("webdriver.headless", "false");
        }
        
        try {
            Config config = new Config(props);
            config.validate();
            System.out.println("配置验证成功！");
            return config;
        } catch (Exception e) {
            System.err.println("配置验证失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 启动服务（命令行）
     */
    private void startService(String[] args) {
        Config config = loadConfigFromArgs(args);
        if (config == null) {
            System.exit(1);
        }
        
        startServiceInteractive(config);
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在关闭服务...");
            stopService();
        }));
        
        // 等待关闭信号
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 启动服务（交互式）
     */
    private void startServiceInteractive(Config config) {
        if (cookieManager != null) {
            System.out.println("服务已在运行中");
            return;
        }
        
        try {
            cookieManager = new CookieManager(config);
            cookieManager.start();
            System.out.println("Cookie管理服务已启动");
        } catch (Exception e) {
            logger.error("启动服务失败", e);
            System.err.println("启动服务失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止服务
     */
    private void stopService() {
        if (cookieManager != null) {
            cookieManager.stop();
            cookieManager = null;
            System.out.println("服务已停止");
        }
        shutdownLatch.countDown();
    }
    
    /**
     * 显示服务状态
     */
    private void showStatus() {
        if (cookieManager == null) {
            System.out.println("服务状态: 未运行");
        } else {
            System.out.println("服务状态: 运行中");
            String currentCookie = cookieManager.getCurrentCookie();
            if (currentCookie != null) {
                System.out.println("当前Cookie长度: " + currentCookie.length() + " 字符");
                System.out.println("Cookie预览: " + (currentCookie.length() > 100 ? 
                    currentCookie.substring(0, 100) + "..." : currentCookie));
            } else {
                System.out.println("当前没有有效的Cookie");
            }
        }
    }
    
    /**
     * 测试配置
     */
    private void testConfiguration(String[] args) {
        Config config = loadConfigFromArgs(args);
        if (config != null) {
            testConfigurationInteractive(config);
        }
    }
    
    /**
     * 测试配置（交互式）
     */
    private void testConfigurationInteractive(Config config) {
        System.out.println("测试配置...");
        
        try {
            // 测试数据库连接
            System.out.print("测试数据库连接... ");
            CookieDatabase db = new CookieDatabase(
                config.getDatabaseUrl(),
                config.getDatabaseUsername(),
                config.getDatabasePassword()
            );
            if (db.testConnection()) {
                System.out.println("✓ 成功");
            } else {
                System.out.println("✗ 失败");
            }
            
            // 测试网络连通性
            System.out.print("测试网络连通性... ");
            dev.selenium.cookiemanager.validation.CookieValidator validator = 
                new dev.selenium.cookiemanager.validation.CookieValidator(config);
            if (validator.testConnectivity()) {
                System.out.println("✓ 成功");
            } else {
                System.out.println("✗ 失败");
            }
            
            // 测试登录页面元素
            System.out.print("测试登录页面元素... ");
            dev.selenium.cookiemanager.login.AutoLogin autoLogin = 
                new dev.selenium.cookiemanager.login.AutoLogin(config);
            if (autoLogin.validateLoginCredentials()) {
                System.out.println("✓ 成功");
            } else {
                System.out.println("✗ 失败");
            }
            
        } catch (Exception e) {
            logger.error("配置测试失败", e);
            System.err.println("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行手动登录
     */
    private void performManualLogin(String[] args) {
        Config config = loadConfigFromArgs(args);
        if (config != null) {
            performManualLoginInteractive(config);
        }
    }
    
    /**
     * 执行手动登录（交互式）
     */
    private void performManualLoginInteractive(Config config) {
        System.out.println("执行手动登录...");
        
        try {
            dev.selenium.cookiemanager.login.AutoLogin autoLogin = 
                new dev.selenium.cookiemanager.login.AutoLogin(config);
            String cookie = autoLogin.login();
            
            if (cookie != null) {
                System.out.println("登录成功！");
                System.out.println("Cookie长度: " + cookie.length() + " 字符");
                
                // 保存到数据库
                CookieDatabase db = new CookieDatabase(
                    config.getDatabaseUrl(),
                    config.getDatabaseUsername(),
                    config.getDatabasePassword()
                );
                db.initializeDatabase();
                db.saveCookie(cookie, config.getTargetUrl(), "手动登录");
                System.out.println("Cookie已保存到数据库");
            } else {
                System.out.println("登录失败！");
            }
        } catch (Exception e) {
            logger.error("手动登录失败", e);
            System.err.println("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证当前Cookie
     */
    private void validateCurrentCookie(String[] args) {
        Config config = loadConfigFromArgs(args);
        if (config != null) {
            validateCurrentCookieInteractive(config);
        }
    }
    
    /**
     * 验证当前Cookie（交互式）
     */
    private void validateCurrentCookieInteractive(Config config) {
        System.out.println("验证当前Cookie...");
        
        try {
            CookieDatabase db = new CookieDatabase(
                config.getDatabaseUrl(),
                config.getDatabaseUsername(),
                config.getDatabasePassword()
            );
            db.initializeDatabase();
            
            String currentCookie = db.getLatestCookie();
            if (currentCookie == null) {
                System.out.println("数据库中没有找到Cookie");
                return;
            }
            
            dev.selenium.cookiemanager.validation.CookieValidator validator = 
                new dev.selenium.cookiemanager.validation.CookieValidator(config);
            
            boolean isValid = validator.isValidCookie(currentCookie);
            System.out.println("Cookie验证结果: " + (isValid ? "有效" : "无效"));
            
        } catch (Exception e) {
            logger.error("Cookie验证失败", e);
            System.err.println("验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示Cookie历史记录
     */
    private void showCookieHistory(Config config) {
        System.out.println("Cookie历史记录:");
        
        try {
            CookieDatabase db = new CookieDatabase(
                config.getDatabaseUrl(),
                config.getDatabaseUsername(),
                config.getDatabasePassword()
            );
            db.initializeDatabase();
            
            var cookies = db.getAllCookies();
            if (cookies.isEmpty()) {
                System.out.println("没有找到Cookie记录");
            } else {
                System.out.println("找到 " + cookies.size() + " 条记录:");
                for (var cookie : cookies) {
                    System.out.println("  " + cookie);
                }
            }
        } catch (Exception e) {
            logger.error("获取Cookie历史失败", e);
            System.err.println("获取历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 从命令行参数加载配置
     */
    private Config loadConfigFromArgs(String[] args) {
        // 这里可以从配置文件或命令行参数加载配置
        // 为简化，使用默认配置
        try {
            return new Config();
        } catch (Exception e) {
            System.err.println("加载配置失败: " + e.getMessage());
            System.err.println("请确保配置文件 config.properties 存在");
            return null;
        }
    }
    
    /**
     * 打印帮助信息
     */
    private void printHelp() {
        System.out.println("Cookie管理工具使用说明:");
        System.out.println("  java -jar cookie-manager.jar start    - 启动服务");
        System.out.println("  java -jar cookie-manager.jar test     - 测试配置");
        System.out.println("  java -jar cookie-manager.jar login    - 手动登录");
        System.out.println("  java -jar cookie-manager.jar validate - 验证Cookie");
        System.out.println("  java -jar cookie-manager.jar help     - 显示帮助");
        System.out.println("");
        System.out.println("交互模式: 直接运行jar文件不带参数");
    }
    
    /**
     * 打印交互模式帮助信息
     */
    private void printInteractiveHelp() {
        System.out.println("交互模式命令:");
        System.out.println("  config   - 配置工具参数");
        System.out.println("  start    - 启动服务");
        System.out.println("  stop     - 停止服务");
        System.out.println("  status   - 查看服务状态");
        System.out.println("  test     - 测试配置");
        System.out.println("  login    - 手动执行登录");
        System.out.println("  validate - 验证当前Cookie");
        System.out.println("  cookies  - 查看Cookie历史");
        System.out.println("  help     - 显示帮助");
        System.out.println("  exit     - 退出程序");
    }
}