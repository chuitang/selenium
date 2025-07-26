@echo off
REM Cookie管理工具构建和运行脚本
REM 适用于Windows环境

echo ===============================================
echo    Cookie管理工具 v1.0 构建和运行脚本
echo ===============================================
echo.

REM 检查Java版本
echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请确保JDK 21已安装并添加到PATH
    pause
    exit /b 1
)

REM 检查Maven
echo 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请确保Maven已安装并添加到PATH
    pause
    exit /b 1
)

REM 显示菜单
:menu
echo.
echo 请选择操作:
echo 1. 编译项目
echo 2. 运行项目 (交互模式)
echo 3. 编译并运行
echo 4. 清理项目
echo 5. 测试配置
echo 6. 手动登录
echo 7. 验证Cookie
echo 8. 退出
echo.
set /p choice=请输入选择 (1-8): 

if "%choice%"=="1" goto compile
if "%choice%"=="2" goto run
if "%choice%"=="3" goto compile_and_run
if "%choice%"=="4" goto clean
if "%choice%"=="5" goto test_config
if "%choice%"=="6" goto manual_login
if "%choice%"=="7" goto validate_cookie
if "%choice%"=="8" goto exit
echo 无效选择，请重新选择
goto menu

:compile
echo.
echo 编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 编译失败!
    pause
    goto menu
)
echo 编译成功!
pause
goto menu

:run
echo.
echo 运行项目 (交互模式)...
if not exist "target\cookie-manager.jar" (
    echo Cookie Manager JAR文件不存在，请先编译项目
    pause
    goto menu
)
java -jar target\cookie-manager.jar
pause
goto menu

:compile_and_run
echo.
echo 编译并打包项目...
mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 编译失败!
    pause
    goto menu
)
echo 编译成功!
echo.
echo 运行项目...
java -jar target\cookie-manager.jar
pause
goto menu

:clean
echo.
echo 清理项目...
mvn clean
echo 清理完成!
pause
goto menu

:test_config
echo.
echo 测试配置...
if not exist "target\cookie-manager.jar" (
    echo Cookie Manager JAR文件不存在，请先编译项目
    pause
    goto menu
)
java -jar target\cookie-manager.jar test
pause
goto menu

:manual_login
echo.
echo 执行手动登录...
if not exist "target\cookie-manager.jar" (
    echo Cookie Manager JAR文件不存在，请先编译项目
    pause
    goto menu
)
java -jar target\cookie-manager.jar login
pause
goto menu

:validate_cookie
echo.
echo 验证当前Cookie...
if not exist "target\cookie-manager.jar" (
    echo Cookie Manager JAR文件不存在，请先编译项目
    pause
    goto menu
)
java -jar target\cookie-manager.jar validate
pause
goto menu

:exit
echo.
echo 感谢使用Cookie管理工具!
exit /b 0