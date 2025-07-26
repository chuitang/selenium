import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # 数据库配置
    DB_PATH = os.getenv('DB_PATH', 'cookies.db')
    
    # 网站配置 - 这些可以通过环境变量配置
    WEBSITE_URL = os.getenv('WEBSITE_URL', 'https://example.com')
    LOGIN_URL = os.getenv('LOGIN_URL', 'https://example.com/login')
    
    # 登录凭据
    USERNAME = os.getenv('USERNAME', '')
    PASSWORD = os.getenv('PASSWORD', '')
    
    # 选择器配置 - 根据实际网站调整
    USERNAME_SELECTOR = os.getenv('USERNAME_SELECTOR', 'input[name="username"]')
    PASSWORD_SELECTOR = os.getenv('PASSWORD_SELECTOR', 'input[name="password"]')
    LOGIN_BUTTON_SELECTOR = os.getenv('LOGIN_BUTTON_SELECTOR', 'button[type="submit"]')
    
    # Cookie检查配置
    CHECK_INTERVAL_MINUTES = int(os.getenv('CHECK_INTERVAL_MINUTES', '30'))  # 检查间隔(分钟)
    COOKIE_EXPIRY_THRESHOLD_HOURS = int(os.getenv('COOKIE_EXPIRY_THRESHOLD_HOURS', '24'))  # cookie过期阈值(小时)
    
    # 浏览器配置
    HEADLESS = os.getenv('HEADLESS', 'True').lower() == 'true'
    BROWSER_TIMEOUT = int(os.getenv('BROWSER_TIMEOUT', '30'))
    
    # 重试配置
    MAX_RETRY_ATTEMPTS = int(os.getenv('MAX_RETRY_ATTEMPTS', '3'))
    RETRY_DELAY_SECONDS = int(os.getenv('RETRY_DELAY_SECONDS', '5'))
    
    # 验证URL - 用于测试cookie是否有效
    VERIFY_URL = os.getenv('VERIFY_URL', '')  # 如果为空则使用WEBSITE_URL
    
    # 成功登录的判断标识 - 可以是URL包含的字符串或页面元素
    SUCCESS_INDICATOR_TYPE = os.getenv('SUCCESS_INDICATOR_TYPE', 'url')  # 'url' 或 'element'
    SUCCESS_INDICATOR_VALUE = os.getenv('SUCCESS_INDICATOR_VALUE', 'dashboard')  # URL关键字或CSS选择器