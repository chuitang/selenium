# Cookie管理器代理配置指南

Cookie管理器v2.0现在支持HTTP/HTTPS/SOCKS代理，解决连接超时问题。

## 🌐 支持的代理类型

- **HTTP代理**: `http://proxy.example.com:8080`
- **HTTPS代理**: `https://proxy.example.com:8080`
- **SOCKS4代理**: `socks4://proxy.example.com:1080`
- **SOCKS5代理**: `socks5://proxy.example.com:1080`

## ⚙️ 配置方法

### 方法1: 环境变量配置（推荐）

在`.env`文件中添加：

```env
# 代理服务器地址（必需）
BROWSER__PROXY_SERVER=http://127.0.0.1:7890

# 代理认证（可选）
BROWSER__PROXY_USERNAME=proxy_user
BROWSER__PROXY_PASSWORD=proxy_pass
```

### 方法2: 直接设置环境变量

```bash
export BROWSER__PROXY_SERVER=http://127.0.0.1:7890
export BROWSER__PROXY_USERNAME=proxy_user
export BROWSER__PROXY_PASSWORD=proxy_pass
```

## 🔧 常见代理配置示例

### Clash代理
```env
BROWSER__PROXY_SERVER=http://127.0.0.1:7890
```

### V2Ray代理
```env
BROWSER__PROXY_SERVER=socks5://127.0.0.1:1080
```

### 企业代理
```env
BROWSER__PROXY_SERVER=http://proxy.company.com:8080
BROWSER__PROXY_USERNAME=your_username
BROWSER__PROXY_PASSWORD=your_password
```

### 无认证的SOCKS5代理
```env
BROWSER__PROXY_SERVER=socks5://127.0.0.1:1080
```

## 🧪 测试代理配置

1. **运行测试脚本**:
   ```bash
   python test_proxy.py
   ```

2. **检查配置是否正确**:
   ```bash
   cookie-manager init
   # 查看输出中的代理配置说明
   ```

## 🚀 使用代理运行Cookie管理器

1. **配置代理**（在`.env`文件中）
2. **初始化配置**:
   ```bash
   cookie-manager init
   ```
3. **编辑配置文件** (`config.json`)
4. **运行工具**:
   ```bash
   cookie-manager login
   ```

## 🔍 故障排查

### 连接超时问题
如果遇到连接超时：
1. 确认代理服务器地址正确
2. 检查代理服务器是否正常运行
3. 验证代理认证信息（如果需要）

### 代理配置无效
如果代理配置不生效：
1. 检查环境变量名称是否正确（注意双下划线`__`）
2. 重新启动应用程序
3. 查看日志输出确认代理配置已加载

### 常见错误

**错误**: `代理服务器地址必须以http://、https://、socks4://或socks5://开头`
**解决**: 确保代理URL包含正确的协议前缀

**错误**: `Connection timeout`
**解决**: 检查代理服务器是否可达，尝试不同的代理服务器

## 📋 配置检查清单

- [ ] 代理服务器地址格式正确
- [ ] 代理服务器正在运行
- [ ] 代理认证信息正确（如果需要）
- [ ] 环境变量名称正确（BROWSER__PROXY_SERVER）
- [ ] 重新启动应用程序加载新配置

## 💡 提示

- 代理配置在浏览器级别设置，影响所有网站访问
- 支持无认证和需要认证的代理
- 可以随时修改`.env`文件中的代理配置
- 如果不需要代理，删除或注释相关环境变量即可