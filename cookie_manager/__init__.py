"""
现代化Cookie管理器
支持异步操作、类型提示和现代Python特性
"""

__version__ = "2.0.0"
__author__ = "Cookie Manager Team"

# 导入核心组件
try:
    from .core.manager import CookieManager
    from .models.config import Settings
    from .models.cookie import CookieData, CookieStatus
    
    __all__ = ["CookieManager", "Settings", "CookieData", "CookieStatus"]
except ImportError:
    # 如果导入失败，可能是依赖问题，提供基础功能
    __all__ = []