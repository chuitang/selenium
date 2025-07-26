#!/usr/bin/env python3
"""
Cookie管理器安装脚本
"""

import os
import sys
import subprocess
from pathlib import Path


def check_python_version():
    """检查Python版本"""
    if sys.version_info < (3, 12):
        print("❌ 错误: 需要Python 3.12或更高版本")
        print(f"   当前版本: Python {sys.version}")
        sys.exit(1)
    print(f"✅ Python版本检查通过: {sys.version}")


def install_dependencies():
    """安装依赖"""
    print("\n📦 安装项目依赖...")
    
    try:
        # 使用pip安装项目
        subprocess.run([
            sys.executable, "-m", "pip", "install", "-e", "."
        ], check=True)
        print("✅ 项目依赖安装完成")
        
        # 安装Playwright浏览器
        print("\n🌐 安装Playwright浏览器...")
        subprocess.run([
            sys.executable, "-m", "playwright", "install", "chromium"
        ], check=True)
        print("✅ Playwright浏览器安装完成")
        
    except subprocess.CalledProcessError as e:
        print(f"❌ 安装失败: {e}")
        return False
    
    return True


def setup_config():
    """设置配置文件"""
    print("\n⚙️ 设置配置文件...")
    
    env_example = Path(".env.example")
    env_file = Path(".env")
    
    if env_example.exists():
        if not env_file.exists():
            # 复制示例配置文件
            import shutil
            shutil.copy(env_example, env_file)
            print("✅ 已创建 .env 配置文件")
        else:
            print("ℹ️  .env 文件已存在")
    else:
        print("⚠️  未找到 .env.example 文件")


def test_installation():
    """测试安装"""
    print("\n🧪 测试安装...")
    
    try:
        # 测试导入
        from cookie_manager import CookieManager, Settings
        print("✅ 模块导入测试通过")
        
        # 测试CLI
        result = subprocess.run([
            sys.executable, "-m", "cookie_manager.cli", "--help"
        ], capture_output=True, text=True)
        
        if result.returncode == 0:
            print("✅ 命令行界面测试通过")
        else:
            print("⚠️  命令行界面测试失败")
            
    except ImportError as e:
        print(f"❌ 模块导入失败: {e}")
        return False
    except Exception as e:
        print(f"⚠️  测试过程中出现异常: {e}")
    
    return True


def show_next_steps():
    """显示下一步操作"""
    print("\n🎉 安装完成！")
    print("\n📋 下一步操作:")
    print("1. 编辑 .env 文件配置你的设置")
    print("2. 运行以下命令初始化配置:")
    print("   cookie-manager init")
    print("3. 编辑生成的 config.json 文件添加你的网站信息")
    print("4. 测试登录:")
    print("   cookie-manager login")
    print("5. 启动监控:")
    print("   cookie-manager run")
    print("\n📖 更多信息请查看 README.md")


def main():
    """主函数"""
    print("🍪 Cookie管理器 v2.0 安装程序")
    print("=" * 40)
    
    # 检查Python版本
    check_python_version()
    
    # 安装依赖
    if not install_dependencies():
        print("\n❌ 安装失败，请检查错误信息")
        sys.exit(1)
    
    # 设置配置
    setup_config()
    
    # 测试安装
    test_installation()
    
    # 显示下一步
    show_next_steps()


if __name__ == "__main__":
    main()