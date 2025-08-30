#!/bin/bash

echo "=== Jolt规则测试脚本 ==="
echo

# 检查是否安装了jolt-cli
if ! command -v jolt &> /dev/null; then
    echo "错误: 未找到jolt命令行工具"
    echo "请安装jolt-cli: npm install -g jolt-cli"
    echo "或者使用Java版本: java -jar jolt-cli.jar"
    exit 1
fi

echo "1. 测试组合规则 - 正常数据"
echo "================================"
jolt transform combined_jolt_rule.json sample_input.json > actual_output_normal.json
echo "✓ 正常数据转换完成，输出保存到: actual_output_normal.json"
echo

echo "2. 测试组合规则 - 包含null/空值数据"
echo "====================================="
jolt transform combined_jolt_rule.json sample_input_with_nulls.json > actual_output_with_nulls.json
echo "✓ 空值数据转换完成，输出保存到: actual_output_with_nulls.json"
echo

echo "3. 验证输出结果"
echo "==============="
echo "比较正常数据转换结果:"
if diff -q expected_output_normal.json actual_output_normal.json > /dev/null; then
    echo "✓ 正常数据测试通过"
else
    echo "✗ 正常数据测试失败，查看差异:"
    diff expected_output_normal.json actual_output_normal.json
fi

echo
echo "比较空值数据转换结果:"
if diff -q expected_output_with_nulls.json actual_output_with_nulls.json > /dev/null; then
    echo "✓ 空值数据测试通过"
else
    echo "✗ 空值数据测试失败，查看差异:"
    diff expected_output_with_nulls.json actual_output_with_nulls.json
fi

echo
echo "4. 测试单个规则"
echo "==============="
echo "测试cpsId规则:"
jolt transform rule_cpsId.json sample_input.json | jq '.cpsId'

echo "测试carrierCode特殊规则:"
jolt transform rule_carrierInvoiceVOs_0_carrierCode.json sample_input.json | jq '.carrierInvoiceVOs[0].carrierCode'

echo "测试carrierCode特殊规则 (VMALL-HUAWEIDEVICE):"
jolt transform rule_carrierInvoiceVOs_0_carrierCode.json sample_input_with_nulls.json | jq '.carrierInvoiceVOs[0].carrierCode'

echo
echo "=== 测试完成 ==="