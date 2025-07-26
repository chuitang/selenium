import time
import logging
import requests
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.action_chains import ActionChains
from fake_useragent import UserAgent
from typing import Dict, List, Optional, Tuple
from config import Config
from database import CookieDatabase

class WebClient:
    def __init__(self):
        self.config = Config()
        self.db = CookieDatabase()
        self.ua = UserAgent()
        self.session = requests.Session()
        
    def _get_chrome_options(self):
        """配置Chrome浏览器选项"""
        options = Options()
        
        if self.config.HEADLESS:
            options.add_argument('--headless')
        
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--disable-blink-features=AutomationControlled')
        options.add_experimental_option("excludeSwitches", ["enable-automation"])
        options.add_experimental_option('useAutomationExtension', False)
        options.add_argument(f'--user-agent={self.ua.random}')
        options.add_argument('--disable-web-security')
        options.add_argument('--allow-running-insecure-content')
        
        return options
    
    def login_with_selenium(self, username: str = None, password: str = None) -> Tuple[bool, Optional[List[Dict]], str]:
        """使用Selenium自动登录网站"""
        driver = None
        try:
            username = username or self.config.USERNAME
            password = password or self.config.PASSWORD
            
            if not username or not password:
                return False, None, "用户名或密码未配置"
            
            # 初始化浏览器
            options = self._get_chrome_options()
            driver = webdriver.Chrome(options=options)
            driver.implicitly_wait(self.config.BROWSER_TIMEOUT)
            
            # 访问登录页面
            logging.info(f"正在访问登录页面: {self.config.LOGIN_URL}")
            driver.get(self.config.LOGIN_URL)
            
            # 等待页面加载
            time.sleep(2)
            
            # 查找并填写用户名
            try:
                username_field = WebDriverWait(driver, 10).until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, self.config.USERNAME_SELECTOR))
                )
                username_field.clear()
                username_field.send_keys(username)
                logging.info("用户名已填写")
            except Exception as e:
                return False, None, f"无法找到用户名输入框: {e}"
            
            # 查找并填写密码
            try:
                password_field = driver.find_element(By.CSS_SELECTOR, self.config.PASSWORD_SELECTOR)
                password_field.clear()
                password_field.send_keys(password)
                logging.info("密码已填写")
            except Exception as e:
                return False, None, f"无法找到密码输入框: {e}"
            
            # 点击登录按钮
            try:
                login_button = driver.find_element(By.CSS_SELECTOR, self.config.LOGIN_BUTTON_SELECTOR)
                ActionChains(driver).move_to_element(login_button).click().perform()
                logging.info("登录按钮已点击")
            except Exception as e:
                return False, None, f"无法找到或点击登录按钮: {e}"
            
            # 等待登录完成
            time.sleep(5)
            
            # 验证登录是否成功
            if not self._verify_login_success(driver):
                return False, None, "登录验证失败"
            
            # 获取cookies
            cookies = driver.get_cookies()
            user_agent = driver.execute_script("return navigator.userAgent;")
            
            logging.info(f"登录成功，获取到 {len(cookies)} 个cookies")
            return True, cookies, user_agent
            
        except Exception as e:
            logging.error(f"登录过程中出错: {e}")
            return False, None, str(e)
        
        finally:
            if driver:
                driver.quit()
    
    def _verify_login_success(self, driver) -> bool:
        """验证登录是否成功"""
        try:
            current_url = driver.current_url
            
            if self.config.SUCCESS_INDICATOR_TYPE == 'url':
                # 通过URL判断
                if self.config.SUCCESS_INDICATOR_VALUE in current_url:
                    logging.info(f"登录成功 - URL包含: {self.config.SUCCESS_INDICATOR_VALUE}")
                    return True
            elif self.config.SUCCESS_INDICATOR_TYPE == 'element':
                # 通过页面元素判断
                try:
                    WebDriverWait(driver, 10).until(
                        EC.presence_of_element_located((By.CSS_SELECTOR, self.config.SUCCESS_INDICATOR_VALUE))
                    )
                    logging.info(f"登录成功 - 找到元素: {self.config.SUCCESS_INDICATOR_VALUE}")
                    return True
                except:
                    pass
            
            # 默认判断：如果当前URL不是登录页面，认为登录成功
            if current_url != self.config.LOGIN_URL and 'login' not in current_url.lower():
                logging.info("登录成功 - 已离开登录页面")
                return True
            
            return False
            
        except Exception as e:
            logging.error(f"验证登录状态时出错: {e}")
            return False
    
    def verify_cookies_with_requests(self, cookies: List[Dict], user_agent: str = None) -> bool:
        """使用requests验证cookies是否有效"""
        try:
            # 准备cookies
            cookie_dict = {cookie['name']: cookie['value'] for cookie in cookies}
            
            # 设置headers
            headers = {
                'User-Agent': user_agent or self.ua.random,
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
                'Accept-Encoding': 'gzip, deflate, br',
                'Connection': 'keep-alive',
                'Upgrade-Insecure-Requests': '1',
            }
            
            # 选择验证URL
            verify_url = self.config.VERIFY_URL or self.config.WEBSITE_URL
            
            # 发送请求
            response = self.session.get(
                verify_url,
                cookies=cookie_dict,
                headers=headers,
                timeout=30,
                allow_redirects=True
            )
            
            # 验证响应
            if response.status_code == 200:
                # 检查是否被重定向到登录页面
                if 'login' in response.url.lower() or response.url == self.config.LOGIN_URL:
                    logging.warning("Cookies验证失败 - 被重定向到登录页面")
                    return False
                
                # 检查成功指示器
                if self.config.SUCCESS_INDICATOR_TYPE == 'url':
                    if self.config.SUCCESS_INDICATOR_VALUE in response.url:
                        logging.info("Cookies验证成功")
                        return True
                elif self.config.SUCCESS_INDICATOR_TYPE == 'element':
                    from bs4 import BeautifulSoup
                    soup = BeautifulSoup(response.text, 'html.parser')
                    if soup.select(self.config.SUCCESS_INDICATOR_VALUE):
                        logging.info("Cookies验证成功")
                        return True
                
                # 默认判断：状态码为200且未重定向到登录页面
                logging.info("Cookies验证成功（默认判断）")
                return True
            
            logging.warning(f"Cookies验证失败 - HTTP状态码: {response.status_code}")
            return False
            
        except Exception as e:
            logging.error(f"验证cookies时出错: {e}")
            return False
    
    def try_refresh_cookies(self, old_cookies: List[Dict], user_agent: str = None) -> Tuple[bool, Optional[List[Dict]]]:
        """尝试使用旧cookies获取新的cookies"""
        driver = None
        try:
            # 初始化浏览器
            options = self._get_chrome_options()
            driver = webdriver.Chrome(options=options)
            driver.implicitly_wait(self.config.BROWSER_TIMEOUT)
            
            # 先访问网站首页
            driver.get(self.config.WEBSITE_URL)
            time.sleep(2)
            
            # 设置旧cookies
            for cookie in old_cookies:
                try:
                    # 清理cookie数据，只保留必要字段
                    clean_cookie = {
                        'name': cookie['name'],
                        'value': cookie['value'],
                        'domain': cookie.get('domain', ''),
                        'path': cookie.get('path', '/'),
                    }
                    
                    # 只添加有效的可选字段
                    if cookie.get('secure') is not None:
                        clean_cookie['secure'] = cookie['secure']
                    if cookie.get('httpOnly') is not None:
                        clean_cookie['httpOnly'] = cookie['httpOnly']
                    
                    driver.add_cookie(clean_cookie)
                except Exception as e:
                    logging.warning(f"无法设置cookie {cookie.get('name', 'unknown')}: {e}")
            
            # 刷新页面以应用cookies
            driver.refresh()
            time.sleep(3)
            
            # 检查是否需要登录
            current_url = driver.current_url
            if 'login' not in current_url.lower():
                # 获取新的cookies
                new_cookies = driver.get_cookies()
                new_user_agent = driver.execute_script("return navigator.userAgent;")
                
                # 验证新cookies
                if self.verify_cookies_with_requests(new_cookies, new_user_agent):
                    logging.info("成功刷新cookies")
                    return True, new_cookies
            
            logging.warning("刷新cookies失败")
            return False, None
            
        except Exception as e:
            logging.error(f"刷新cookies时出错: {e}")
            return False, None
        
        finally:
            if driver:
                driver.quit()
    
    def perform_full_login(self) -> bool:
        """执行完整的登录流程"""
        website_url = self.config.WEBSITE_URL
        
        try:
            # 尝试登录
            success, cookies, user_agent = self.login_with_selenium()
            
            if success and cookies:
                # 验证cookies
                if self.verify_cookies_with_requests(cookies, user_agent):
                    # 保存到数据库
                    if self.db.save_cookies(website_url, cookies, user_agent, "自动登录获取"):
                        self.db.log_action(website_url, "login", True, "登录并保存cookies成功")
                        logging.info("完整登录流程成功")
                        return True
                    else:
                        self.db.log_action(website_url, "login", False, "保存cookies失败")
                else:
                    self.db.log_action(website_url, "login", False, "cookies验证失败")
            else:
                self.db.log_action(website_url, "login", False, f"登录失败")
            
            return False
            
        except Exception as e:
            logging.error(f"完整登录流程失败: {e}")
            self.db.log_action(website_url, "login", False, str(e))
            return False
    
    def check_and_refresh_cookies(self) -> bool:
        """检查并刷新cookies"""
        website_url = self.config.WEBSITE_URL
        
        try:
            # 从数据库获取现有cookies
            cookie_data = self.db.get_cookies(website_url)
            
            if not cookie_data:
                logging.info("数据库中无有效cookies，需要重新登录")
                return self.perform_full_login()
            
            cookies = cookie_data['cookies']
            user_agent = cookie_data['user_agent']
            
            # 验证现有cookies
            if self.verify_cookies_with_requests(cookies, user_agent):
                logging.info("现有cookies仍然有效")
                self.db.update_last_verified(website_url)
                self.db.log_action(website_url, "verify", True, "cookies验证通过")
                return True
            
            logging.info("现有cookies已失效，尝试刷新")
            self.db.log_action(website_url, "verify", False, "cookies已失效")
            
            # 尝试刷新cookies
            refresh_success, new_cookies = self.try_refresh_cookies(cookies, user_agent)
            
            if refresh_success and new_cookies:
                # 保存新cookies
                if self.db.save_cookies(website_url, new_cookies, user_agent, "刷新获取"):
                    self.db.log_action(website_url, "refresh", True, "cookies刷新成功")
                    logging.info("cookies刷新成功")
                    return True
                else:
                    self.db.log_action(website_url, "refresh", False, "保存刷新的cookies失败")
            else:
                self.db.log_action(website_url, "refresh", False, "cookies刷新失败")
            
            # 刷新失败，重新登录
            logging.info("cookies刷新失败，执行重新登录")
            self.db.mark_cookies_invalid(website_url)
            return self.perform_full_login()
            
        except Exception as e:
            logging.error(f"检查和刷新cookies失败: {e}")
            self.db.log_action(website_url, "check", False, str(e))
            return False