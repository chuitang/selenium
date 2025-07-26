import schedule
import time
import logging
import threading
from datetime import datetime
from config import Config
from web_client import WebClient
from database import CookieDatabase

class CookieScheduler:
    def __init__(self):
        self.config = Config()
        self.web_client = WebClient()
        self.db = CookieDatabase()
        self.running = False
        self.scheduler_thread = None
        
    def setup_logging(self):
        """设置日志配置"""
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s',
            handlers=[
                logging.FileHandler('cookie_manager.log', encoding='utf-8'),
                logging.StreamHandler()
            ]
        )
    
    def check_cookies_job(self):
        """定时检查cookies的任务"""
        try:
            logging.info("开始定时检查cookies")
            
            # 执行检查和刷新
            success = self.web_client.check_and_refresh_cookies()
            
            if success:
                logging.info("Cookies检查完成，状态正常")
            else:
                logging.warning("Cookies检查失败或需要手动干预")
                
            # 清理旧记录
            self.db.cleanup_old_records()
            
        except Exception as e:
            logging.error(f"定时检查任务失败: {e}")
    
    def start_scheduler(self):
        """启动调度器"""
        if self.running:
            logging.warning("调度器已在运行中")
            return
        
        self.setup_logging()
        logging.info("正在启动Cookie管理调度器...")
        
        # 设置定时任务
        schedule.every(self.config.CHECK_INTERVAL_MINUTES).minutes.do(self.check_cookies_job)
        
        # 立即执行一次检查
        logging.info("执行初始检查...")
        self.check_cookies_job()
        
        # 启动调度器线程
        self.running = True
        self.scheduler_thread = threading.Thread(target=self._run_scheduler, daemon=True)
        self.scheduler_thread.start()
        
        logging.info(f"调度器已启动，将每 {self.config.CHECK_INTERVAL_MINUTES} 分钟检查一次cookies")
    
    def _run_scheduler(self):
        """运行调度器的内部方法"""
        while self.running:
            try:
                schedule.run_pending()
                time.sleep(1)
            except Exception as e:
                logging.error(f"调度器运行错误: {e}")
                time.sleep(60)  # 出错时等待1分钟后继续
    
    def stop_scheduler(self):
        """停止调度器"""
        if not self.running:
            logging.info("调度器未在运行")
            return
        
        logging.info("正在停止调度器...")
        self.running = False
        
        if self.scheduler_thread and self.scheduler_thread.is_alive():
            self.scheduler_thread.join(timeout=5)
        
        schedule.clear()
        logging.info("调度器已停止")
    
    def run_once(self):
        """手动执行一次检查"""
        self.setup_logging()
        logging.info("手动执行cookies检查...")
        self.check_cookies_job()
        
    def get_status(self) -> dict:
        """获取当前状态"""
        website_url = self.config.WEBSITE_URL
        cookie_data = self.db.get_cookies(website_url)
        recent_logs = self.db.get_recent_logs(website_url, 10)
        
        status = {
            'scheduler_running': self.running,
            'check_interval_minutes': self.config.CHECK_INTERVAL_MINUTES,
            'website_url': website_url,
            'has_valid_cookies': cookie_data is not None,
            'last_update': cookie_data['updated_at'] if cookie_data else None,
            'last_verified': cookie_data['last_verified'] if cookie_data else None,
            'recent_logs': recent_logs
        }
        
        return status
    
    def force_login(self):
        """强制重新登录"""
        self.setup_logging()
        logging.info("强制执行重新登录...")
        
        # 标记现有cookies为无效
        self.db.mark_cookies_invalid(self.config.WEBSITE_URL)
        
        # 执行完整登录
        success = self.web_client.perform_full_login()
        
        if success:
            logging.info("强制重新登录成功")
        else:
            logging.error("强制重新登录失败")
        
        return success
    
    def run_continuous(self):
        """持续运行模式（阻塞）"""
        self.start_scheduler()
        
        try:
            logging.info("Cookie管理器正在运行，按 Ctrl+C 停止...")
            while self.running:
                time.sleep(1)
        except KeyboardInterrupt:
            logging.info("收到停止信号")
        finally:
            self.stop_scheduler()

def main():
    """主函数，用于命令行启动"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Cookie管理器')
    parser.add_argument('--mode', choices=['once', 'continuous', 'login', 'status'], 
                       default='continuous', help='运行模式')
    parser.add_argument('--config', help='配置文件路径')
    
    args = parser.parse_args()
    
    scheduler = CookieScheduler()
    
    if args.mode == 'once':
        scheduler.run_once()
    elif args.mode == 'continuous':
        scheduler.run_continuous()
    elif args.mode == 'login':
        scheduler.force_login()
    elif args.mode == 'status':
        import json
        status = scheduler.get_status()
        print(json.dumps(status, indent=2, ensure_ascii=False))

if __name__ == '__main__':
    main()