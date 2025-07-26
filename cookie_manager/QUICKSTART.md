# Cookie管理器快速入门

## 🚀 快速开始

### 1. 安装和设置

```bash
# 运行自动安装脚本
python setup.py

# 或者手动安装
pip install -r requirements.txt
cp .env.example .env
```

### 2. 配置网站信息

编辑 `.env` 文件：

```env
# 必填项
WEBSITE_URL=https://your-website.com
LOGIN_URL=https://your-website.com/login
USERNAME=your_username
PASSWORD=your_password

# 根据网站调整选择器
USERNAME_SELECTOR=input[name="username"]
PASSWORD_SELECTOR=input[name="password"]
LOGIN_BUTTON_SELECTOR=button[type="submit"]

# 登录成功判断
SUCCESS_INDICATOR_TYPE=url
SUCCESS_INDICATOR_VALUE=dashboard
```

### 3. 测试工具

```bash
# 运行测试确保一切正常
python test_tool.py

# 测试一次登录
python main.py login --debug
```

### 4. 开始使用

```bash
# 持续运行（推荐）
python main.py run

# 或者只运行一次
python main.py once
```

## 📝 常用命令

```bash
python main.py help       # 查看帮助
python main.py status     # 查看状态
python main.py login      # 强制重新登录
python main.py run        # 持续运行
python main.py once       # 执行一次检查
```

## 🔧 配置说明

### 选择器配置

需要根据目标网站的HTML结构配置CSS选择器：

1. 打开网站登录页面
2. 使用浏览器开发者工具（F12）
3. 找到用户名、密码输入框和登录按钮的选择器
4. 更新 `.env` 文件中的对应配置

### 成功判断配置

- **URL判断**：登录成功后URL包含某个关键字
  ```env
  SUCCESS_INDICATOR_TYPE=url
  SUCCESS_INDICATOR_VALUE=dashboard
  ```

- **元素判断**：登录成功后页面包含某个元素
  ```env
  SUCCESS_INDICATOR_TYPE=element
  SUCCESS_INDICATOR_VALUE=.user-menu
  ```

## 🛠️ 故障排除

### 常见问题

1. **找不到页面元素**
   - 检查CSS选择器是否正确
   - 尝试使用更通用的选择器
   - 增加等待时间

2. **Chrome/ChromeDriver问题**
   - 确保安装了Google Chrome
   - ChromeDriver会自动下载，如有问题可手动安装

3. **网站有验证码**
   - 目前不支持验证码，需要使用没有验证码的网站
   - 或者考虑其他认证方式

### 调试模式

```bash
python main.py once --debug
```

这会显示详细的运行信息，帮助诊断问题。

## 📊 监控和日志

- **日志文件**：`cookie_manager.log`
- **数据库**：`cookies.db`
- **状态查看**：`python main.py status`

## 🔒 安全建议

- 不要在公共环境存储登录凭据
- 定期更换密码
- 注意目标网站的使用条款
- 考虑使用环境变量存储敏感信息

## 🎯 使用场景

- 自动维护网站登录状态
- 爬虫项目的登录管理
- API调用的认证token管理
- 定时任务的身份验证

## 📞 获取帮助

如遇问题，请：

1. 查看日志文件
2. 运行测试工具
3. 使用调试模式
4. 检查网站是否有变化