# Jolt 参数替换规则

本项目包含了一套完整的 Jolt 转换规则，用于替换 JSON 数据中的特定参数值。

## 规则概述

生成的规则包括以下参数的替换：

### 顶级参数
1. **cpsId** → `${cpsId1111}` (如果不为空)
2. **cpsWi** → `${cpsWi1111}` (如果不为空)
3. **seqNo** → `${seqNo1111}` (如果不为空)
4. **orderSouce** → `${orderSouce00}` (强制替换)
5. **salePortal** → `${salePortal00}` (强制替换)

### 订单项参数 (orderItemReqArgs)
6. **orderItemReqArgs[0].itemId** → `${itemId00}` (强制替换)
7. **orderItemReqArgs[0].itemPro.dp_group** → `${dp_group00}` (如果存在且不为空)
8. **orderItemReqArgs[0].itemPro.dp_package_code** → `${dp_package_code00}` (如果存在且不为空)
9. **orderItemReqArgs[1].itemId** → `${itemId01}` (强制替换)
10. **orderItemReqArgs[1].itemPro.dp_group** → `${dp_group01}` (如果存在且不为空)
11. **orderItemReqArgs[1].itemPro.dp_package_code** → `${dp_package_code01}` (如果存在且不为空)

### 子订单项参数 (subOrderItemReqArgs)
12. **orderItemReqArgs[0].subOrderItemReqArgs[0].itemId** → `${itemId0000}` (如果存在且不为空)
13. **orderItemReqArgs[0].subOrderItemReqArgs[0].itemPro.dp_group** → `${dp_group0000}` (如果存在且不为空)
14. **orderItemReqArgs[0].subOrderItemReqArgs[0].itemPro.dp_package_code** → `${dp_package_code0000}` (如果存在且不为空)
15. **orderItemReqArgs[0].subOrderItemReqArgs[1].itemId** → `${itemId0001}` (如果存在且不为空)
16. **orderItemReqArgs[0].subOrderItemReqArgs[1].itemPro.dp_group** → `${dp_group0001}` (如果存在且不为空)
17. **orderItemReqArgs[0].subOrderItemReqArgs[1].itemPro.dp_package_code** → `${dp_package_code0001}` (如果存在且不为空)
18. **orderItemReqArgs[1].subOrderItemReqArgs[0].itemId** → `${itemId0100}` (如果存在且不为空)
19. **orderItemReqArgs[1].subOrderItemReqArgs[0].itemPro.dp_group** → `${dp_group0100}` (如果存在且不为空)
20. **orderItemReqArgs[1].subOrderItemReqArgs[0].itemPro.dp_package_code** → `${dp_package_code0100}` (如果存在且不为空)

### 其他参数
21. **couponList[0].couponCodes** → `${couponCodes0000}` (如果不为空)
22. **carrierInvoiceVOs[0].carrierCode** → `${carrierCode0000}` (如果不为 VMALL-HUAWEIDEVICE 且不为空)

## 文件结构

```
jolt-rules/
├── rule-cpsId.json                                                    # cpsId规则
├── rule-cpsWi.json                                                    # cpsWi规则
├── rule-seqNo.json                                                    # seqNo规则
├── rule-orderSouce.json                                               # orderSouce规则
├── rule-salePortal.json                                               # salePortal规则
├── rule-orderItemReqArgs0-itemId.json                                 # orderItemReqArgs[0].itemId规则
├── rule-orderItemReqArgs0-itemPro-dp_group.json                       # orderItemReqArgs[0].itemPro.dp_group规则
├── rule-orderItemReqArgs0-itemPro-dp_package_code.json                # orderItemReqArgs[0].itemPro.dp_package_code规则
├── rule-orderItemReqArgs0-subOrderItemReqArgs0-itemId.json            # 子订单项规则
├── rule-orderItemReqArgs0-subOrderItemReqArgs0-itemPro-dp_group.json  # 子订单项属性规则
├── ... (其他规则文件)
└── all-parameter-rules.json                                           # 所有规则合并文件
```

## Java 代码使用方法

### 1. 执行单个规则

```java
// 执行单个规则
String inputJson = "{ \"cpsId\": \"12345\" }";
String result = JoltTransformationExecutor.executeSingleRuleTest(inputJson, "rule-cpsId.json");
System.out.println(result);
```

### 2. 执行多个规则

```java
// 执行多个规则
Object inputJson = JoltTransformationExecutor.parseJsonString(inputJsonString);
List<String> ruleFiles = Arrays.asList(
    "/workspace/jolt-rules/rule-cpsId.json",
    "/workspace/jolt-rules/rule-cpsWi.json"
);
Object result = JoltTransformationExecutor.executeMultipleRules(inputJson, ruleFiles);
```

### 3. 执行所有参数替换规则

```java
// 执行所有规则
Object inputJson = JoltTransformationExecutor.parseJsonString(inputJsonString);
Object result = JoltTransformationExecutor.executeAllParameterRules(inputJson, "/workspace/jolt-rules");
String resultJson = JoltTransformationExecutor.toJsonString(result);
```

### 4. 使用合并的规则文件

```java
// 使用合并的规则文件（更高效）
Object result = JoltTransformationExecutor.executeSingleRule(inputJson, "/workspace/jolt-rules/all-parameter-rules.json");
```

## 运行测试

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 运行示例
mvn exec:java -Dexec.mainClass="com.example.JoltUsageExample"
```

## 特殊规则说明

1. **条件替换**: 大部分规则只在参数不为空时才进行替换
2. **强制替换**: `orderSouce`, `salePortal`, 和 `orderItemReqArgs[x].itemId` 会强制替换
3. **特殊排除**: `carrierCode` 如果值为 `VMALL-HUAWEIDEVICE` 则不会被替换
4. **嵌套结构**: 支持深层嵌套的JSON结构转换

## 输入示例

```json
{
  "cpsId": "12345",
  "cpsWi": "67890",
  "seqNo": "SEQ001",
  "orderSouce": "ONLINE",
  "salePortal": "PORTAL1",
  "orderItemReqArgs": [
    {
      "itemId": "ITEM001",
      "itemPro": {
        "dp_group": "GROUP1",
        "dp_package_code": "PKG001"
      }
    }
  ],
  "couponList": [
    {
      "couponCodes": "COUPON001"
    }
  ],
  "carrierInvoiceVOs": [
    {
      "carrierCode": "CARRIER001"
    }
  ]
}
```

## 输出示例

```json
{
  "cpsId": "${cpsId1111}",
  "cpsWi": "${cpsWi1111}",
  "seqNo": "${seqNo1111}",
  "orderSouce": "${orderSouce00}",
  "salePortal": "${salePortal00}",
  "orderItemReqArgs": [
    {
      "itemId": "${itemId00}",
      "itemPro": {
        "dp_group": "${dp_group00}",
        "dp_package_code": "${dp_package_code00}"
      }
    }
  ],
  "couponList": [
    {
      "couponCodes": "${couponCodes0000}"
    }
  ],
  "carrierInvoiceVOs": [
    {
      "carrierCode": "${carrierCode0000}"
    }
  ]
}
```