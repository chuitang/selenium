# Cookie管理工具 v1.0

一个基于JDK 21和Selenium WebDriver的自动登录和Cookie管理工具，专为Windows环境设计。

## 功能特性

- ✅ **自动登录** - 使用Selenium WebDriver自动执行网站登录
- ✅ **Cookie管理** - 自动保存和管理登录后的Cookie到数据库
- ✅ **定时检查** - 定期检查Cookie是否失效
- ✅ **智能刷新** - 尝试使用旧Cookie刷新获取新Cookie
- ✅ **自动重登** - Cookie无效时自动重新登录
- ✅ **数据库存储** - 使用H2数据库存储Cookie历史记录
- ✅ **交互界面** - 友好的命令行交互界面
- ✅ **批处理脚本** - Windows批处理脚本简化操作

## 系统要求

### 必需环境
- **JDK 21** 或更高版本
- **Apache Maven 3.6+**
- **Google Chrome 浏览器**
- **Windows 10/11** 操作系统

### 可选组件
- **ChromeDriver** (工具会自动下载管理)

## 快速开始

### 1. 环境准备

确保已安装以下软件：

```bash
# 检查Java版本 (需要JDK 21+)
java -version

# 检查Maven版本
mvn -version

# 检查Chrome浏览器
"C:\Program Files\Google\Chrome\Application\chrome.exe" --version
```

### 2. 下载和配置

1. 克隆或下载项目到本地
2. 复制配置文件模板：
   ```bash
   copy src\main\resources\config.properties.example src\main\resources\config.properties
   ```
3. 编辑 `config.properties` 文件，设置你的目标网站信息

### 3. 编译和运行

#### 方法一：使用批处理脚本（推荐）
```bash
# 双击运行批处理脚本
build-and-run.bat
```

#### 方法二：使用Maven命令
```bash
# 编译并打包
mvn clean package -DskipTests

# 运行 (交互模式)
java -jar target/cookie-manager.jar

# 或者使用命令行参数
java -jar target/cookie-manager.jar start    # 启动服务
java -jar target/cookie-manager.jar test     # 测试配置
java -jar target/cookie-manager.jar login    # 手动登录
java -jar target/cookie-manager.jar validate # 验证Cookie
```

## 配置说明

编辑 `src/main/resources/config.properties` 文件：

```properties
# ========== 网站配置 ==========
# 目标网站URL (必需)
target.url=https://example.com/dashboard

# 登录页面URL (必需)  
login.url=https://example.com/login

# 登录用户名和密码 (必需)
login.username=your_username
login.password=your_password

# ========== 登录元素选择器 ==========
# 用户名输入框选择器 (支持多种格式)
login.username.field=username          # name属性
# login.username.field=#username       # ID选择器
# login.username.field=.username-input # Class选择器
# login.username.field=[type="text"]   # CSS选择器
# login.username.field=//input[@name="username"] # XPath选择器

# 密码输入框选择器
login.password.field=password

# 登录按钮选择器
login.button.selector=input[type='submit']

# ========== Cookie检查配置 ==========
# Cookie检查间隔(分钟)
cookie.check.interval.minutes=30

# ========== WebDriver配置 ==========
# 是否使用无头模式 (true=后台运行，false=显示浏览器)
webdriver.headless=true
```

## 使用指南

### 交互模式

运行程序后进入交互模式：

```
> config    # 配置工具参数
> start     # 启动Cookie管理服务
> stop      # 停止服务
> status    # 查看服务状态
> test      # 测试配置
> login     # 手动执行登录
> validate  # 验证当前Cookie
> cookies   # 查看Cookie历史
> help      # 显示帮助
> exit      # 退出程序
```

### 命令行模式

```bash
java -jar cookie-manager.jar [command]

Commands:
  start    - 启动Cookie管理服务
  test     - 测试配置
  login    - 手动登录
  validate - 验证Cookie
  help     - 显示帮助
```

### 工作流程

1. **初始配置**: 使用 `config` 命令或编辑配置文件
2. **测试配置**: 使用 `test` 命令验证配置正确性
3. **手动登录**: 使用 `login` 命令进行首次登录测试
4. **启动服务**: 使用 `start` 命令启动自动化服务
5. **监控运行**: 查看日志输出，确保正常运行

## 元素选择器指南

Cookie管理工具支持多种元素定位方式：

### 1. Name属性 (推荐)
```properties
login.username.field=username
```

### 2. ID选择器
```properties
login.username.field=#login-username
```

### 3. Class选择器
```properties
login.username.field=.form-control
```

### 4. CSS选择器
```properties
login.username.field=input[name="username"]
login.password.field=input[type="password"]
login.button.selector=button[type="submit"]
```

### 5. XPath选择器
```properties
login.username.field=//input[@placeholder="用户名"]
login.password.field=//input[@placeholder="密码"]
```

## 数据库管理

工具使用H2数据库存储Cookie历史：

- **数据库文件**: `cookiedb.mv.db` (自动创建)
- **访问控制台**: 在浏览器访问 `http://localhost:8082` (如果启用H2控制台)
- **连接URL**: `jdbc:h2:./cookiedb`
- **用户名**: `sa`
- **密码**: (空)

## 日志管理

日志配置文件位于 `src/main/resources/logback.xml`：

- **日志级别**: INFO (可调整为DEBUG、WARN、ERROR)
- **日志文件**: `logs/cookie-manager.log`
- **控制台输出**: 彩色格式化输出

## 故障排除

### 常见问题

#### 1. ChromeDriver不匹配
```
错误: SessionNotCreatedException: session not created: This version of ChromeDriver only supports Chrome version XX
```
**解决方案**: 工具会自动下载匹配的ChromeDriver，如果仍有问题，请更新Chrome浏览器。

#### 2. 元素定位失败
```
错误: NoSuchElementException: no such element: Unable to locate element
```
**解决方案**: 
- 检查选择器是否正确
- 使用浏览器开发者工具验证元素选择器
- 尝试不同的定位方式

#### 3. 登录失败
```
错误: 登录失败或获取的Cookie无效
```
**解决方案**:
- 验证用户名和密码
- 检查登录页面是否有验证码
- 确认登录后的页面URL变化

#### 4. 数据库连接失败
```
错误: 数据库连接测试失败
```
**解决方案**:
- 确保没有其他程序占用数据库文件
- 检查磁盘空间
- 尝试删除 `cookiedb.mv.db` 文件重新创建

### 调试模式

启用详细日志进行调试：

1. 编辑 `config.properties`:
   ```properties
   webdriver.headless=false  # 显示浏览器窗口
   ```

2. 修改日志级别为DEBUG:
   ```properties
   logging.level=DEBUG
   ```

## 高级配置

### 自定义WebDriver路径
```properties
webdriver.path=C:\\path\\to\\chromedriver.exe
```

### 调整超时设置
```properties
webdriver.page.load.timeout.seconds=60
webdriver.implicit.wait.seconds=20
```

### 数据库配置
```properties
# 使用外部数据库
database.url=jdbc:mysql://localhost:3306/cookies
database.username=root
database.password=password
```

## 部署和分发

### 打包分发版本
```bash
# 创建完整的分发包
mvn clean package -DskipTests

# 分发包包含:
# - cookie-manager.jar (主程序)
# - config.properties.example (配置模板)
# - build-and-run.bat (启动脚本)
# - README.md (说明文档)
```

### Windows服务安装

可以使用第三方工具(如NSSM)将工具安装为Windows服务：

```bash
# 下载NSSM
# 安装服务
nssm install CookieManager "C:\path\to\java.exe" "-jar C:\path\to\cookie-manager.jar start"

# 启动服务
nssm start CookieManager
```

## 许可证

本项目基于MIT许可证开源。

## 更新日志

### v1.0.0 (2024-01-XX)
- 初始版本发布
- 支持自动登录和Cookie管理
- 集成H2数据库
- 提供Windows批处理脚本
- 完整的命令行和交互模式

## 技术支持

如有问题或建议，请通过以下方式联系：

- 创建Issue报告问题
- 提交Pull Request贡献代码
- 发送邮件至技术支持

---

**注意**: 请遵守目标网站的使用条款和robots.txt规则，合理使用本工具。