# 🍪 现代化Cookie管理器 v2.0

基于Python 3.12+的现代化Cookie自动管理工具，支持异步操作、类型安全、现代化UI。

## ✨ 主要特性

- 🚀 **异步架构** - 基于asyncio和Playwright的高性能异步操作
- 🔒 **类型安全** - 使用Pydantic进行数据验证和类型检查
- 🎨 **现代化UI** - 使用Rich库提供美观的命令行界面
- 📊 **结构化日志** - 使用structlog提供详细的操作日志
- 🗄️ **现代数据库** - 使用SQLAlchemy 2.0进行数据管理
- 🌐 **智能浏览器** - 使用Playwright进行可靠的网页自动化
- ⚙️ **灵活配置** - 支持环境变量和配置文件
- 🔄 **智能重试** - 自动检测、刷新和重新登录机制

## 🛠️ 技术栈

- **Python 3.12+** - 使用最新的Python特性
- **Playwright** - 现代化的网页自动化框架
- **SQLAlchemy 2.0** - 现代化的ORM框架  
- **Pydantic v2** - 数据验证和设置管理
- **Rich** - 美观的终端界面
- **structlog** - 结构化日志记录
- **Click** - 命令行接口框架

## 📦 安装

### 使用pip安装

```bash
# 克隆仓库
git clone <repository-url>
cd cookie_manager_v2

# 安装项目（开发模式）
pip install -e .

# 或者安装所有依赖
pip install -e ".[dev]"
```

### 使用Poetry安装

```bash
# 安装Poetry（如果未安装）
curl -sSL https://install.python-poetry.org | python3 -

# 安装依赖
poetry install

# 激活虚拟环境
poetry shell
```

### 安装Playwright浏览器

```bash
# 安装Playwright浏览器
playwright install chromium
```

## 🚀 快速开始

### 1. 初始化配置

```bash
# 初始化配置文件
cookie-manager init

# 复制环境变量文件
cp .env.example .env
```

### 2. 配置网站信息

编辑生成的 `config.json` 文件：

```json
{
  "websites": [
    {
      "name": "my_website",
      "url": "https://example.com",
      "username": "your_username", 
      "password": "your_password",
      "login": {
        "url": "https://example.com/login",
        "selectors": {
          "username": "input[name='username']",
          "password": "input[name='password']", 
          "login_button": "button[type='submit']"
        }
      },
      "validation": {
        "type": "url",
        "value": "dashboard"
      }
    }
  ]
}
```

### 3. 测试登录

```bash
# 测试所有网站登录
cookie-manager login

# 测试特定网站
cookie-manager login --website my_website
```

### 4. 启动监控

```bash
# 启动持续监控（默认30分钟检查一次）
cookie-manager run

# 自定义检查间隔（10分钟）
cookie-manager run --interval 10
```

## 📖 命令行使用

### 基本命令

```bash
# 查看帮助
cookie-manager --help

# 初始化配置
cookie-manager init

# 查看状态
cookie-manager status

# 检查所有网站Cookie
cookie-manager check

# 列出所有Cookie
cookie-manager list-cookies

# 删除指定网站Cookie
cookie-manager delete my_website
```

### 高级选项

```bash
# 启用调试模式
cookie-manager --debug status

# 使用自定义配置文件
cookie-manager --config /path/to/config.json run
```

## ⚙️ 配置详解

### 网站配置

每个网站需要配置以下信息：

```json
{
  "name": "网站标识符",
  "url": "网站主页URL", 
  "username": "登录用户名",
  "password": "登录密码",
  "login": {
    "url": "登录页面URL",
    "method": "form",
    "selectors": {
      "username": "用户名输入框选择器",
      "password": "密码输入框选择器", 
      "login_button": "登录按钮选择器",
      "remember_me": "记住我选择器（可选）"
    },
    "wait_after_login": 2,
    "page_load_timeout": 30
  },
  "validation": {
    "type": "url|element|text",
    "value": "验证值",
    "timeout": 10
  }
}
```

### 环境变量配置

主要的环境变量配置项：

```bash
# 数据库配置
DATABASE__URL=sqlite:///cookies.db

# 浏览器配置  
BROWSER__HEADLESS=true
BROWSER__TIMEOUT=30

# 调度器配置
SCHEDULER__CHECK_INTERVAL_MINUTES=30
SCHEDULER__COOKIE_EXPIRY_THRESHOLD_HOURS=24

# 日志配置
LOGGING__LEVEL=INFO
```

## 🔧 开发指南

### 项目结构

```
cookie_manager_v2/
├── cookie_manager/           # 主包
│   ├── __init__.py
│   ├── cli.py               # 命令行界面
│   ├── core/                # 核心功能
│   │   ├── manager.py       # 主管理器
│   │   ├── database.py      # 数据库管理
│   │   └── browser.py       # 浏览器管理
│   ├── models/              # 数据模型
│   │   ├── config.py        # 配置模型
│   │   ├── cookie.py        # Cookie模型
│   │   └── website.py       # 网站模型
│   └── utils/               # 工具函数
├── tests/                   # 测试文件
├── pyproject.toml           # 项目配置
├── README.md               # 文档
└── .env.example            # 环境变量示例
```

### 运行测试

```bash
# 运行所有测试
pytest

# 运行特定测试
pytest tests/test_manager.py

# 运行覆盖率测试
pytest --cov=cookie_manager
```

### 代码格式化

```bash
# 使用black格式化代码
black cookie_manager/

# 使用ruff检查代码
ruff check cookie_manager/

# 类型检查
mypy cookie_manager/
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📝 更新日志

### v2.0.0

- 🚀 完全重写，基于Python 3.12+
- ✨ 使用Playwright替代Selenium
- 📊 集成结构化日志系统
- 🎨 现代化命令行界面
- 🔒 完整的类型安全支持
- ⚡ 异步架构提升性能

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🆘 常见问题

### Q: 如何处理验证码？
A: 目前版本暂不支持自动处理验证码，建议使用记住登录状态的功能。

### Q: 支持哪些浏览器？
A: 默认使用Chromium，Playwright也支持Firefox和Safari。

### Q: 如何备份Cookie数据？
A: Cookie数据保存在SQLite数据库中，可以直接备份 `cookies.db` 文件。

### Q: 性能优化建议？
A: 可以调整检查间隔、启用无头模式、限制并发数量等。

---

如有问题或建议，请提交 [Issue](../../issues) 或联系维护者。
