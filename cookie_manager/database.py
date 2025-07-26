import sqlite3
import json
import logging
from datetime import datetime, timedelta
from typing import Dict, List, Optional
from config import Config

class CookieDatabase:
    def __init__(self, db_path: str = None):
        self.db_path = db_path or Config.DB_PATH
        self.init_database()
    
    def init_database(self):
        """初始化数据库表"""
        try:
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS cookies (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        website_url TEXT NOT NULL,
                        cookies_json TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        is_valid BOOLEAN DEFAULT 1,
                        last_verified TIMESTAMP,
                        expiry_time TIMESTAMP,
                        user_agent TEXT,
                        notes TEXT
                    )
                ''')
                
                cursor.execute('''
                    CREATE TABLE IF NOT EXISTS login_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        website_url TEXT NOT NULL,
                        action TEXT NOT NULL,  -- login, verify, refresh, failed
                        success BOOLEAN NOT NULL,
                        message TEXT,
                        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                ''')
                
                conn.commit()
                logging.info("数据库初始化成功")
        except Exception as e:
            logging.error(f"数据库初始化失败: {e}")
            raise
    
    def save_cookies(self, website_url: str, cookies: List[Dict], user_agent: str = None, notes: str = None) -> bool:
        """保存cookies到数据库"""
        try:
            cookies_json = json.dumps(cookies)
            expiry_time = self._calculate_expiry_time(cookies)
            
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                
                # 先删除旧的cookies
                cursor.execute('DELETE FROM cookies WHERE website_url = ?', (website_url,))
                
                # 插入新的cookies
                cursor.execute('''
                    INSERT INTO cookies (website_url, cookies_json, expiry_time, user_agent, notes, last_verified)
                    VALUES (?, ?, ?, ?, ?, ?)
                ''', (website_url, cookies_json, expiry_time, user_agent, notes, datetime.now()))
                
                conn.commit()
                logging.info(f"Cookies已保存到数据库: {website_url}")
                return True
                
        except Exception as e:
            logging.error(f"保存cookies失败: {e}")
            return False
    
    def get_cookies(self, website_url: str) -> Optional[Dict]:
        """从数据库获取cookies"""
        try:
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                cursor.execute('''
                    SELECT cookies_json, created_at, updated_at, is_valid, last_verified, expiry_time, user_agent
                    FROM cookies 
                    WHERE website_url = ? AND is_valid = 1
                    ORDER BY updated_at DESC 
                    LIMIT 1
                ''', (website_url,))
                
                row = cursor.fetchone()
                if row:
                    return {
                        'cookies': json.loads(row[0]),
                        'created_at': row[1],
                        'updated_at': row[2],
                        'is_valid': bool(row[3]),
                        'last_verified': row[4],
                        'expiry_time': row[5],
                        'user_agent': row[6]
                    }
                return None
                
        except Exception as e:
            logging.error(f"获取cookies失败: {e}")
            return None
    
    def mark_cookies_invalid(self, website_url: str) -> bool:
        """标记cookies为无效"""
        try:
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                cursor.execute('''
                    UPDATE cookies 
                    SET is_valid = 0, updated_at = CURRENT_TIMESTAMP 
                    WHERE website_url = ?
                ''', (website_url,))
                conn.commit()
                logging.info(f"Cookies已标记为无效: {website_url}")
                return True
                
        except Exception as e:
            logging.error(f"标记cookies无效失败: {e}")
            return False
    
    def update_last_verified(self, website_url: str) -> bool:
        """更新最后验证时间"""
        try:
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                cursor.execute('''
                    UPDATE cookies 
                    SET last_verified = CURRENT_TIMESTAMP 
                    WHERE website_url = ?
                ''', (website_url,))
                conn.commit()
                return True
                
        except Exception as e:
            logging.error(f"更新验证时间失败: {e}")
            return False
    
    def log_action(self, website_url: str, action: str, success: bool, message: str = None) -> bool:
        """记录操作日志"""
        try:
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                cursor.execute('''
                    INSERT INTO login_logs (website_url, action, success, message)
                    VALUES (?, ?, ?, ?)
                ''', (website_url, action, success, message))
                conn.commit()
                return True
                
        except Exception as e:
            logging.error(f"记录日志失败: {e}")
            return False
    
    def get_recent_logs(self, website_url: str = None, limit: int = 50) -> List[Dict]:
        """获取最近的日志"""
        try:
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                
                if website_url:
                    cursor.execute('''
                        SELECT website_url, action, success, message, timestamp
                        FROM login_logs 
                        WHERE website_url = ?
                        ORDER BY timestamp DESC 
                        LIMIT ?
                    ''', (website_url, limit))
                else:
                    cursor.execute('''
                        SELECT website_url, action, success, message, timestamp
                        FROM login_logs 
                        ORDER BY timestamp DESC 
                        LIMIT ?
                    ''', (limit,))
                
                rows = cursor.fetchall()
                return [
                    {
                        'website_url': row[0],
                        'action': row[1],
                        'success': bool(row[2]),
                        'message': row[3],
                        'timestamp': row[4]
                    }
                    for row in rows
                ]
                
        except Exception as e:
            logging.error(f"获取日志失败: {e}")
            return []
    
    def _calculate_expiry_time(self, cookies: List[Dict]) -> Optional[str]:
        """计算cookies的过期时间"""
        try:
            min_expiry = None
            
            for cookie in cookies:
                if 'expiry' in cookie:
                    expiry = datetime.fromtimestamp(cookie['expiry'])
                    if min_expiry is None or expiry < min_expiry:
                        min_expiry = expiry
            
            # 如果没有找到过期时间，设置默认过期时间
            if min_expiry is None:
                min_expiry = datetime.now() + timedelta(hours=Config.COOKIE_EXPIRY_THRESHOLD_HOURS)
            
            return min_expiry.isoformat()
            
        except Exception as e:
            logging.error(f"计算过期时间失败: {e}")
            return None
    
    def cleanup_old_records(self, days_old: int = 30):
        """清理旧记录"""
        try:
            cutoff_date = datetime.now() - timedelta(days=days_old)
            
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                
                # 清理旧的日志
                cursor.execute('DELETE FROM login_logs WHERE timestamp < ?', (cutoff_date.isoformat(),))
                
                # 清理无效的旧cookies
                cursor.execute('''
                    DELETE FROM cookies 
                    WHERE is_valid = 0 AND updated_at < ?
                ''', (cutoff_date.isoformat(),))
                
                conn.commit()
                logging.info(f"已清理{days_old}天前的旧记录")
                
        except Exception as e:
            logging.error(f"清理旧记录失败: {e}")