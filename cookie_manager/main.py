#!/usr/bin/env python3
"""
Cookie管理器主程序
自动登录网站并管理cookies，定时检查有效性
"""

import os
import sys
import argparse
import logging
from scheduler import CookieScheduler
from web_client import WebClient
from database import CookieDatabase
from config import Config

def setup_environment():
    """设置环境"""
    # 检查.env文件是否存在
    if not os.path.exists('.env'):
        print("警告: .env文件不存在")
        print("请复制 .env.example 为 .env 并配置相关参数")
        
        if input("是否要创建默认的.env文件? (y/n): ").lower() == 'y':
            import shutil
            if os.path.exists('.env.example'):
                shutil.copy('.env.example', '.env')
                print("已创建 .env 文件，请编辑后重新运行")
            else:
                print("找不到 .env.example 文件")
        return False
    return True

def print_status():
    """打印当前状态"""
    scheduler = CookieScheduler()
    status = scheduler.get_status()
    
    print("\n=== Cookie管理器状态 ===")
    print(f"目标网站: {status['website_url']}")
    print(f"调度器运行中: {'是' if status['scheduler_running'] else '否'}")
    print(f"检查间隔: {status['check_interval_minutes']} 分钟")
    print(f"有效Cookies: {'是' if status['has_valid_cookies'] else '否'}")
    
    if status['last_update']:
        print(f"最后更新: {status['last_update']}")
    if status['last_verified']:
        print(f"最后验证: {status['last_verified']}")
    
    if status['recent_logs']:
        print("\n=== 最近日志 ===")
        for log in status['recent_logs'][:5]:
            status_icon = "✓" if log['success'] else "✗"
            print(f"{status_icon} {log['timestamp']} - {log['action']}: {log['message'] or '无消息'}")

def print_help():
    """打印帮助信息"""
    help_text = """
Cookie管理器 - 自动登录和Cookie管理工具

使用方法:
    python main.py [命令] [选项]

命令:
    run         启动持续运行模式（默认）
    once        执行一次检查
    login       强制重新登录
    status      显示当前状态
    help        显示此帮助信息

选项:
    --headless  无头模式运行（不显示浏览器窗口）
    --debug     启用调试模式
    --config    指定配置文件路径

示例:
    python main.py run                  # 持续运行
    python main.py once --debug         # 执行一次并显示调试信息
    python main.py login --headless     # 强制重新登录（无头模式）
    python main.py status               # 查看状态

配置:
    请确保 .env 文件已正确配置，包含目标网站信息和登录凭据。
    可以复制 .env.example 文件开始配置。
    """
    print(help_text)

def main():
    parser = argparse.ArgumentParser(description='Cookie管理器', add_help=False)
    parser.add_argument('command', nargs='?', default='run',
                       choices=['run', 'once', 'login', 'status', 'help'])
    parser.add_argument('--headless', action='store_true', help='无头模式运行')
    parser.add_argument('--debug', action='store_true', help='启用调试模式')
    parser.add_argument('--config', help='配置文件路径')
    parser.add_argument('--help', '-h', action='store_true', help='显示帮助信息')
    
    args = parser.parse_args()
    
    if args.help or args.command == 'help':
        print_help()
        return
    
    # 设置日志级别
    if args.debug:
        logging.basicConfig(level=logging.DEBUG)
    
    # 检查环境配置
    if not setup_environment():
        return
    
    # 如果指定了无头模式，临时设置环境变量
    if args.headless:
        os.environ['HEADLESS'] = 'True'
    
    try:
        scheduler = CookieScheduler()
        
        if args.command == 'run':
            print("启动Cookie管理器（持续运行模式）...")
            print("按 Ctrl+C 停止运行")
            scheduler.run_continuous()
            
        elif args.command == 'once':
            print("执行一次Cookie检查...")
            scheduler.run_once()
            
        elif args.command == 'login':
            print("强制重新登录...")
            success = scheduler.force_login()
            if success:
                print("重新登录成功!")
            else:
                print("重新登录失败!")
                sys.exit(1)
                
        elif args.command == 'status':
            print_status()
            
    except KeyboardInterrupt:
        print("\n程序已停止")
    except Exception as e:
        print(f"程序运行出错: {e}")
        if args.debug:
            import traceback
            traceback.print_exc()
        sys.exit(1)

if __name__ == '__main__':
    main()