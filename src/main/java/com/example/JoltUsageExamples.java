package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Jolt使用示例集合
 * 展示各种使用场景和最佳实践
 */
public class JoltUsageExamples {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 示例1: 基础用法 - 单个字段转换
     */
    public static void example1_BasicUsage() {
        System.out.println("=== 示例1: 基础用法 ===");
        
        // 定义Jolt规则
        List<Object> spec = JsonUtils.jsonToList(
            "[{\"operation\": \"modify-overwrite-beta\", \"spec\": {\"cpsId\": \"${cpsId1111}\"}}]"
        );
        
        // 创建转换器
        Chainr chainr = Chainr.fromSpec(spec);
        
        // 输入数据
        String inputJson = "{\"cpsId\": \"12345\", \"otherField\": \"unchanged\"}";
        Object input = JsonUtils.jsonToObject(inputJson);
        
        // 执行转换
        Object transformed = chainr.transform(input);
        
        // 输出结果
        System.out.println("输入: " + inputJson);
        System.out.println("输出: " + JsonUtils.toJsonString(transformed));
        System.out.println();
    }

    /**
     * 示例2: 条件转换 - 只有非空值才转换
     */
    public static void example2_ConditionalTransformation() {
        System.out.println("=== 示例2: 条件转换 ===");
        
        // 定义条件转换规则
        List<Object> spec = JsonUtils.jsonToList(
            "[{\"operation\": \"modify-overwrite-beta\", \"spec\": {" +
            "\"cpsId\": \"=if(@(1,cpsId) != null && @(1,cpsId) != '', '${cpsId1111}', @(1,cpsId))\"" +
            "}}]"
        );
        
        Chainr chainr = Chainr.fromSpec(spec);
        
        // 测试不同输入
        String[] testInputs = {
            "{\"cpsId\": \"12345\"}",        // 有值 -> 转换
            "{\"cpsId\": null}",             // null -> 保持
            "{\"cpsId\": \"\"}",             // 空字符串 -> 保持
            "{\"otherField\": \"test\"}"     // 缺失字段 -> 不影响
        };
        
        for (String inputJson : testInputs) {
            Object input = JsonUtils.jsonToObject(inputJson);
            Object transformed = chainr.transform(input);
            System.out.println("输入: " + inputJson);
            System.out.println("输出: " + JsonUtils.toJsonString(transformed));
            System.out.println();
        }
    }

    /**
     * 示例3: 嵌套对象和数组转换
     */
    public static void example3_NestedTransformation() {
        System.out.println("=== 示例3: 嵌套对象和数组转换 ===");
        
        // 定义嵌套转换规则
        List<Object> spec = JsonUtils.jsonToList(
            "[{\"operation\": \"modify-overwrite-beta\", \"spec\": {" +
            "\"orderItemReqArgs[0].itemId\": \"${itemId00}\"," +
            "\"orderItemReqArgs[0].itemPro.dp_group\": \"=if(@(1,orderItemReqArgs[0].itemPro.dp_group) != null && @(1,orderItemReqArgs[0].itemPro.dp_group) != '', '${dp_group00}', @(1,orderItemReqArgs[0].itemPro.dp_group))\"" +
            "}}]"
        );
        
        Chainr chainr = Chainr.fromSpec(spec);
        
        String inputJson = "{\n" +
                "  \"orderItemReqArgs\": [\n" +
                "    {\n" +
                "      \"itemId\": \"ITEM001\",\n" +
                "      \"itemPro\": {\n" +
                "        \"dp_group\": \"GROUP001\",\n" +
                "        \"dp_package_code\": \"PKG001\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        
        Object input = JsonUtils.jsonToObject(inputJson);
        Object transformed = chainr.transform(input);
        
        System.out.println("输入:");
        System.out.println(JsonUtils.toPrettyJsonString(input));
        System.out.println("输出:");
        System.out.println(JsonUtils.toPrettyJsonString(transformed));
        System.out.println();
    }

    /**
     * 示例4: 特殊条件转换 - carrierCode排除特定值
     */
    public static void example4_SpecialConditionTransformation() {
        System.out.println("=== 示例4: 特殊条件转换 ===");
        
        // carrierCode特殊规则：排除VMALL-HUAWEIDEVICE
        List<Object> spec = JsonUtils.jsonToList(
            "[{\"operation\": \"modify-overwrite-beta\", \"spec\": {" +
            "\"carrierInvoiceVOs[0].carrierCode\": \"=if(@(1,carrierInvoiceVOs[0].carrierCode) != null && @(1,carrierInvoiceVOs[0].carrierCode) != '' && @(1,carrierInvoiceVOs[0].carrierCode) != 'VMALL-HUAWEIDEVICE', '${carrierCode0000}', @(1,carrierInvoiceVOs[0].carrierCode))\"" +
            "}}]"
        );
        
        Chainr chainr = Chainr.fromSpec(spec);
        
        // 测试不同carrierCode值
        String[] testCases = {
            "{\"carrierInvoiceVOs\": [{\"carrierCode\": \"CARRIER001\"}]}",           // 普通值 -> 转换
            "{\"carrierInvoiceVOs\": [{\"carrierCode\": \"VMALL-HUAWEIDEVICE\"}]}", // 排除值 -> 保持
            "{\"carrierInvoiceVOs\": [{\"carrierCode\": \"\"}]}",                   // 空值 -> 保持
            "{\"carrierInvoiceVOs\": [{\"carrierCode\": null}]}"                   // null -> 保持
        };
        
        for (String testCase : testCases) {
            Object input = JsonUtils.jsonToObject(testCase);
            Object transformed = chainr.transform(input);
            System.out.println("输入: " + testCase);
            System.out.println("输出: " + JsonUtils.toJsonString(transformed));
            System.out.println();
        }
    }

    /**
     * 示例5: 使用服务类进行转换
     */
    public static void example5_ServiceUsage() {
        System.out.println("=== 示例5: 服务类使用 ===");
        
        JoltTransformationService service = new JoltTransformationService();
        
        // 预加载规则
        service.preloadAllRules();
        System.out.println("已预加载规则数量: " + service.getCacheSize());
        
        // 使用数据脱敏功能
        String inputJson = "{\n" +
                "  \"cpsId\": \"12345\",\n" +
                "  \"cpsWi\": \"67890\",\n" +
                "  \"orderSouce\": \"ONLINE\",\n" +
                "  \"carrierInvoiceVOs\": [{\"carrierCode\": \"CARRIER001\"}]\n" +
                "}";
        
        try {
            String maskedData = service.applyDataMaskingRules(inputJson);
            System.out.println("原始数据:");
            System.out.println(JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(inputJson)));
            System.out.println("脱敏后数据:");
            System.out.println(JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(maskedData)));
        } catch (Exception e) {
            System.err.println("转换失败: " + e.getMessage());
        }
        
        System.out.println();
    }

    /**
     * 示例6: 错误处理和日志记录
     */
    public static void example6_ErrorHandling() {
        System.out.println("=== 示例6: 错误处理 ===");
        
        JoltTransformationService service = new JoltTransformationService();
        
        // 测试无效JSON
        try {
            service.loadAndCacheRule("test_rule", "rule_cpsId.json");
            service.transform("test_rule", "{invalid json}");
        } catch (RuntimeException e) {
            System.out.println("捕获到预期异常: " + e.getMessage());
        }
        
        // 测试缺失规则
        try {
            service.transform("nonexistent_rule", "{\"test\": \"value\"}");
        } catch (RuntimeException e) {
            System.out.println("捕获到预期异常: " + e.getMessage());
        }
        
        System.out.println();
    }

    /**
     * 示例7: 性能优化 - 规则复用
     */
    public static void example7_PerformanceOptimization() {
        System.out.println("=== 示例7: 性能优化 ===");
        
        JoltTransformationService service = new JoltTransformationService();
        
        // 预加载常用规则
        service.loadAndCacheRule("cpsId_rule", "rule_cpsId.json");
        
        // 模拟大量转换请求
        String[] inputs = {
            "{\"cpsId\": \"12345\"}",
            "{\"cpsId\": \"67890\"}",
            "{\"cpsId\": \"11111\"}"
        };
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 1000; i++) {
            for (String input : inputs) {
                service.transform("cpsId_rule", input);
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("处理3000次转换耗时: " + (endTime - startTime) + "ms");
        System.out.println("平均每次转换耗时: " + (endTime - startTime) / 3000.0 + "ms");
        System.out.println();
    }

    /**
     * 主方法 - 运行所有示例
     */
    public static void main(String[] args) {
        System.out.println("Jolt转换规则使用示例");
        System.out.println("====================");
        System.out.println();
        
        try {
            example1_BasicUsage();
            example2_ConditionalTransformation();
            example3_NestedTransformation();
            example4_SpecialConditionTransformation();
            example5_ServiceUsage();
            example6_ErrorHandling();
            example7_PerformanceOptimization();
            
            System.out.println("所有示例执行完成！");
            
        } catch (Exception e) {
            System.err.println("示例执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}