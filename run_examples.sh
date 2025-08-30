#!/bin/bash

echo "=== Jolt Java示例执行脚本 ==="
echo

# 检查Java版本
echo "1. 检查Java环境"
echo "================"
if command -v java &> /dev/null; then
    java -version
    echo
else
    echo "错误: 未找到Java环境"
    echo "请安装Java 8或更高版本"
    exit 1
fi

# 检查Maven
echo "2. 检查构建工具"
echo "==============="
if command -v mvn &> /dev/null; then
    echo "Maven版本:"
    mvn -version | head -1
    BUILD_TOOL="maven"
elif command -v gradle &> /dev/null; then
    echo "Gradle版本:"
    gradle -version | grep "Gradle"
    BUILD_TOOL="gradle"
else
    echo "警告: 未找到Maven或Gradle"
    echo "将尝试直接编译运行"
    BUILD_TOOL="direct"
fi
echo

# 编译和运行
echo "3. 编译和运行示例"
echo "=================="

case $BUILD_TOOL in
    "maven")
        echo "使用Maven编译..."
        mvn clean compile
        if [ $? -eq 0 ]; then
            echo "✓ 编译成功"
            echo
            echo "运行使用示例:"
            mvn exec:java -Dexec.mainClass="com.example.JoltUsageExamples"
            echo
            echo "运行单元测试:"
            mvn test
        else
            echo "✗ Maven编译失败"
        fi
        ;;
    "gradle")
        echo "使用Gradle编译..."
        ./gradlew build
        if [ $? -eq 0 ]; then
            echo "✓ 编译成功"
            echo
            echo "运行使用示例:"
            ./gradlew run
            echo
            echo "运行单元测试:"
            ./gradlew test
        else
            echo "✗ Gradle编译失败"
        fi
        ;;
    "direct")
        echo "尝试直接编译..."
        echo "注意: 需要手动下载Jolt依赖JAR文件到lib目录"
        echo
        echo "创建lib目录并下载依赖:"
        mkdir -p lib
        echo "请手动下载以下JAR文件到lib目录:"
        echo "- jolt-core-0.1.7.jar"
        echo "- json-utils-0.1.7.jar" 
        echo "- jackson-databind-2.15.2.jar"
        echo "- jackson-core-2.15.2.jar"
        echo "- jackson-annotations-2.15.2.jar"
        echo
        echo "然后运行:"
        echo "javac -cp \"lib/*\" src/main/java/com/example/*.java"
        echo "java -cp \"lib/*:src/main/java\" com.example.JoltUsageExamples"
        ;;
esac

echo
echo "4. 示例说明"
echo "==========="
echo "生成的示例包括:"
echo "- JoltTransformationExample.java: 基础使用示例"
echo "- JoltTransformationService.java: 服务类（推荐）"
echo "- JoltUsageExamples.java: 完整使用场景"
echo "- SpringBootJoltService.java: Spring Boot集成"
echo "- JoltController.java: REST API接口"
echo "- JoltTransformationTest.java: 单元测试"
echo

echo "5. API接口测试（如果运行Spring Boot应用）"
echo "========================================"
echo "启动应用后可以测试以下接口:"
echo
echo "# 数据脱敏"
echo "curl -X POST http://localhost:8080/api/jolt/mask \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"data\": \"{\\\"cpsId\\\": \\\"12345\\\", \\\"cpsWi\\\": \\\"67890\\\"}\"}'"
echo
echo "# 使用特定规则转换"
echo "curl -X POST http://localhost:8080/api/jolt/transform/rule_cpsId \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"data\": \"{\\\"cpsId\\\": \\\"12345\\\"}\"}'"
echo
echo "# 获取缓存状态"
echo "curl http://localhost:8080/api/jolt/cache/stats"
echo
echo "# 健康检查"
echo "curl http://localhost:8080/api/jolt/health"

echo
echo "=== 脚本执行完成 ==="