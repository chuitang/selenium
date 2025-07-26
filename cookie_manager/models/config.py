"""配置模型，使用Pydantic进行验证和类型安全"""

from pathlib import Path
from typing import Literal, Optional
from pydantic import BaseModel, Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class DatabaseConfig(BaseModel):
    """数据库配置"""
    url: str = Field(default="sqlite:///cookies.db", description="数据库连接URL")
    echo: bool = Field(default=False, description="是否打印SQL语句")
    pool_size: int = Field(default=5, description="连接池大小")


class WebsiteConfig(BaseModel):
    """网站配置"""
    name: str = Field(description="网站名称")
    url: str = Field(description="网站主页URL")
    login_url: str = Field(description="登录页面URL")
    username: str = Field(description="用户名")
    password: str = Field(description="密码")
    
    # 页面元素选择器
    username_selector: str = Field(default='input[name="username"]')
    password_selector: str = Field(default='input[name="password"]')
    login_button_selector: str = Field(default='button[type="submit"]')
    
    # 验证配置
    success_indicator_type: Literal["url", "element", "text"] = Field(default="url")
    success_indicator_value: str = Field(default="dashboard")
    
    @field_validator('url', 'login_url')
    @classmethod
    def validate_url(cls, v: str) -> str:
        if not v.startswith(('http://', 'https://')):
            raise ValueError('URL必须以http://或https://开头')
        return v


class BrowserConfig(BaseModel):
    """浏览器配置"""
    headless: bool = Field(default=True, description="是否使用无头模式")
    timeout: int = Field(default=30, description="超时时间(秒)")
    user_agent: Optional[str] = Field(default=None, description="用户代理")
    viewport_width: int = Field(default=1920, description="视口宽度")
    viewport_height: int = Field(default=1080, description="视口高度")


class SchedulerConfig(BaseModel):
    """调度器配置"""
    check_interval_minutes: int = Field(default=30, description="检查间隔(分钟)")
    retry_attempts: int = Field(default=3, description="重试次数")
    retry_delay_seconds: int = Field(default=5, description="重试延迟(秒)")
    cookie_expiry_threshold_hours: int = Field(default=24, description="Cookie过期阈值(小时)")


class LoggingConfig(BaseModel):
    """日志配置"""
    level: Literal["DEBUG", "INFO", "WARNING", "ERROR"] = Field(default="INFO")
    file_path: Optional[Path] = Field(default=Path("cookie_manager.log"))
    max_bytes: int = Field(default=10_000_000, description="日志文件最大大小")
    backup_count: int = Field(default=5, description="备份文件数量")


class Settings(BaseSettings):
    """主配置类"""
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        env_nested_delimiter="__",
        case_sensitive=False,
    )
    
    # 各模块配置
    database: DatabaseConfig = Field(default_factory=DatabaseConfig)
    browser: BrowserConfig = Field(default_factory=BrowserConfig)
    scheduler: SchedulerConfig = Field(default_factory=SchedulerConfig)
    logging: LoggingConfig = Field(default_factory=LoggingConfig)
    
    # 网站配置列表
    websites: list[WebsiteConfig] = Field(default_factory=list)
    
    # 全局配置
    debug: bool = Field(default=False, description="调试模式")
    data_dir: Path = Field(default=Path("data"), description="数据目录")
    
    def model_post_init(self, __context) -> None:
        """模型初始化后的处理"""
        # 确保数据目录存在
        self.data_dir.mkdir(exist_ok=True)
        
        # 如果没有配置网站，添加示例配置
        if not self.websites:
            from .website import WebsiteConfig, LoginConfig, ValidationConfig
            self.websites = [
                WebsiteConfig(
                    name="示例网站",
                    url="https://example.com",
                    login=LoginConfig(url="https://example.com/login"),
                    validation=ValidationConfig(value="dashboard"),
                    username="your_username",
                    password="your_password"
                )
            ]