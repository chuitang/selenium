# Jolt规则语法修复说明

## 问题分析

错误 `JOLT Chainr encountered an exception constructing Transform className:com.bazaarvoice.jolt.Modifier.Overwritr at index:0` 表明：

1. **操作名称问题**: `modify-overwrite-beta` 可能在您的Jolt版本中不被支持
2. **条件表达式问题**: 复杂的 `if()` 条件表达式可能不兼容
3. **语法版本问题**: 不同Jolt版本的语法支持不同

## 解决方案

### 方案1: 使用简化的Jolt规则（推荐）

对于简单的替换需求，使用基础的 `shift` + `default` 操作：

```json
[
  {
    "operation": "shift",
    "spec": {
      "cpsId": {
        "null": "=null",
        "": "=null",
        "*": "cpsId"
      },
      "*": "&"
    }
  },
  {
    "operation": "default",
    "spec": {
      "cpsId": "${cpsId1111}"
    }
  }
]
```

### 方案2: 使用Java代码实现条件逻辑（最可靠）

```java
public String applyCpsIdRule(String inputJson) {
    JsonNode jsonNode = objectMapper.readTree(inputJson);
    
    if (jsonNode.has("cpsId")) {
        JsonNode cpsIdNode = jsonNode.get("cpsId");
        if (!cpsIdNode.isNull() && !cpsIdNode.asText().isEmpty()) {
            ((ObjectNode) jsonNode).put("cpsId", "${cpsId1111}");
        }
    }
    
    return objectMapper.writeValueAsString(jsonNode);
}
```

### 方案3: 使用兼容的Jolt版本

确保使用支持 `modify-overwrite-beta` 的Jolt版本：

```xml
<dependency>
    <groupId>com.bazaarvoice.jolt</groupId>
    <artifactId>jolt-core</artifactId>
    <version>0.1.7</version>
</dependency>
```

## 修复后的文件

我已经创建了以下修复版本：

1. **simple_rule_cpsId.json** - 最简单的无条件替换
2. **working_rules/rule_cpsId.json** - 使用shift+default的条件逻辑
3. **ConditionalJoltTransformer.java** - Java实现的条件逻辑
4. **JoltSyntaxTester.java** - 语法测试工具

## 推荐使用方式

### 立即可用的解决方案
使用 `ConditionalJoltTransformer` 类，它用Java代码实现了所有条件逻辑：

```java
ConditionalJoltTransformer transformer = new ConditionalJoltTransformer();
String result = transformer.applyAllConditionalRules(inputJson);
```

### 长期解决方案
1. 升级到最新版本的Jolt库
2. 或者使用Java代码处理复杂条件
3. 对于简单替换使用基础Jolt操作

## 测试验证

运行测试器验证哪种语法在您的环境中有效：

```bash
mvn exec:java -Dexec.mainClass="com.example.JoltSyntaxTester"
```