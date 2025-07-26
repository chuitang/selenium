@echo off
REM Cookie管理工具部署脚本
REM 此脚本将创建完整的项目部署包

echo ===============================================
echo    Cookie管理工具 v1.0 部署脚本
echo ===============================================
echo.

REM 创建目录结构
echo 创建目录结构...
if not exist "dist" mkdir dist
if not exist "dist\config" mkdir dist\config
if not exist "dist\logs" mkdir dist\logs
if not exist "dist\data" mkdir dist\data

REM 检查Maven
echo 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请确保Maven已安装并添加到PATH
    pause
    exit /b 1
)

REM 编译并打包项目
echo.
echo 编译并打包项目...
mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 编译失败!
    pause
    exit /b 1
)

REM 复制文件到部署目录
echo.
echo 复制文件到部署目录...
copy target\cookie-manager.jar dist\ >nul
copy build-and-run.bat dist\ >nul
copy README.md dist\ >nul
copy src\main\resources\config.properties.example dist\config\ >nul

REM 创建启动脚本
echo.
echo 创建启动脚本...
(
echo @echo off
echo REM Cookie管理工具启动脚本
echo echo 启动Cookie管理工具...
echo java -jar cookie-manager.jar
echo pause
) > dist\start.bat

REM 创建配置向导脚本
echo.
echo 创建配置向导脚本...
(
echo @echo off
echo echo ===============================================
echo echo    Cookie管理工具配置向导
echo echo ===============================================
echo echo.
echo if not exist "config.properties" (
echo     echo 首次运行，复制配置文件模板...
echo     copy config\config.properties.example config.properties
echo     echo 配置文件已创建: config.properties
echo     echo 请编辑此文件并填入您的网站信息
echo     echo.
echo     echo 配置完成后，请运行 start.bat 启动程序
echo     pause
echo     notepad config.properties
echo ^) else (
echo     echo 配置文件已存在
echo     echo 是否要编辑配置文件? (Y/N^)
echo     set /p choice=
echo     if /i "%%choice%%"=="Y" notepad config.properties
echo ^)
) > dist\setup.bat

REM 创建使用说明
echo.
echo 创建使用说明...
(
echo Cookie管理工具 v1.0 - 使用说明
echo =====================================
echo.
echo 文件说明:
echo   cookie-manager.jar       - 主程序文件
echo   setup.bat               - 配置向导
echo   start.bat               - 启动程序
echo   build-and-run.bat       - 构建和运行脚本
echo   config.properties       - 配置文件 ^(首次运行时创建^)
echo   README.md               - 详细说明文档
echo.
echo 快速开始:
echo   1. 运行 setup.bat 进行初始配置
echo   2. 编辑 config.properties 文件，填入您的网站信息
echo   3. 运行 start.bat 启动程序
echo.
echo 系统要求:
echo   - JDK 21 或更高版本
echo   - Google Chrome 浏览器
echo   - Windows 10/11 操作系统
echo.
echo 配置示例:
echo   target.url=https://example.com/dashboard
echo   login.url=https://example.com/login
echo   login.username=your_username
echo   login.password=your_password
echo.
echo 更多信息请查看 README.md 文件
) > dist\使用说明.txt

REM 创建卸载脚本
echo.
echo 创建卸载脚本...
(
echo @echo off
echo echo 准备卸载Cookie管理工具...
echo echo 这将删除所有程序文件和数据文件
echo echo 是否继续? (Y/N^)
echo set /p choice=
echo if /i "%%choice%%"=="Y" (
echo     echo 正在删除文件...
echo     del /q *.jar *.bat *.txt *.md 2^>nul
echo     rd /s /q config logs data 2^>nul
echo     del /q *.mv.db *.trace.db 2^>nul
echo     echo 卸载完成
echo ^) else (
echo     echo 取消卸载
echo ^)
echo pause
) > dist\uninstall.bat

echo.
echo ===============================================
echo 部署完成!
echo ===============================================
echo.
echo 部署包位置: dist\
echo.
echo 包含文件:
echo   ✓ cookie-manager.jar (主程序)
echo   ✓ setup.bat (配置向导)
echo   ✓ start.bat (启动脚本)
echo   ✓ build-and-run.bat (构建脚本)
echo   ✓ uninstall.bat (卸载脚本)
echo   ✓ 使用说明.txt (快速说明)
echo   ✓ README.md (详细文档)
echo   ✓ config\ (配置模板目录)
echo.
echo 使用步骤:
echo   1. 将 dist\ 目录复制到目标计算机
echo   2. 在目标计算机上运行 setup.bat
echo   3. 编辑配置文件
echo   4. 运行 start.bat 启动程序
echo.
pause