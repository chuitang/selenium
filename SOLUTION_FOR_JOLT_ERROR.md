# Jolt错误解决方案

## 错误分析

您遇到的错误：
```
JOLT Chainr encountered an exception constructing Transform className:com.bazaarvoice.jolt.Modifier.Overwritr at index:0
```

这个错误的主要原因是：

1. **复杂条件表达式不兼容**: `modify-overwrite-beta` 操作中的复杂 `if()` 表达式在某些Jolt版本中不被支持
2. **语法版本问题**: 不同Jolt版本对条件逻辑的支持不同

## 🔧 解决方案

### 方案1: 使用Java代码实现条件逻辑（推荐）

**优点**: 最可靠、最易维护、功能最强大
**文件**: `FixedJoltTransformationService.java`

```java
FixedJoltTransformationService service = new FixedJoltTransformationService();
String result = service.applyAllDataMaskingRules(inputJson);
```

### 方案2: 使用简化的Jolt规则

**优点**: 纯Jolt实现、兼容性好
**文件**: `compatible_rules/` 目录下的规则文件

```java
// 使用兼容的简单规则
List<Object> spec = JsonUtils.jsonToList(new FileInputStream("compatible_rules/rule_cpsId.json"));
Chainr chainr = Chainr.fromSpec(spec);
```

### 方案3: 混合方案

对于简单的无条件替换使用Jolt，对于复杂条件使用Java：

```java
// 1. 先用Java处理条件逻辑
ConditionalJoltTransformer conditionalTransformer = new ConditionalJoltTransformer();
String afterConditional = conditionalTransformer.applyAllConditionalRules(inputJson);

// 2. 再用简单Jolt规则处理无条件替换
Chainr chainr = Chainr.fromSpec(simpleUnconditionalSpec);
Object transformed = chainr.transform(JsonUtils.jsonToObject(afterConditional));
```

## 🚀 立即可用的代码

### 完整的Java实现（无需Jolt规则文件）

```java
public class DataMaskingService {
    
    public String maskAllData(String inputJson) {
        FixedJoltTransformationService service = new FixedJoltTransformationService();
        return service.applyAllDataMaskingRules(inputJson);
    }
}
```

### 测试代码

```java
public static void main(String[] args) {
    String testJson = "{\"cpsId\": \"12345\", \"cpsWi\": \"67890\"}";
    
    FixedJoltTransformationService service = new FixedJoltTransformationService();
    String result = service.applyAllDataMaskingRules(testJson);
    
    System.out.println("输入: " + testJson);
    System.out.println("输出: " + result);
}
```

## 🧪 验证修复

运行快速测试：

```bash
# 编译
mvn compile

# 运行测试
mvn exec:java -Dexec.mainClass="com.example.QuickTest"
```

## 📋 规则映射表

| 原始参数 | 条件 | 替换值 | 实现方式 |
|---------|------|--------|----------|
| cpsId | 不为空 | ${cpsId1111} | Java条件逻辑 |
| cpsWi | 不为空 | ${cpsWi1111} | Java条件逻辑 |
| seqNo | 不为空 | ${seqNo1111} | Java条件逻辑 |
| orderSouce | 总是 | ${orderSouce00} | 简单Jolt或Java |
| salePortal | 总是 | ${salePortal00} | 简单Jolt或Java |
| carrierCode | 不为空且≠VMALL-HUAWEIDEVICE | ${carrierCode0000} | Java条件逻辑 |

## ⚡ 性能对比

- **纯Java实现**: 最快，无需解析Jolt规则
- **简单Jolt规则**: 中等，适合简单场景
- **复杂Jolt规则**: 最慢，且有兼容性问题

## 🎯 推荐使用

**立即使用**: `FixedJoltTransformationService` - 完全用Java实现，无兼容性问题
**长期方案**: 升级Jolt版本或等待官方修复条件表达式支持

## 文件清单

修复后的关键文件：
- ✅ `FixedJoltTransformationService.java` - 主要解决方案
- ✅ `QuickTest.java` - 快速验证
- ✅ `compatible_rules/` - 简化的Jolt规则
- ✅ `ConditionalJoltTransformer.java` - 条件逻辑实现