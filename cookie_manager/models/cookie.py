"""Cookie数据模型"""

from datetime import datetime
from enum import Enum
from typing import Any, Dict, Optional
from pydantic import BaseModel, Field


class CookieStatus(str, Enum):
    """Cookie状态枚举"""
    VALID = "valid"          # 有效
    EXPIRED = "expired"      # 已过期
    INVALID = "invalid"      # 无效
    REFRESHED = "refreshed"  # 已刷新
    FAILED = "failed"        # 验证失败


class CookieData(BaseModel):
    """Cookie数据模型"""
    id: Optional[int] = Field(default=None, description="数据库ID")
    website_name: str = Field(description="网站名称")
    website_url: str = Field(description="网站URL")
    
    # Cookie数据
    cookies: Dict[str, Any] = Field(description="Cookie字典")
    headers: Optional[Dict[str, str]] = Field(default=None, description="HTTP头")
    user_agent: Optional[str] = Field(default=None, description="用户代理")
    
    # 状态信息
    status: CookieStatus = Field(default=CookieStatus.VALID, description="Cookie状态")
    last_validated: datetime = Field(default_factory=datetime.now, description="最后验证时间")
    last_refreshed: Optional[datetime] = Field(default=None, description="最后刷新时间")
    
    # 时间戳
    created_at: datetime = Field(default_factory=datetime.now, description="创建时间")
    updated_at: datetime = Field(default_factory=datetime.now, description="更新时间")
    
    # 验证相关
    validation_count: int = Field(default=0, description="验证次数")
    refresh_count: int = Field(default=0, description="刷新次数")
    failure_count: int = Field(default=0, description="失败次数")
    
    # 额外元数据
    metadata: Optional[Dict[str, Any]] = Field(default=None, description="额外元数据")
    
    class Config:
        """Pydantic配置"""
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }
    
    def is_expired(self, threshold_hours: int = 24) -> bool:
        """检查Cookie是否过期"""
        if self.status == CookieStatus.EXPIRED:
            return True
        
        # 检查最后验证时间
        time_diff = datetime.now() - self.last_validated
        return time_diff.total_seconds() > threshold_hours * 3600
    
    def mark_validated(self) -> None:
        """标记为已验证"""
        self.last_validated = datetime.now()
        self.validation_count += 1
        self.updated_at = datetime.now()
    
    def mark_refreshed(self) -> None:
        """标记为已刷新"""
        self.last_refreshed = datetime.now()
        self.refresh_count += 1
        self.status = CookieStatus.REFRESHED
        self.updated_at = datetime.now()
    
    def mark_failed(self) -> None:
        """标记为失败"""
        self.failure_count += 1
        self.status = CookieStatus.FAILED
        self.updated_at = datetime.now()
    
    def mark_expired(self) -> None:
        """标记为过期"""
        self.status = CookieStatus.EXPIRED
        self.updated_at = datetime.now()


class LoginResult(BaseModel):
    """登录结果模型"""
    success: bool = Field(description="登录是否成功")
    cookie_data: Optional[CookieData] = Field(default=None, description="Cookie数据")
    error_message: Optional[str] = Field(default=None, description="错误信息")
    screenshot_path: Optional[str] = Field(default=None, description="截图路径")
    
    # 登录统计
    login_time: datetime = Field(default_factory=datetime.now, description="登录时间")
    attempt_count: int = Field(default=1, description="尝试次数")


class ValidationResult(BaseModel):
    """验证结果模型"""
    is_valid: bool = Field(description="是否有效")
    status_code: Optional[int] = Field(default=None, description="HTTP状态码")
    response_time: Optional[float] = Field(default=None, description="响应时间")
    error_message: Optional[str] = Field(default=None, description="错误信息")
    
    # 验证详情
    validated_at: datetime = Field(default_factory=datetime.now, description="验证时间")
    validation_url: Optional[str] = Field(default=None, description="验证URL")