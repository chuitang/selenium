"""Cookie管理器主类，协调所有组件"""

import asyncio
from datetime import datetime, timedelta
from typing import List, Optional, Dict, Any

import structlog

from ..models.config import Settings
from ..models.cookie import CookieData, CookieStatus, LoginResult, ValidationResult
from ..models.website import WebsiteConfig
from .database import DatabaseManager
from .browser import BrowserManager


logger = structlog.get_logger(__name__)


class CookieManager:
    """现代化Cookie管理器主类"""
    
    def __init__(self, settings: Settings):
        self.settings = settings
        self.db_manager = DatabaseManager(settings)
        self.browser_manager = BrowserManager(settings)
        self._running = False
        
    async def initialize(self) -> None:
        """初始化管理器"""
        logger.info("初始化Cookie管理器")
        
        # 初始化数据库
        await self.db_manager.init_database()
        
        # 启动浏览器
        await self.browser_manager.start()
        
        logger.info("Cookie管理器初始化完成")
    
    async def close(self) -> None:
        """关闭管理器"""
        logger.info("关闭Cookie管理器")
        
        self._running = False
        await self.browser_manager.close()
        
        logger.info("Cookie管理器已关闭")
    
    async def __aenter__(self):
        """异步上下文管理器入口"""
        await self.initialize()
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """异步上下文管理器出口"""
        await self.close()
    
    async def login_website(self, website_config: WebsiteConfig) -> LoginResult:
        """登录指定网站"""
        logger.info("开始登录网站", website=website_config.name)
        
        max_attempts = website_config.max_login_attempts
        
        for attempt in range(1, max_attempts + 1):
            try:
                logger.info(
                    "登录尝试",
                    website=website_config.name,
                    attempt=attempt,
                    max_attempts=max_attempts
                )
                
                # 执行登录
                result = await self.browser_manager.login(website_config)
                
                if result.success and result.cookie_data:
                    # 保存cookies到数据库
                    saved_cookie = self.db_manager.save_cookie(result.cookie_data)
                    result.cookie_data = saved_cookie
                    
                    logger.info(
                        "网站登录成功",
                        website=website_config.name,
                        attempt=attempt,
                        cookies_count=len(saved_cookie.cookies)
                    )
                    
                    return result
                else:
                    logger.warning(
                        "登录失败",
                        website=website_config.name,
                        attempt=attempt,
                        error=result.error_message
                    )
                    
                    if attempt < max_attempts:
                        # 等待后重试
                        await asyncio.sleep(website_config.retry_delay)
                    
            except Exception as e:
                logger.error(
                    "登录异常",
                    website=website_config.name,
                    attempt=attempt,
                    error=str(e)
                )
                
                if attempt < max_attempts:
                    await asyncio.sleep(website_config.retry_delay)
        
        # 所有尝试都失败
        logger.error(
            "网站登录失败，已达到最大尝试次数",
            website=website_config.name,
            max_attempts=max_attempts
        )
        
        return LoginResult(
            success=False,
            error_message=f"登录失败，已达到最大尝试次数 ({max_attempts})",
            attempt_count=max_attempts
        )
    
    async def validate_website_cookies(self, website_config: WebsiteConfig) -> ValidationResult:
        """验证指定网站的cookies"""
        logger.info("验证网站cookies", website=website_config.name)
        
        # 从数据库获取cookies
        cookie_data = self.db_manager.get_cookie(website_config.name)
        
        if not cookie_data:
            logger.warning("未找到网站cookies", website=website_config.name)
            return ValidationResult(
                is_valid=False,
                error_message="未找到cookies"
            )
        
        # 检查cookies是否过期
        if cookie_data.is_expired(self.settings.scheduler.cookie_expiry_threshold_hours):
            logger.info("Cookies已过期", website=website_config.name)
            cookie_data.mark_expired()
            self.db_manager.save_cookie(cookie_data)
            
            return ValidationResult(
                is_valid=False,
                error_message="Cookies已过期"
            )
        
        # 使用浏览器验证cookies
        validation_result = await self.browser_manager.validate_cookies(
            cookie_data, 
            website_config
        )
        
        # 更新cookie状态
        if validation_result.is_valid:
            cookie_data.mark_validated()
            cookie_data.status = CookieStatus.VALID
        else:
            cookie_data.mark_failed()
            cookie_data.status = CookieStatus.INVALID
        
        # 保存更新后的cookie
        self.db_manager.save_cookie(cookie_data)
        
        logger.info(
            "Cookies验证完成",
            website=website_config.name,
            is_valid=validation_result.is_valid,
            response_time=validation_result.response_time
        )
        
        return validation_result
    
    async def refresh_website_cookies(self, website_config: WebsiteConfig) -> bool:
        """尝试刷新指定网站的cookies"""
        logger.info("尝试刷新网站cookies", website=website_config.name)
        
        # 从数据库获取当前cookies
        cookie_data = self.db_manager.get_cookie(website_config.name)
        
        if not cookie_data:
            logger.warning("未找到网站cookies，无法刷新", website=website_config.name)
            return False
        
        # 尝试刷新cookies
        refreshed_cookie = await self.browser_manager.refresh_cookies(
            cookie_data, 
            website_config
        )
        
        if refreshed_cookie:
            # 保存刷新后的cookies
            self.db_manager.save_cookie(refreshed_cookie)
            
            logger.info(
                "Cookies刷新成功",
                website=website_config.name,
                refresh_count=refreshed_cookie.refresh_count
            )
            
            return True
        else:
            logger.info("Cookies无需刷新或刷新失败", website=website_config.name)
            return False
    
    async def check_and_refresh_website(self, website_config: WebsiteConfig) -> Dict[str, Any]:
        """检查并刷新指定网站的cookies"""
        logger.info("检查并刷新网站", website=website_config.name)
        
        start_time = datetime.now()
        result = {
            "website": website_config.name,
            "start_time": start_time.isoformat(),
            "actions": [],
            "final_status": "unknown",
            "error": None
        }
        
        try:
            # 1. 验证当前cookies
            validation_result = await self.validate_website_cookies(website_config)
            result["actions"].append({
                "action": "validate",
                "success": validation_result.is_valid,
                "response_time": validation_result.response_time,
                "error": validation_result.error_message
            })
            
            if validation_result.is_valid:
                result["final_status"] = "valid"
                logger.info("网站cookies有效", website=website_config.name)
                return result
            
            # 2. 尝试刷新cookies
            refresh_success = await self.refresh_website_cookies(website_config)
            result["actions"].append({
                "action": "refresh",
                "success": refresh_success
            })
            
            if refresh_success:
                # 验证刷新后的cookies
                validation_result = await self.validate_website_cookies(website_config)
                result["actions"].append({
                    "action": "validate_after_refresh",
                    "success": validation_result.is_valid,
                    "response_time": validation_result.response_time
                })
                
                if validation_result.is_valid:
                    result["final_status"] = "refreshed"
                    logger.info("网站cookies刷新后有效", website=website_config.name)
                    return result
            
            # 3. 重新登录
            logger.info("尝试重新登录", website=website_config.name)
            login_result = await self.login_website(website_config)
            result["actions"].append({
                "action": "relogin",
                "success": login_result.success,
                "attempt_count": login_result.attempt_count,
                "error": login_result.error_message
            })
            
            if login_result.success:
                result["final_status"] = "relogged"
                logger.info("网站重新登录成功", website=website_config.name)
            else:
                result["final_status"] = "failed"
                result["error"] = login_result.error_message
                logger.error("网站重新登录失败", website=website_config.name)
            
        except Exception as e:
            result["final_status"] = "error"
            result["error"] = str(e)
            logger.error(
                "检查和刷新网站异常",
                website=website_config.name,
                error=str(e)
            )
        
        finally:
            end_time = datetime.now()
            result["end_time"] = end_time.isoformat()
            result["duration"] = (end_time - start_time).total_seconds()
        
        return result
    
    async def check_all_websites(self) -> List[Dict[str, Any]]:
        """检查所有网站的cookies"""
        logger.info("开始检查所有网站", count=len(self.settings.websites))
        
        results = []
        
        # 并发处理所有网站（可以配置并发数量）
        semaphore = asyncio.Semaphore(3)  # 限制并发数
        
        async def check_website_with_semaphore(website_config: WebsiteConfig):
            async with semaphore:
                return await self.check_and_refresh_website(website_config)
        
        # 创建所有任务
        tasks = [
            check_website_with_semaphore(website_config)
            for website_config in self.settings.websites
        ]
        
        # 等待所有任务完成
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # 处理异常结果
        processed_results = []
        for i, result in enumerate(results):
            if isinstance(result, Exception):
                processed_results.append({
                    "website": self.settings.websites[i].name,
                    "final_status": "error",
                    "error": str(result)
                })
            else:
                processed_results.append(result)
        
        logger.info("所有网站检查完成", total=len(processed_results))
        
        return processed_results
    
    async def run_scheduler(self) -> None:
        """运行定时任务调度器"""
        logger.info(
            "启动定时调度器",
            interval=self.settings.scheduler.check_interval_minutes
        )
        
        self._running = True
        
        while self._running:
            try:
                logger.info("开始定时检查循环")
                
                # 检查所有网站
                results = await self.check_all_websites()
                
                # 统计结果
                stats = {
                    "total": len(results),
                    "valid": sum(1 for r in results if r["final_status"] == "valid"),
                    "refreshed": sum(1 for r in results if r["final_status"] == "refreshed"),
                    "relogged": sum(1 for r in results if r["final_status"] == "relogged"),
                    "failed": sum(1 for r in results if r["final_status"] == "failed"),
                    "error": sum(1 for r in results if r["final_status"] == "error"),
                }
                
                logger.info("定时检查完成", stats=stats)
                
                # 等待下次检查
                if self._running:
                    wait_seconds = self.settings.scheduler.check_interval_minutes * 60
                    logger.info(f"等待 {wait_seconds} 秒后进行下次检查")
                    await asyncio.sleep(wait_seconds)
                    
            except Exception as e:
                logger.error("调度器异常", error=str(e))
                
                # 异常后等待较短时间再重试
                if self._running:
                    await asyncio.sleep(60)
    
    def stop_scheduler(self) -> None:
        """停止调度器"""
        logger.info("停止调度器")
        self._running = False
    
    def get_statistics(self) -> Dict[str, Any]:
        """获取统计信息"""
        db_stats = self.db_manager.get_statistics()
        
        return {
            "database": db_stats,
            "websites_configured": len(self.settings.websites),
            "scheduler_running": self._running,
            "check_interval_minutes": self.settings.scheduler.check_interval_minutes,
        }
    
    async def test_website_login(self, website_config: WebsiteConfig) -> Dict[str, Any]:
        """测试网站登录（调试用）"""
        logger.info("测试网站登录", website=website_config.name)
        
        try:
            result = await self.login_website(website_config)
            
            return {
                "website": website_config.name,
                "success": result.success,
                "error": result.error_message,
                "attempt_count": result.attempt_count,
                "screenshot": result.screenshot_path,
                "cookies_count": len(result.cookie_data.cookies) if result.cookie_data else 0
            }
            
        except Exception as e:
            logger.error("测试登录异常", website=website_config.name, error=str(e))
            return {
                "website": website_config.name,
                "success": False,
                "error": str(e)
            }