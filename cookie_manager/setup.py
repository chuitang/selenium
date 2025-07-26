#!/usr/bin/env python3
"""
Cookie管理器安装和设置脚本
"""

import os
import sys
import shutil
import subprocess

def check_python_version():
    """检查Python版本"""
    if sys.version_info < (3, 7):
        print("错误: 需要Python 3.7或更高版本")
        sys.exit(1)
    print(f"✓ Python版本检查通过: {sys.version}")

def install_dependencies():
    """安装Python依赖"""
    print("\n正在安装Python依赖...")
    try:
        subprocess.run([sys.executable, "-m", "pip", "install", "-r", "requirements.txt"], 
                      check=True)
        print("✓ Python依赖安装完成")
    except subprocess.CalledProcessError:
        print("✗ Python依赖安装失败")
        return False
    return True

def setup_config():
    """设置配置文件"""
    print("\n设置配置文件...")
    
    if not os.path.exists('.env.example'):
        print("✗ 找不到 .env.example 文件")
        return False
    
    if os.path.exists('.env'):
        overwrite = input("配置文件 .env 已存在，是否覆盖? (y/n): ").lower()
        if overwrite != 'y':
            print("跳过配置文件设置")
            return True
    
    shutil.copy('.env.example', '.env')
    print("✓ 配置文件已创建: .env")
    print("请编辑 .env 文件，配置你的网站信息和登录凭据")
    return True

def check_chrome():
    """检查Chrome浏览器"""
    print("\n检查Chrome浏览器...")
    
    # 尝试找到Chrome可执行文件
    chrome_paths = [
        '/usr/bin/google-chrome',
        '/usr/bin/chromium-browser',
        '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
        'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
        'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe'
    ]
    
    chrome_found = False
    for path in chrome_paths:
        if os.path.exists(path):
            print(f"✓ 找到Chrome浏览器: {path}")
            chrome_found = True
            break
    
    if not chrome_found:
        print("⚠ 未找到Chrome浏览器")
        print("请确保安装了Google Chrome浏览器")
        print("下载地址: https://www.google.com/chrome/")
    
    return chrome_found

def create_example_config():
    """创建示例配置文件指导"""
    example_sites = {
        "GitHub": {
            "WEBSITE_URL": "https://github.com",
            "LOGIN_URL": "https://github.com/login",
            "USERNAME_SELECTOR": "#login_field",
            "PASSWORD_SELECTOR": "#password",
            "LOGIN_BUTTON_SELECTOR": "input[type='submit']",
            "SUCCESS_INDICATOR_TYPE": "url",
            "SUCCESS_INDICATOR_VALUE": "dashboard"
        },
        "示例网站": {
            "WEBSITE_URL": "https://example.com",
            "LOGIN_URL": "https://example.com/login",
            "USERNAME_SELECTOR": "input[name='username']",
            "PASSWORD_SELECTOR": "input[name='password']",
            "LOGIN_BUTTON_SELECTOR": "button[type='submit']",
            "SUCCESS_INDICATOR_TYPE": "url",
            "SUCCESS_INDICATOR_VALUE": "dashboard"
        }
    }
    
    print("\n配置示例:")
    for site_name, config in example_sites.items():
        print(f"\n{site_name}:")
        for key, value in config.items():
            print(f"  {key}={value}")

def main():
    print("Cookie管理器安装和设置脚本")
    print("=" * 40)
    
    # 检查Python版本
    check_python_version()
    
    # 安装依赖
    if not install_dependencies():
        sys.exit(1)
    
    # 设置配置文件
    setup_config()
    
    # 检查Chrome浏览器
    check_chrome()
    
    # 显示配置示例
    create_example_config()
    
    print("\n" + "=" * 40)
    print("安装完成!")
    print("\n下一步:")
    print("1. 编辑 .env 文件，配置你的网站信息")
    print("2. 运行: python main.py help  # 查看使用帮助")
    print("3. 运行: python main.py once  # 测试一次")
    print("4. 运行: python main.py run   # 持续运行")

if __name__ == '__main__':
    main()