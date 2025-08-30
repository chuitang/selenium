# Jolt Java使用指南

## 支持的Java版本

### 推荐版本
- **Java 8** (最低要求)
- **Java 11** (LTS推荐)
- **Java 17** (LTS推荐)
- **Java 21** (最新LTS)

### 版本兼容性说明
- **Java 8**: Jolt库的最低要求版本，完全支持所有功能
- **Java 11+**: 更好的性能和内存管理
- **Java 17+**: 支持最新的JVM优化特性
- **Java 21**: 最佳性能，支持虚拟线程等新特性

## Maven依赖配置

### 核心依赖
```xml
<dependencies>
    <!-- Jolt核心库 -->
    <dependency>
        <groupId>com.bazaarvoice.jolt</groupId>
        <artifactId>jolt-core</artifactId>
        <version>0.1.7</version>
    </dependency>
    
    <!-- JSON工具库 -->
    <dependency>
        <groupId>com.bazaarvoice.jolt</groupId>
        <artifactId>json-utils</artifactId>
        <version>0.1.7</version>
    </dependency>
</dependencies>
```

### 完整配置
参考项目根目录的 `pom.xml` 文件

## Gradle依赖配置

```gradle
dependencies {
    implementation 'com.bazaarvoice.jolt:jolt-core:0.1.7'
    implementation 'com.bazaarvoice.jolt:json-utils:0.1.7'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
}
```

## 快速开始

### 1. 基础使用方式

```java
import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

// 加载Jolt规则
List<Object> spec = JsonUtils.jsonToList(ruleJsonString);
Chainr chainr = Chainr.fromSpec(spec);

// 执行转换
Object input = JsonUtils.jsonToObject(inputJsonString);
Object transformed = chainr.transform(input);
String result = JsonUtils.toJsonString(transformed);
```

### 2. 使用服务类（推荐）

```java
JoltTransformationService service = new JoltTransformationService();

// 预加载所有规则
service.preloadAllRules();

// 执行数据脱敏
String maskedData = service.applyDataMaskingRules(inputJson);
```

## 项目结构

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── JoltTransformationExample.java    # 基础示例
│   │   ├── JoltTransformationService.java    # 服务类（推荐使用）
│   │   └── JoltUsageExamples.java           # 各种使用场景示例
│   └── resources/
│       ├── combined_jolt_rule.json          # 组合规则
│       ├── rule_*.json                      # 单个规则文件
│       ├── sample_input.json               # 测试输入数据
│       └── expected_output_*.json          # 期望输出数据
└── test/
    └── java/com/example/
        └── JoltTransformationTest.java      # 单元测试
```

## 编译和运行

### Maven方式
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 运行示例
mvn exec:java -Dexec.mainClass="com.example.JoltUsageExamples"

# 打包
mvn package
```

### Gradle方式
```bash
# 编译项目
./gradlew build

# 运行测试
./gradlew test

# 运行示例
./gradlew run

# 打包
./gradlew jar
```

### 直接编译运行
```bash
# 编译
javac -cp "lib/*" src/main/java/com/example/*.java

# 运行
java -cp "lib/*:src/main/java" com.example.JoltUsageExamples
```

## 核心类说明

### 1. JoltTransformationExample.java
- 基础使用示例
- 演示从文件加载规则
- 展示不同的转换方法

### 2. JoltTransformationService.java
- 生产级服务类
- 支持规则缓存
- 批量处理功能
- 错误处理机制

### 3. JoltUsageExamples.java
- 7个完整的使用示例
- 覆盖各种场景
- 性能优化演示

### 4. JoltTransformationTest.java
- 完整的单元测试
- 覆盖所有规则类型
- 边界条件测试

## 性能优化建议

### 1. 规则缓存
```java
// 预加载常用规则，避免重复解析
service.preloadAllRules();

// 复用Chainr对象
Chainr chainr = Chainr.fromSpec(spec); // 缓存这个对象
```

### 2. 批量处理
```java
// 批量转换多个JSON对象
List<String> results = service.batchTransform(spec, inputJsonList);
```

### 3. 内存管理
```java
// 定期清理缓存（如果规则会变化）
service.clearCache();
```

## 常见问题和解决方案

### 1. ClassNotFoundException
确保所有依赖都已正确添加到classpath

### 2. 规则解析失败
检查JSON规则文件格式是否正确

### 3. 转换结果不符合预期
- 验证输入数据结构
- 检查规则中的条件逻辑
- 使用调试模式查看中间结果

### 4. 性能问题
- 使用规则缓存
- 避免在循环中重复创建Chainr对象
- 考虑使用批量处理

## 部署建议

### 开发环境
- 使用Maven或Gradle进行依赖管理
- 配置IDE的classpath包含所有依赖

### 生产环境
- 使用fat jar打包所有依赖
- 配置适当的JVM参数
- 监控内存使用和性能指标

### Spring Boot集成
```java
@Service
public class DataMaskingService {
    
    @Autowired
    private JoltTransformationService joltService;
    
    @PostConstruct
    public void init() {
        joltService.preloadAllRules();
    }
    
    public String maskSensitiveData(String jsonData) {
        return joltService.applyDataMaskingRules(jsonData);
    }
}
```

## 示例执行命令

```bash
# 1. 使用Maven运行示例
mvn exec:java -Dexec.mainClass="com.example.JoltUsageExamples"

# 2. 运行单元测试
mvn test

# 3. 运行特定测试
mvn test -Dtest=JoltTransformationTest#testCpsIdRule

# 4. 生成测试报告
mvn surefire-report:report
```