"""浏览器管理模块，使用Playwright进行现代化的网页自动化"""

import asyncio
import json
import time
from pathlib import Path
from typing import Dict, List, Optional, Any
from urllib.parse import urlparse

from playwright.async_api import async_playwright, Browser, Page, BrowserContext
import structlog

from ..models.config import Settings
from ..models.website import WebsiteConfig
from ..models.cookie import CookieData, LoginResult, ValidationResult, CookieStatus


logger = structlog.get_logger(__name__)


class BrowserManager:
    """现代化浏览器管理器，使用Playwright"""
    
    def __init__(self, settings: Settings):
        self.settings = settings
        self.playwright = None
        self.browser: Optional[Browser] = None
        self.contexts: Dict[str, BrowserContext] = {}
        
    async def __aenter__(self):
        """异步上下文管理器入口"""
        await self.start()
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """异步上下文管理器出口"""
        await self.close()
    
    async def start(self) -> None:
        """启动浏览器"""
        if self.playwright is None:
            self.playwright = await async_playwright().start()
            
        if self.browser is None:
            self.browser = await self.playwright.chromium.launch(
                headless=self.settings.browser.headless,
                args=[
                    '--no-sandbox',
                    '--disable-blink-features=AutomationControlled',
                    '--disable-web-security',
                    '--disable-features=VizDisplayCompositor'
                ]
            )
            
        logger.info("浏览器启动成功", headless=self.settings.browser.headless)
    
    async def close(self) -> None:
        """关闭浏览器"""
        # 关闭所有上下文
        for context in self.contexts.values():
            await context.close()
        self.contexts.clear()
        
        # 关闭浏览器
        if self.browser:
            await self.browser.close()
            self.browser = None
            
        # 关闭Playwright
        if self.playwright:
            await self.playwright.stop()
            self.playwright = None
            
        logger.info("浏览器已关闭")
    
    async def _create_context(self, website_config: WebsiteConfig) -> BrowserContext:
        """为特定网站创建浏览器上下文"""
        if not self.browser:
            await self.start()
            
        context = await self.browser.new_context(
            viewport={
                'width': self.settings.browser.viewport_width,
                'height': self.settings.browser.viewport_height
            },
            user_agent=self.settings.browser.user_agent,
            extra_http_headers=website_config.custom_headers or {},
        )
        
        # 设置超时
        context.set_default_timeout(self.settings.browser.timeout * 1000)
        
        return context
    
    async def login(self, website_config: WebsiteConfig) -> LoginResult:
        """执行登录操作"""
        start_time = time.time()
        
        try:
            logger.info(
                "开始登录",
                website=website_config.name,
                url=website_config.login.url
            )
            
            # 创建新的浏览器上下文
            context = await self._create_context(website_config)
            page = await context.new_page()
            
            # 导航到登录页面
            await page.goto(website_config.login.url, wait_until='networkidle')
            
            # 等待页面加载
            await asyncio.sleep(1)
            
            # 如果有登录前步骤，执行它们
            if website_config.login.pre_login_steps:
                await self._execute_steps(page, website_config.login.pre_login_steps)
            
            # 填写用户名
            await page.fill(
                website_config.login.selectors.username, 
                website_config.username
            )
            await asyncio.sleep(0.5)
            
            # 填写密码
            await page.fill(
                website_config.login.selectors.password, 
                website_config.password
            )
            await asyncio.sleep(0.5)
            
            # 如果有记住我选项，选中它
            if website_config.login.selectors.remember_me:
                try:
                    await page.check(website_config.login.selectors.remember_me)
                except Exception:
                    logger.warning("无法选中记住我选项")
            
            # 点击登录按钮
            await page.click(website_config.login.selectors.login_button)
            
            # 等待登录完成
            await asyncio.sleep(website_config.login.wait_after_login)
            
            # 检查登录是否成功
            is_success = await self._verify_login_success(page, website_config)
            
            if is_success:
                # 获取cookies
                cookies = await context.cookies()
                cookie_dict = {cookie['name']: cookie['value'] for cookie in cookies}
                
                # 获取用户代理
                user_agent = await page.evaluate('navigator.userAgent')
                
                # 创建cookie数据
                cookie_data = CookieData(
                    website_name=website_config.name,
                    website_url=website_config.url,
                    cookies=cookie_dict,
                    headers=website_config.custom_headers,
                    user_agent=user_agent,
                    status=CookieStatus.VALID,
                    metadata={
                        'login_time': time.time() - start_time,
                        'login_url': website_config.login.url,
                        'browser_context': 'playwright'
                    }
                )
                
                # 执行登录后步骤
                if website_config.login.post_login_steps:
                    await self._execute_steps(page, website_config.login.post_login_steps)
                
                # 保存上下文供后续使用
                self.contexts[website_config.name] = context
                
                logger.info(
                    "登录成功",
                    website=website_config.name,
                    cookies_count=len(cookie_dict),
                    duration=time.time() - start_time
                )
                
                return LoginResult(
                    success=True,
                    cookie_data=cookie_data,
                    login_time=time.time(),
                    attempt_count=1
                )
            else:
                # 登录失败，保存截图
                screenshot_path = await self._save_screenshot(
                    page, 
                    f"login_failed_{website_config.name}"
                )
                
                await context.close()
                
                logger.error(
                    "登录失败",
                    website=website_config.name,
                    screenshot=screenshot_path
                )
                
                return LoginResult(
                    success=False,
                    error_message="登录验证失败",
                    screenshot_path=screenshot_path,
                    attempt_count=1
                )
                
        except Exception as e:
            logger.error(
                "登录过程发生异常",
                website=website_config.name,
                error=str(e),
                duration=time.time() - start_time
            )
            
            return LoginResult(
                success=False,
                error_message=f"登录异常: {str(e)}",
                attempt_count=1
            )
    
    async def validate_cookies(
        self, 
        cookie_data: CookieData, 
        website_config: WebsiteConfig
    ) -> ValidationResult:
        """验证cookies是否有效"""
        start_time = time.time()
        
        try:
            logger.info(
                "开始验证cookies",
                website=website_config.name,
                cookies_count=len(cookie_data.cookies)
            )
            
            # 创建新的上下文或使用现有上下文
            if website_config.name in self.contexts:
                context = self.contexts[website_config.name]
            else:
                context = await self._create_context(website_config)
                
                # 添加cookies到上下文
                await context.add_cookies([
                    {
                        'name': name,
                        'value': value,
                        'domain': website_config.cookies_domain,
                        'path': '/'
                    }
                    for name, value in cookie_data.cookies.items()
                ])
            
            page = await context.new_page()
            
            # 导航到验证URL
            validation_url = website_config.url
            if hasattr(website_config, 'validation_url'):
                validation_url = website_config.validation_url
            
            response = await page.goto(validation_url, wait_until='networkidle')
            
            # 检查响应状态
            status_code = response.status if response else 0
            
            # 根据配置的验证方式进行验证
            is_valid = await self._verify_page_success(page, website_config)
            
            response_time = time.time() - start_time
            
            await page.close()
            
            if is_valid:
                logger.info(
                    "Cookies验证成功",
                    website=website_config.name,
                    status_code=status_code,
                    response_time=response_time
                )
            else:
                logger.warning(
                    "Cookies验证失败",
                    website=website_config.name,
                    status_code=status_code
                )
            
            return ValidationResult(
                is_valid=is_valid,
                status_code=status_code,
                response_time=response_time,
                validation_url=validation_url
            )
            
        except Exception as e:
            logger.error(
                "Cookies验证异常",
                website=website_config.name,
                error=str(e)
            )
            
            return ValidationResult(
                is_valid=False,
                error_message=f"验证异常: {str(e)}",
                response_time=time.time() - start_time
            )
    
    async def refresh_cookies(
        self, 
        cookie_data: CookieData, 
        website_config: WebsiteConfig
    ) -> Optional[CookieData]:
        """尝试刷新cookies"""
        try:
            logger.info("尝试刷新cookies", website=website_config.name)
            
            # 使用现有cookies访问网站
            if website_config.name in self.contexts:
                context = self.contexts[website_config.name]
                page = await context.new_page()
                
                # 访问网站主页或特定刷新页面
                await page.goto(website_config.url, wait_until='networkidle')
                
                # 获取新的cookies
                new_cookies = await context.cookies()
                cookie_dict = {cookie['name']: cookie['value'] for cookie in new_cookies}
                
                # 检查是否有新的cookies
                if cookie_dict and cookie_dict != cookie_data.cookies:
                    updated_cookie_data = cookie_data.model_copy()
                    updated_cookie_data.cookies = cookie_dict
                    updated_cookie_data.mark_refreshed()
                    
                    await page.close()
                    
                    logger.info(
                        "Cookies刷新成功",
                        website=website_config.name,
                        new_cookies_count=len(cookie_dict)
                    )
                    
                    return updated_cookie_data
                
                await page.close()
            
            logger.info("Cookies无需刷新或刷新失败", website=website_config.name)
            return None
            
        except Exception as e:
            logger.error(
                "Cookies刷新异常",
                website=website_config.name,
                error=str(e)
            )
            return None
    
    async def _verify_login_success(
        self, 
        page: Page, 
        website_config: WebsiteConfig
    ) -> bool:
        """验证登录是否成功"""
        return await self._verify_page_success(page, website_config)
    
    async def _verify_page_success(
        self, 
        page: Page, 
        website_config: WebsiteConfig
    ) -> bool:
        """根据配置验证页面状态"""
        try:
            validation_type = website_config.validation.type
            validation_value = website_config.validation.value
            
            if validation_type == "url":
                current_url = page.url
                return validation_value in current_url
                
            elif validation_type == "element":
                element = await page.query_selector(validation_value)
                return element is not None
                
            elif validation_type == "text":
                content = await page.content()
                return validation_value in content
                
            elif validation_type == "api":
                # API验证需要额外实现
                return True
                
            return False
            
        except Exception as e:
            logger.error("页面验证异常", error=str(e))
            return False
    
    async def _execute_steps(self, page: Page, steps: List[str]) -> None:
        """执行自定义步骤"""
        for step in steps:
            try:
                # 这里可以实现自定义步骤的执行逻辑
                # 例如：点击某个元素、填写表单等
                if step.startswith("click:"):
                    selector = step.replace("click:", "")
                    await page.click(selector)
                elif step.startswith("wait:"):
                    seconds = float(step.replace("wait:", ""))
                    await asyncio.sleep(seconds)
                elif step.startswith("fill:"):
                    parts = step.replace("fill:", "").split("=", 1)
                    if len(parts) == 2:
                        selector, value = parts
                        await page.fill(selector, value)
                        
            except Exception as e:
                logger.warning("执行步骤失败", step=step, error=str(e))
    
    async def _save_screenshot(
        self, 
        page: Page, 
        filename_prefix: str
    ) -> str:
        """保存页面截图"""
        try:
            screenshots_dir = self.settings.data_dir / "screenshots"
            screenshots_dir.mkdir(exist_ok=True)
            
            timestamp = int(time.time())
            screenshot_path = screenshots_dir / f"{filename_prefix}_{timestamp}.png"
            
            await page.screenshot(path=str(screenshot_path), full_page=True)
            
            return str(screenshot_path)
            
        except Exception as e:
            logger.error("截图失败", error=str(e))
            return ""