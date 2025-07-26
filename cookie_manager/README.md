# Cookie管理器

一个自动登录网站并管理cookies的Python工具，支持定时检查cookie有效性、自动刷新和重新登录。

## 功能特性

- ✅ 自动登录网站并保存cookies到SQLite数据库
- ✅ 定时检查cookies有效性
- ✅ 自动尝试刷新失效的cookies
- ✅ 失效时自动重新登录
- ✅ 支持无头浏览器模式
- ✅ 详细的操作日志记录
- ✅ 命令行界面，易于使用和集成
- ✅ 灵活的配置系统

## 安装

1. 克隆或下载项目文件
2. 安装依赖：

```bash
pip install -r requirements.txt
```

3. 安装Chrome浏览器和ChromeDriver（用于Selenium）

## 配置

1. 复制配置文件：
```bash
cp .env.example .env
```

2. 编辑 `.env` 文件，配置你的网站信息：

```env
# 网站配置
WEBSITE_URL=https://example.com
LOGIN_URL=https://example.com/login

# 登录凭据
USERNAME=your_username
PASSWORD=your_password

# 页面元素选择器（需要根据实际网站调整）
USERNAME_SELECTOR=input[name="username"]
PASSWORD_SELECTOR=input[name="password"]
LOGIN_BUTTON_SELECTOR=button[type="submit"]

# 检查间隔（分钟）
CHECK_INTERVAL_MINUTES=30

# 其他配置...
```

### 重要配置说明

- **选择器配置**：需要根据目标网站的实际HTML结构调整CSS选择器
- **成功指示器**：配置如何判断登录成功
  - `SUCCESS_INDICATOR_TYPE=url`：通过URL判断（URL包含某个关键字）
  - `SUCCESS_INDICATOR_TYPE=element`：通过页面元素判断（页面包含某个CSS选择器）

## 使用方法

### 基本命令

```bash
# 持续运行模式（推荐）
python main.py run

# 执行一次检查
python main.py once

# 强制重新登录
python main.py login

# 查看状态
python main.py status

# 显示帮助
python main.py help
```

### 命令选项

```bash
# 无头模式运行（不显示浏览器窗口）
python main.py run --headless

# 启用调试模式
python main.py once --debug

# 强制重新登录（无头模式）
python main.py login --headless
```

## 工作流程

1. **初始登录**：首次运行时自动登录网站并保存cookies
2. **定时检查**：每隔指定时间检查cookies是否仍然有效
3. **验证失败**：如果cookies失效，先尝试刷新cookies
4. **刷新失败**：如果刷新也失败，则重新执行完整登录流程
5. **日志记录**：所有操作都会记录到数据库和日志文件

## 文件结构

```
cookie_manager/
├── main.py              # 主程序入口
├── config.py            # 配置管理
├── database.py          # 数据库操作
├── web_client.py        # 网页自动化客户端
├── scheduler.py         # 定时任务调度器
├── requirements.txt     # Python依赖
├── .env.example         # 配置文件示例
└── README.md           # 说明文档
```

## 数据库

工具使用SQLite数据库存储：

- **cookies表**：存储网站cookies和相关信息
- **login_logs表**：记录所有操作日志

数据库文件默认为 `cookies.db`，可通过配置修改。

## 日志

- **控制台日志**：实时显示运行状态
- **文件日志**：保存到 `cookie_manager.log` 文件
- **数据库日志**：详细操作记录存储在数据库中

## 高级用法

### 在后台运行

```bash
# Linux/Mac后台运行
nohup python main.py run --headless > /dev/null 2>&1 &

# 使用systemd服务（Linux）
# 创建服务文件 /etc/systemd/system/cookie-manager.service
```

### 定制化配置

可以通过环境变量或修改配置文件来定制：

- 检查间隔时间
- 浏览器选项
- 重试次数和延迟
- 成功判断条件

## 故障排除

### 常见问题

1. **找不到页面元素**
   - 检查CSS选择器是否正确
   - 网站可能使用了动态加载，需要增加等待时间

2. **登录失败**
   - 确认用户名和密码正确
   - 检查网站是否有验证码或其他安全措施
   - 尝试手动登录确认流程

3. **ChromeDriver问题**
   - 确保安装了Chrome浏览器
   - ChromeDriver版本需要与Chrome版本兼容

### 调试模式

使用 `--debug` 选项获取详细日志：

```bash
python main.py once --debug
```

## 安全注意事项

- 不要在公共环境中存储登录凭据
- 定期更换密码
- 考虑使用环境变量而不是配置文件存储敏感信息
- 注意目标网站的使用条款

## 扩展开发

工具采用模块化设计，可以轻松扩展：

- 支持更多浏览器（Firefox、Edge等）
- 添加更多验证方式
- 集成其他数据库（MySQL、PostgreSQL等）
- 添加Web界面管理

## 许可证

本项目使用MIT许可证。