#!/usr/bin/env python3
"""
现代化Cookie管理器命令行界面
"""

import asyncio
import json
import sys
from pathlib import Path

import click
import structlog
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn

# 导入我们的核心模块
try:
    from .core.manager import CookieManager
    from .models.config import Settings
    from .models.website import WebsiteConfig
except ImportError:
    try:
        from cookie_manager.core.manager import CookieManager
        from cookie_manager.models.config import Settings
        from cookie_manager.models.website import WebsiteConfig
    except ImportError:
        click.echo("❌ 无法导入必要的模块。请确保已正确安装cookie-manager。")
        CookieManager = None
        Settings = None
        WebsiteConfig = None

console = Console()
logger = structlog.get_logger()

def setup_logging(debug: bool = False):
    """设置日志配置"""
    structlog.configure(
        processors=[
            structlog.stdlib.filter_by_level,
            structlog.stdlib.add_logger_name,
            structlog.stdlib.add_log_level,
            structlog.stdlib.PositionalArgumentsFormatter(),
            structlog.dev.ConsoleRenderer()
        ],
        context_class=dict,
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=structlog.stdlib.BoundLogger,
        cache_logger_on_first_use=True,
    )

@click.group()
@click.option('--debug', is_flag=True, help='启用调试模式')
@click.pass_context
def cli(ctx, debug):
    """🍪 现代化Cookie管理器 - 自动登录和Cookie维护工具"""
    ctx.ensure_object(dict)
    ctx.obj['debug'] = debug
    setup_logging(debug)

@cli.command()
@click.option('--config-path', default='config.json', help='配置文件路径')
def init(config_path):
    """初始化配置文件"""
    config_file = Path(config_path)
    
    if config_file.exists():
        if not click.confirm(f"配置文件 {config_path} 已存在，是否覆盖？"):
            console.print("❌ 操作已取消", style="red")
            return
    
    example_config = {
        "websites": [
            {
                "name": "example_site",
                "url": "https://example.com",
                "login_url": "https://example.com/login",
                "login_config": {
                    "method": "form",
                    "username_selector": "#username",
                    "password_selector": "#password",
                    "submit_selector": "#login-button",
                    "username": "${USERNAME}",
                    "password": "${PASSWORD}"
                },
                "validation_config": {
                    "type": "url_check",
                    "success_url": "https://example.com/dashboard",
                    "success_selector": ".user-profile"
                },
                "retry_config": {
                    "max_retries": 3,
                    "retry_delay": 5
                }
            }
        ]
    }
    
    with open(config_file, 'w', encoding='utf-8') as f:
        json.dump(example_config, f, indent=2, ensure_ascii=False)
    
    console.print(f"✅ 配置文件已创建: {config_path}", style="green")
    console.print("\n📝 请编辑配置文件，添加你的网站信息:")
    console.print(f"   nano {config_path}")
    console.print("\n💡 别忘了在 .env 文件中设置用户名和密码:")
    console.print("   USERNAME=your_username")
    console.print("   PASSWORD=your_password")

@cli.command()
def hello():
    """测试命令"""
    console.print("🍪 Cookie管理器 v2.0 正在运行！", style="green")
    console.print("使用基于Python 3.12+的现代化架构", style="blue")

@cli.command()
def version():
    """显示版本信息"""
    console.print("Cookie管理器 v2.0.0", style="green")
    console.print("基于Playwright + SQLAlchemy 2.0 + Pydantic v2", style="blue")

def main():
    """主入口点"""
    cli()

if __name__ == '__main__':
    main()
