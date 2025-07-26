"""网站配置模型"""

from typing import Dict, List, Literal, Optional
from pydantic import BaseModel, Field, field_validator


class SelectorConfig(BaseModel):
    """选择器配置"""
    username: str = Field(default='input[name="username"]', description="用户名选择器")
    password: str = Field(default='input[name="password"]', description="密码选择器") 
    login_button: str = Field(default='button[type="submit"]', description="登录按钮选择器")
    
    # 可选的额外选择器
    captcha: Optional[str] = Field(default=None, description="验证码选择器")
    remember_me: Optional[str] = Field(default=None, description="记住我选择器")
    logout_button: Optional[str] = Field(default=None, description="退出按钮选择器")


class ValidationConfig(BaseModel):
    """验证配置"""
    type: Literal["url", "element", "text", "api"] = Field(default="url", description="验证类型")
    value: str = Field(description="验证值")
    
    # 额外验证参数
    timeout: int = Field(default=10, description="验证超时时间")
    retry_count: int = Field(default=3, description="验证重试次数")
    
    @field_validator('type')
    @classmethod
    def validate_type(cls, v: str) -> str:
        valid_types = ["url", "element", "text", "api"]
        if v not in valid_types:
            raise ValueError(f'验证类型必须是: {", ".join(valid_types)}')
        return v


class LoginConfig(BaseModel):
    """登录配置"""
    url: str = Field(description="登录页面URL")
    method: Literal["form", "api"] = Field(default="form", description="登录方式")
    
    # 表单登录配置
    selectors: SelectorConfig = Field(default_factory=SelectorConfig)
    
    # API登录配置 
    api_endpoint: Optional[str] = Field(default=None, description="API端点")
    api_method: Literal["GET", "POST", "PUT"] = Field(default="POST", description="API方法")
    api_headers: Optional[Dict[str, str]] = Field(default=None, description="API头部")
    
    # 登录流程配置
    pre_login_steps: Optional[List[str]] = Field(default=None, description="登录前步骤")
    post_login_steps: Optional[List[str]] = Field(default=None, description="登录后步骤")
    
    # 等待配置
    wait_after_login: int = Field(default=2, description="登录后等待时间(秒)")
    page_load_timeout: int = Field(default=30, description="页面加载超时时间")


class WebsiteConfig(BaseModel):
    """完整的网站配置"""
    # 基本信息
    name: str = Field(description="网站名称")
    url: str = Field(description="网站首页URL")
    description: Optional[str] = Field(default=None, description="网站描述")
    
    # 认证信息
    username: str = Field(description="用户名")
    password: str = Field(description="密码")
    
    # 登录配置
    login: LoginConfig = Field(description="登录配置")
    
    # 验证配置
    validation: ValidationConfig = Field(description="Cookie验证配置")
    
    # 高级配置
    cookies_domain: Optional[str] = Field(default=None, description="Cookie域名")
    requires_2fa: bool = Field(default=False, description="是否需要双因子认证")
    custom_headers: Optional[Dict[str, str]] = Field(default=None, description="自定义请求头")
    
    # 限制配置
    rate_limit_delay: float = Field(default=1.0, description="请求间隔(秒)")
    max_concurrent_requests: int = Field(default=1, description="最大并发请求数")
    
    # 重试配置
    max_login_attempts: int = Field(default=3, description="最大登录尝试次数")
    retry_delay: int = Field(default=5, description="重试延迟(秒)")
    
    @field_validator('url', 'login')
    @classmethod
    def validate_urls(cls, v) -> str:
        if isinstance(v, str):
            if not v.startswith(('http://', 'https://')):
                raise ValueError('URL必须以http://或https://开头')
        elif hasattr(v, 'url'):
            if not v.url.startswith(('http://', 'https://')):
                raise ValueError('登录URL必须以http://或https://开头')
        return v
    
    def get_base_domain(self) -> str:
        """获取基础域名"""
        from urllib.parse import urlparse
        parsed = urlparse(self.url)
        return f"{parsed.scheme}://{parsed.netloc}"
    
    def model_post_init(self, __context) -> None:
        """模型初始化后处理"""
        # 如果没有指定Cookie域名，使用网站域名
        if not self.cookies_domain:
            from urllib.parse import urlparse
            parsed = urlparse(self.url)
            self.cookies_domain = parsed.netloc