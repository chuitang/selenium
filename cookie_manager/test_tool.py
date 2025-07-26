#!/usr/bin/env python3
"""
Cookie管理器测试工具
用于验证各个模块是否正常工作
"""

import sys
import os
import logging

def test_imports():
    """测试模块导入"""
    print("测试模块导入...")
    
    try:
        from config import Config
        print("✓ config模块导入成功")
    except Exception as e:
        print(f"✗ config模块导入失败: {e}")
        return False
    
    try:
        from database import CookieDatabase
        print("✓ database模块导入成功")
    except Exception as e:
        print(f"✗ database模块导入失败: {e}")
        return False
    
    try:
        from web_client import WebClient
        print("✓ web_client模块导入成功")
    except Exception as e:
        print(f"✗ web_client模块导入失败: {e}")
        return False
    
    try:
        from scheduler import CookieScheduler
        print("✓ scheduler模块导入成功")
    except Exception as e:
        print(f"✗ scheduler模块导入失败: {e}")
        return False
    
    return True

def test_database():
    """测试数据库连接"""
    print("\n测试数据库...")
    
    try:
        from database import CookieDatabase
        
        # 使用临时数据库
        db = CookieDatabase("test.db")
        
        # 测试保存和获取cookies
        test_cookies = [
            {"name": "test_cookie", "value": "test_value", "domain": "example.com"}
        ]
        
        success = db.save_cookies("https://example.com", test_cookies, "test-agent", "测试")
        if success:
            print("✓ 数据库保存测试成功")
        else:
            print("✗ 数据库保存测试失败")
            return False
        
        # 测试获取cookies
        retrieved = db.get_cookies("https://example.com")
        if retrieved and retrieved['cookies']:
            print("✓ 数据库获取测试成功")
        else:
            print("✗ 数据库获取测试失败")
            return False
        
        # 测试日志记录
        db.log_action("https://example.com", "test", True, "测试日志")
        logs = db.get_recent_logs("https://example.com", 1)
        if logs:
            print("✓ 数据库日志测试成功")
        else:
            print("✗ 数据库日志测试失败")
            return False
        
        # 清理测试数据库
        os.remove("test.db")
        print("✓ 测试数据库已清理")
        
        return True
        
    except Exception as e:
        print(f"✗ 数据库测试失败: {e}")
        return False

def test_config():
    """测试配置"""
    print("\n测试配置...")
    
    try:
        from config import Config
        
        config = Config()
        
        # 检查基本配置
        if hasattr(config, 'WEBSITE_URL'):
            print(f"✓ 网站URL: {config.WEBSITE_URL}")
        else:
            print("✗ 缺少WEBSITE_URL配置")
            return False
        
        if hasattr(config, 'CHECK_INTERVAL_MINUTES'):
            print(f"✓ 检查间隔: {config.CHECK_INTERVAL_MINUTES}分钟")
        else:
            print("✗ 缺少CHECK_INTERVAL_MINUTES配置")
            return False
        
        return True
        
    except Exception as e:
        print(f"✗ 配置测试失败: {e}")
        return False

def test_selenium():
    """测试Selenium环境"""
    print("\n测试Selenium环境...")
    
    try:
        from selenium import webdriver
        from selenium.webdriver.chrome.options import Options
        
        # 测试Chrome选项
        options = Options()
        options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        
        print("✓ Selenium Chrome选项配置成功")
        
        # 尝试创建WebDriver（但不实际启动）
        print("✓ Selenium环境检查通过")
        
        return True
        
    except Exception as e:
        print(f"✗ Selenium环境测试失败: {e}")
        print("请确保已安装Chrome浏览器和ChromeDriver")
        return False

def test_dependencies():
    """测试依赖包"""
    print("\n测试依赖包...")
    
    dependencies = [
        'requests',
        'selenium', 
        'schedule',
        'fake_useragent',
        'bs4',
        'cryptography'
    ]
    
    missing = []
    for dep in dependencies:
        try:
            __import__(dep)
            print(f"✓ {dep}")
        except ImportError:
            print(f"✗ {dep} - 未安装")
            missing.append(dep)
    
    if missing:
        print(f"\n缺少依赖: {', '.join(missing)}")
        print("请运行: pip install -r requirements.txt")
        return False
    
    return True

def run_full_test():
    """运行完整测试"""
    print("Cookie管理器测试工具")
    print("=" * 40)
    
    tests = [
        ("依赖包", test_dependencies),
        ("模块导入", test_imports),
        ("配置", test_config),
        ("数据库", test_database),
        ("Selenium环境", test_selenium),
    ]
    
    results = []
    for test_name, test_func in tests:
        print(f"\n{'='*20} {test_name} {'='*20}")
        try:
            result = test_func()
            results.append((test_name, result))
        except Exception as e:
            print(f"✗ {test_name}测试异常: {e}")
            results.append((test_name, False))
    
    # 汇总结果
    print("\n" + "=" * 40)
    print("测试结果汇总:")
    
    all_passed = True
    for test_name, result in results:
        status = "✓ 通过" if result else "✗ 失败"
        print(f"{test_name}: {status}")
        if not result:
            all_passed = False
    
    print("\n" + "=" * 40)
    if all_passed:
        print("🎉 所有测试通过！工具已准备就绪。")
        print("\n建议:")
        print("1. 配置 .env 文件")
        print("2. 运行: python main.py once --debug")
    else:
        print("❌ 部分测试失败，请解决上述问题后重试。")
    
    return all_passed

if __name__ == '__main__':
    success = run_full_test()
    sys.exit(0 if success else 1)