package com.example;

import com.bazaarvoice.jolt.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Jolt转换规则测试类
 */
public class JoltTransformationTest {
    
    private JoltTransformationService transformationService;
    
    @Before
    public void setUp() {
        transformationService = new JoltTransformationService();
    }

    @Test
    public void testCpsIdRule() {
        // 测试cpsId规则 - 有值的情况
        String inputJson = "{\"cpsId\": \"12345\"}";
        String expectedJson = "{\"cpsId\": \"${cpsId1111}\"}";
        
        transformationService.loadAndCacheRule("rule_cpsId", "rule_cpsId.json");
        String result = transformationService.transform("rule_cpsId", inputJson);
        
        assertEquals("cpsId转换失败", expectedJson, result);
    }

    @Test
    public void testCpsIdRuleWithNull() {
        // 测试cpsId规则 - null值的情况
        String inputJson = "{\"cpsId\": null}";
        String expectedJson = "{\"cpsId\": null}";
        
        transformationService.loadAndCacheRule("rule_cpsId", "rule_cpsId.json");
        String result = transformationService.transform("rule_cpsId", inputJson);
        
        assertEquals("cpsId null值处理失败", expectedJson, result);
    }

    @Test
    public void testCpsIdRuleWithEmpty() {
        // 测试cpsId规则 - 空字符串的情况
        String inputJson = "{\"cpsId\": \"\"}";
        String expectedJson = "{\"cpsId\": \"\"}";
        
        transformationService.loadAndCacheRule("rule_cpsId", "rule_cpsId.json");
        String result = transformationService.transform("rule_cpsId", inputJson);
        
        assertEquals("cpsId空字符串处理失败", expectedJson, result);
    }

    @Test
    public void testCarrierCodeRule() {
        // 测试carrierCode规则 - 正常值
        String inputJson = "{\"carrierInvoiceVOs\": [{\"carrierCode\": \"CARRIER001\"}]}";
        String expectedJson = "{\"carrierInvoiceVOs\": [{\"carrierCode\": \"${carrierCode0000}\"}]}";
        
        transformationService.loadAndCacheRule("rule_carrierInvoiceVOs_0_carrierCode", 
                                             "rule_carrierInvoiceVOs_0_carrierCode.json");
        String result = transformationService.transform("rule_carrierInvoiceVOs_0_carrierCode", inputJson);
        
        assertEquals("carrierCode转换失败", expectedJson, result);
    }

    @Test
    public void testCarrierCodeRuleWithExcludedValue() {
        // 测试carrierCode规则 - 排除值VMALL-HUAWEIDEVICE
        String inputJson = "{\"carrierInvoiceVOs\": [{\"carrierCode\": \"VMALL-HUAWEIDEVICE\"}]}";
        String expectedJson = "{\"carrierInvoiceVOs\": [{\"carrierCode\": \"VMALL-HUAWEIDEVICE\"}]}";
        
        transformationService.loadAndCacheRule("rule_carrierInvoiceVOs_0_carrierCode", 
                                             "rule_carrierInvoiceVOs_0_carrierCode.json");
        String result = transformationService.transform("rule_carrierInvoiceVOs_0_carrierCode", inputJson);
        
        assertEquals("carrierCode排除值处理失败", expectedJson, result);
    }

    @Test
    public void testOrderItemIdRule() {
        // 测试orderItemReqArgs[0].itemId规则 - 无条件转换
        String inputJson = "{\"orderItemReqArgs\": [{\"itemId\": \"ITEM001\"}]}";
        String expectedJson = "{\"orderItemReqArgs\": [{\"itemId\": \"${itemId00}\"}]}";
        
        transformationService.loadAndCacheRule("rule_orderItemReqArgs_0_itemId", 
                                             "rule_orderItemReqArgs_0_itemId.json");
        String result = transformationService.transform("rule_orderItemReqArgs_0_itemId", inputJson);
        
        assertEquals("orderItemReqArgs[0].itemId转换失败", expectedJson, result);
    }

    @Test
    public void testNestedArrayRule() {
        // 测试嵌套数组规则
        String inputJson = "{\"orderItemReqArgs\": [{\"subOrderItemReqArgs\": [{\"itemId\": \"SUBITEM001\"}]}]}";
        String expectedJson = "{\"orderItemReqArgs\": [{\"subOrderItemReqArgs\": [{\"itemId\": \"${itemId0000}\"}]}]}";
        
        transformationService.loadAndCacheRule("rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemId", 
                                             "rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemId.json");
        String result = transformationService.transform("rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemId", inputJson);
        
        assertEquals("嵌套数组规则转换失败", expectedJson, result);
    }

    @Test
    public void testCombinedRules() {
        // 测试组合规则
        String inputJson = "{\n" +
                "  \"cpsId\": \"12345\",\n" +
                "  \"cpsWi\": \"67890\",\n" +
                "  \"seqNo\": \"SEQ001\",\n" +
                "  \"orderItemReqArgs\": [\n" +
                "    {\n" +
                "      \"itemId\": \"ITEM001\",\n" +
                "      \"itemPro\": {\n" +
                "        \"dp_group\": \"GROUP001\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"orderSouce\": \"ONLINE\",\n" +
                "  \"carrierInvoiceVOs\": [\n" +
                "    {\n" +
                "      \"carrierCode\": \"CARRIER001\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        
        transformationService.loadAndCacheRule("combined_jolt_rule", "combined_jolt_rule.json");
        String result = transformationService.transform("combined_jolt_rule", inputJson);
        
        // 验证关键字段是否正确转换
        assertTrue("结果应包含脱敏后的cpsId", result.contains("${cpsId1111}"));
        assertTrue("结果应包含脱敏后的itemId", result.contains("${itemId00}"));
        assertTrue("结果应包含脱敏后的orderSouce", result.contains("${orderSouce00}"));
        assertTrue("结果应包含脱敏后的carrierCode", result.contains("${carrierCode0000}"));
    }

    @Test
    public void testBatchTransformation() {
        // 测试批量转换
        java.util.List<String> inputList = java.util.Arrays.asList(
            "{\"cpsId\": \"12345\"}",
            "{\"cpsId\": \"67890\"}",
            "{\"cpsId\": null}"
        );
        
        List<Object> spec = JsonUtils.jsonToList(
            "[{\"operation\": \"modify-overwrite-beta\", \"spec\": {\"cpsId\": \"=if(@(1,cpsId) != null && @(1,cpsId) != '', '${cpsId1111}', @(1,cpsId))\"}}]"
        );
        
        java.util.List<String> results = transformationService.batchTransform(spec, inputList);
        
        assertEquals("批量转换结果数量不正确", 3, results.size());
        assertTrue("第一个结果应包含脱敏值", results.get(0).contains("${cpsId1111}"));
        assertTrue("第二个结果应包含脱敏值", results.get(1).contains("${cpsId1111}"));
        assertTrue("第三个结果应保持null", results.get(2).contains("null"));
    }

    @Test
    public void testCachePerformance() {
        // 测试缓存性能
        String inputJson = "{\"cpsId\": \"12345\"}";
        
        // 第一次加载（会缓存）
        long startTime = System.currentTimeMillis();
        transformationService.loadAndCacheRule("rule_cpsId", "rule_cpsId.json");
        transformationService.transform("rule_cpsId", inputJson);
        long firstCallTime = System.currentTimeMillis() - startTime;
        
        // 第二次调用（使用缓存）
        startTime = System.currentTimeMillis();
        transformationService.transform("rule_cpsId", inputJson);
        long secondCallTime = System.currentTimeMillis() - startTime;
        
        assertTrue("缓存应该提高性能", secondCallTime <= firstCallTime);
        assertTrue("规则应该已缓存", transformationService.isRuleCached("rule_cpsId"));
    }

    @Test
    public void testInvalidJson() {
        // 测试无效JSON处理
        String invalidJson = "{invalid json}";
        
        transformationService.loadAndCacheRule("rule_cpsId", "rule_cpsId.json");
        
        try {
            transformationService.transform("rule_cpsId", invalidJson);
            fail("应该抛出异常处理无效JSON");
        } catch (RuntimeException e) {
            assertTrue("异常消息应该包含转换失败信息", e.getMessage().contains("转换失败"));
        }
    }

    @Test
    public void testMissingRule() {
        // 测试缺失规则处理
        try {
            transformationService.transform("nonexistent_rule", "{\"test\": \"value\"}");
            fail("应该抛出异常处理缺失规则");
        } catch (RuntimeException e) {
            assertTrue("异常消息应该包含规则未找到信息", e.getMessage().contains("规则未找到"));
        }
    }
}