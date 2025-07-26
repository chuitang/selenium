package dev.selenium.cookiemanager.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Cookie数据库管理类
 * 负责Cookie的存储、检索和管理
 */
public class CookieDatabase {
    private static final Logger logger = LoggerFactory.getLogger(CookieDatabase.class);
    
    private final String databaseUrl;
    private final String username;
    private final String password;
    
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS cookies (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            cookie_value TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            is_active BOOLEAN DEFAULT TRUE,
            website_url VARCHAR(500),
            notes TEXT
        )
        """;
    
    private static final String INSERT_COOKIE_SQL = """
        INSERT INTO cookies (cookie_value, website_url, notes) 
        VALUES (?, ?, ?)
        """;
    
    private static final String UPDATE_PREVIOUS_COOKIES_SQL = """
        UPDATE cookies SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP 
        WHERE is_active = TRUE
        """;
    
    private static final String GET_LATEST_COOKIE_SQL = """
        SELECT cookie_value FROM cookies 
        WHERE is_active = TRUE 
        ORDER BY created_at DESC 
        LIMIT 1
        """;
    
    private static final String GET_ALL_COOKIES_SQL = """
        SELECT id, cookie_value, created_at, updated_at, is_active, website_url, notes 
        FROM cookies 
        ORDER BY created_at DESC
        """;
    
    private static final String GET_ACTIVE_COOKIES_SQL = """
        SELECT id, cookie_value, created_at, updated_at, is_active, website_url, notes 
        FROM cookies 
        WHERE is_active = TRUE 
        ORDER BY created_at DESC
        """;
    
    private static final String DELETE_OLD_COOKIES_SQL = """
        DELETE FROM cookies 
        WHERE is_active = FALSE 
        AND created_at < DATEADD('DAY', -?, CURRENT_TIMESTAMP)
        """;
    
    public CookieDatabase(String databaseUrl, String username, String password) {
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
    }
    
    /**
     * 初始化数据库，创建必要的表
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_TABLE_SQL);
            logger.info("数据库初始化完成");
            
        } catch (SQLException e) {
            logger.error("数据库初始化失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
    
    /**
     * 保存新的Cookie到数据库
     */
    public void saveCookie(String cookieValue) {
        saveCookie(cookieValue, null, null);
    }
    
    /**
     * 保存新的Cookie到数据库，包含网站URL和备注
     */
    public void saveCookie(String cookieValue, String websiteUrl, String notes) {
        if (cookieValue == null || cookieValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Cookie值不能为空");
        }
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 首先将所有现有的Cookie设置为非活跃状态
                try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_PREVIOUS_COOKIES_SQL)) {
                    updateStmt.executeUpdate();
                }
                
                // 插入新的Cookie
                try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_COOKIE_SQL)) {
                    insertStmt.setString(1, cookieValue);
                    insertStmt.setString(2, websiteUrl);
                    insertStmt.setString(3, notes);
                    insertStmt.executeUpdate();
                }
                
                conn.commit();
                logger.info("Cookie已成功保存到数据库");
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("保存Cookie失败", e);
            throw new RuntimeException("保存Cookie失败", e);
        }
    }
    
    /**
     * 获取最新的活跃Cookie
     */
    public String getLatestCookie() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_LATEST_COOKIE_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                String cookie = rs.getString("cookie_value");
                logger.debug("从数据库获取到最新Cookie");
                return cookie;
            }
            
            logger.warn("数据库中没有找到活跃的Cookie");
            return null;
            
        } catch (SQLException e) {
            logger.error("获取最新Cookie失败", e);
            throw new RuntimeException("获取最新Cookie失败", e);
        }
    }
    
    /**
     * 获取所有Cookie记录
     */
    public List<CookieRecord> getAllCookies() {
        return getCookies(GET_ALL_COOKIES_SQL);
    }
    
    /**
     * 获取所有活跃的Cookie记录
     */
    public List<CookieRecord> getActiveCookies() {
        return getCookies(GET_ACTIVE_COOKIES_SQL);
    }
    
    private List<CookieRecord> getCookies(String sql) {
        List<CookieRecord> cookies = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                CookieRecord record = new CookieRecord(
                    rs.getLong("id"),
                    rs.getString("cookie_value"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime(),
                    rs.getBoolean("is_active"),
                    rs.getString("website_url"),
                    rs.getString("notes")
                );
                cookies.add(record);
            }
            
        } catch (SQLException e) {
            logger.error("获取Cookie记录失败", e);
            throw new RuntimeException("获取Cookie记录失败", e);
        }
        
        return cookies;
    }
    
    /**
     * 清理过期的Cookie记录
     */
    public int cleanupOldCookies(int daysOld) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_OLD_COOKIES_SQL)) {
            
            stmt.setInt(1, daysOld);
            int deletedCount = stmt.executeUpdate();
            
            logger.info("清理了 {} 个超过 {} 天的Cookie记录", deletedCount, daysOld);
            return deletedCount;
            
        } catch (SQLException e) {
            logger.error("清理过期Cookie失败", e);
            throw new RuntimeException("清理过期Cookie失败", e);
        }
    }
    
    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        try {
            // 确保H2驱动已加载
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2数据库驱动未找到", e);
        }
        
        return DriverManager.getConnection(databaseUrl, username, password);
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("数据库连接测试成功");
            return true;
        } catch (SQLException e) {
            logger.error("数据库连接测试失败", e);
            return false;
        }
    }
    
    /**
     * 关闭数据库资源（如果需要的话）
     */
    public void close() {
        // 对于H2数据库，通常不需要显式关闭连接池
        // 但可以在这里添加任何必要的清理逻辑
        logger.info("数据库资源已清理");
    }
    
    /**
     * Cookie记录数据类
     */
    public static class CookieRecord {
        private final long id;
        private final String cookieValue;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;
        private final boolean isActive;
        private final String websiteUrl;
        private final String notes;
        
        public CookieRecord(long id, String cookieValue, LocalDateTime createdAt, 
                           LocalDateTime updatedAt, boolean isActive, String websiteUrl, String notes) {
            this.id = id;
            this.cookieValue = cookieValue;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.isActive = isActive;
            this.websiteUrl = websiteUrl;
            this.notes = notes;
        }
        
        // Getters
        public long getId() { return id; }
        public String getCookieValue() { return cookieValue; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public boolean isActive() { return isActive; }
        public String getWebsiteUrl() { return websiteUrl; }
        public String getNotes() { return notes; }
        
        @Override
        public String toString() {
            return "CookieRecord{" +
                    "id=" + id +
                    ", cookieValue='" + (cookieValue.length() > 50 ? cookieValue.substring(0, 50) + "..." : cookieValue) + '\'' +
                    ", createdAt=" + createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    ", isActive=" + isActive +
                    ", websiteUrl='" + websiteUrl + '\'' +
                    '}';
        }
    }
}