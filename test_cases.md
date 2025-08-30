# Jolt规则执行用例

## 概述
本文档提供了所有生成的Jolt转换规则的执行用例，包括不同场景下的输入数据和期望输出。

## 测试场景

### 场景1：正常数据（所有字段都有值）

**输入文件**: `sample_input.json`
**Jolt规则**: `combined_jolt_rule.json`
**期望输出**: `expected_output_normal.json`

#### 执行命令
```bash
# 使用Jolt CLI工具执行转换
jolt transform combined_jolt_rule.json sample_input.json
```

#### 关键验证点
- `cpsId`: "12345" → "${cpsId1111}"
- `cpsWi`: "67890" → "${cpsWi1111}"
- `seqNo`: "SEQ001" → "${seqNo1111}"
- `orderItemReqArgs[0].itemId`: "ITEM001" → "${itemId00}"
- `orderItemReqArgs[1].itemId`: "ITEM002" → "${itemId01}"
- `carrierInvoiceVOs[0].carrierCode`: "CARRIER001" → "${carrierCode0000}"

### 场景2：包含空值和null的数据

**输入文件**: `sample_input_with_nulls.json`
**Jolt规则**: `combined_jolt_rule.json`
**期望输出**: `expected_output_with_nulls.json`

#### 执行命令
```bash
jolt transform combined_jolt_rule.json sample_input_with_nulls.json
```

#### 关键验证点
- `cpsId`: null → null (保持不变)
- `cpsWi`: "" → "" (保持不变)
- `seqNo`: "SEQ001" → "${seqNo1111}" (正常转换)
- `orderItemReqArgs[0].itemPro.dp_group`: null → null (保持不变)
- `orderItemReqArgs[0].itemPro.dp_package_code`: "" → "" (保持不变)
- `carrierInvoiceVOs[0].carrierCode`: "VMALL-HUAWEIDEVICE" → "VMALL-HUAWEIDEVICE" (保持不变，因为是排除值)

## 单个规则测试用例

### 1. cpsId规则测试
```bash
# 测试cpsId转换规则
jolt transform rule_cpsId.json sample_input.json
```

**输入**: `{"cpsId": "12345"}`
**输出**: `{"cpsId": "${cpsId1111}"}`

**输入**: `{"cpsId": null}`
**输出**: `{"cpsId": null}`

### 2. carrierCode特殊规则测试
```bash
# 测试carrierCode特殊条件规则
jolt transform rule_carrierInvoiceVOs_0_carrierCode.json sample_input.json
```

**测试用例1**:
- 输入: `{"carrierInvoiceVOs": [{"carrierCode": "CARRIER001"}]}`
- 输出: `{"carrierInvoiceVOs": [{"carrierCode": "${carrierCode0000}"}]}`

**测试用例2**:
- 输入: `{"carrierInvoiceVOs": [{"carrierCode": "VMALL-HUAWEIDEVICE"}]}`
- 输出: `{"carrierInvoiceVOs": [{"carrierCode": "VMALL-HUAWEIDEVICE"}]}`

**测试用例3**:
- 输入: `{"carrierInvoiceVOs": [{"carrierCode": ""}]}`
- 输出: `{"carrierInvoiceVOs": [{"carrierCode": ""}]}`

### 3. 嵌套数组规则测试
```bash
# 测试嵌套数组转换规则
jolt transform rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemId.json sample_input.json
```

**输入**: 
```json
{
  "orderItemReqArgs": [
    {
      "subOrderItemReqArgs": [
        {"itemId": "SUBITEM001"}
      ]
    }
  ]
}
```

**输出**:
```json
{
  "orderItemReqArgs": [
    {
      "subOrderItemReqArgs": [
        {"itemId": "${itemId0000}"}
      ]
    }
  ]
}
```

## 批量测试脚本

### 测试所有单个规则
```bash
#!/bin/bash
echo "Testing individual Jolt rules..."

for rule_file in rule_*.json; do
    echo "Testing $rule_file"
    jolt transform "$rule_file" sample_input.json > "output_${rule_file%.json}.json"
    echo "Output saved to output_${rule_file%.json}.json"
done
```

### 验证组合规则
```bash
#!/bin/bash
echo "Testing combined Jolt rule..."

# 测试正常数据
jolt transform combined_jolt_rule.json sample_input.json > actual_output_normal.json
echo "Normal case output saved to actual_output_normal.json"

# 测试包含null/空值的数据
jolt transform combined_jolt_rule.json sample_input_with_nulls.json > actual_output_with_nulls.json
echo "Null case output saved to actual_output_with_nulls.json"

# 比较期望输出和实际输出
echo "Comparing results..."
diff expected_output_normal.json actual_output_normal.json
diff expected_output_with_nulls.json actual_output_with_nulls.json
```

## 性能测试

### 大数据量测试
```bash
# 生成包含大量数据的测试文件
# 测试规则在处理大量orderItemReqArgs时的性能
```

## 边界条件测试

### 测试用例: 缺失字段
**输入**: 
```json
{
  "cpsId": "12345",
  "orderItemReqArgs": [
    {
      "itemId": "ITEM001"
      // 缺少itemPro字段
    }
  ]
}
```

### 测试用例: 空数组
**输入**:
```json
{
  "orderItemReqArgs": [],
  "couponList": [],
  "carrierInvoiceVOs": []
}
```

## 错误处理测试

### 测试用例: 数组索引越界
当输入数据中没有足够的数组元素时，规则应该优雅地处理这种情况。

## 使用说明

1. **单个规则使用**: 当只需要转换特定字段时，使用对应的单个规则文件
2. **组合规则使用**: 当需要一次性转换所有字段时，使用`combined_jolt_rule.json`
3. **测试验证**: 使用提供的测试用例验证规则的正确性
4. **性能考虑**: 对于大数据量，建议分批处理或使用单个规则以提高性能

## 注意事项

1. **条件逻辑**: 带有条件的规则只有在满足条件时才会替换值
2. **特殊排除**: `carrierCode`规则排除"VMALL-HUAWEIDEVICE"值
3. **数组索引**: 规则中的数组索引是硬编码的，确保输入数据结构匹配
4. **空值处理**: null和空字符串被视为不同的条件，规则会相应处理