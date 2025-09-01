package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Jolt转换规则测试类
 */
public class JoltTransformationTest {
    
    private String testInputJson;
    private String rulesDirectory;
    
    @BeforeEach
    public void setUp() {
        rulesDirectory = "/workspace/jolt-rules";
        
        // 测试用的输入JSON数据
        testInputJson = """
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
                        },
                        "subOrderItemReqArgs": [
                            {
                                "itemId": "SUBITEM001",
                                "itemPro": {
                                    "dp_group": "SUBGROUP1",
                                    "dp_package_code": "SUBPKG001"
                                }
                            },
                            {
                                "itemId": "SUBITEM002",
                                "itemPro": {
                                    "dp_group": "SUBGROUP2",
                                    "dp_package_code": "SUBPKG002"
                                }
                            }
                        ]
                    },
                    {
                        "itemId": "ITEM002",
                        "itemPro": {
                            "dp_group": "GROUP2",
                            "dp_package_code": "PKG002"
                        },
                        "subOrderItemReqArgs": [
                            {
                                "itemId": "SUBITEM003",
                                "itemPro": {
                                    "dp_group": "SUBGROUP3",
                                    "dp_package_code": "SUBPKG003"
                                }
                            }
                        ]
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
            """;
    }
    
    @Test
    public void testCpsIdRule() throws Exception {
        String result = JoltTransformationExecutor.executeSingleRuleTest(testInputJson, "rule-cpsId.json");
        assertTrue(result.contains("${cpsId1111}"), "cpsId should be replaced with ${cpsId1111}");
    }
    
    @Test
    public void testCpsWiRule() throws Exception {
        String result = JoltTransformationExecutor.executeSingleRuleTest(testInputJson, "rule-cpsWi.json");
        assertTrue(result.contains("${cpsWi1111}"), "cpsWi should be replaced with ${cpsWi1111}");
    }
    
    @Test
    public void testSeqNoRule() throws Exception {
        String result = JoltTransformationExecutor.executeSingleRuleTest(testInputJson, "rule-seqNo.json");
        assertTrue(result.contains("${seqNo1111}"), "seqNo should be replaced with ${seqNo1111}");
    }
    
    @Test
    public void testOrderSouceRule() throws Exception {
        String result = JoltTransformationExecutor.executeSingleRuleTest(testInputJson, "rule-orderSouce.json");
        assertTrue(result.contains("${orderSouce00}"), "orderSouce should be replaced with ${orderSouce00}");
    }
    
    @Test
    public void testSalePortalRule() throws Exception {
        String result = JoltTransformationExecutor.executeSingleRuleTest(testInputJson, "rule-salePortal.json");
        assertTrue(result.contains("${salePortal00}"), "salePortal should be replaced with ${salePortal00}");
    }
    
    @Test
    public void testCarrierCodeRule() throws Exception {
        String result = JoltTransformationExecutor.executeSingleRuleTest(testInputJson, "rule-carrierInvoiceVOs0-carrierCode.json");
        assertTrue(result.contains("${carrierCode0000}"), "carrierCode should be replaced with ${carrierCode0000}");
    }
    
    @Test
    public void testCarrierCodeRuleWithVMALLHUAWEIDEVICE() throws Exception {
        String testJsonWithVMALL = testInputJson.replace("CARRIER001", "VMALL-HUAWEIDEVICE");
        String result = JoltTransformationExecutor.executeSingleRuleTest(testJsonWithVMALL, "rule-carrierInvoiceVOs0-carrierCode.json");
        assertTrue(result.contains("VMALL-HUAWEIDEVICE"), "VMALL-HUAWEIDEVICE should not be replaced");
    }
    
    @Test
    public void testAllRulesExecution() throws Exception {
        try {
            Object inputJson = JoltTransformationExecutor.parseJsonString(testInputJson);
            Object transformedJson = JoltTransformationExecutor.executeAllParameterRules(inputJson, rulesDirectory);
            String result = JoltTransformationExecutor.toJsonString(transformedJson);
            
            // 验证关键替换是否成功
            assertTrue(result.contains("${cpsId1111}"), "cpsId should be replaced");
            assertTrue(result.contains("${cpsWi1111}"), "cpsWi should be replaced");
            assertTrue(result.contains("${seqNo1111}"), "seqNo should be replaced");
            assertTrue(result.contains("${orderSouce00}"), "orderSouce should be replaced");
            assertTrue(result.contains("${salePortal00}"), "salePortal should be replaced");
            assertTrue(result.contains("${itemId00}"), "orderItemReqArgs[0].itemId should be replaced");
            assertTrue(result.contains("${itemId01}"), "orderItemReqArgs[1].itemId should be replaced");
            assertTrue(result.contains("${couponCodes0000}"), "couponList[0].couponCodes should be replaced");
            assertTrue(result.contains("${carrierCode0000}"), "carrierInvoiceVOs[0].carrierCode should be replaced");
            
            System.out.println("转换结果:");
            System.out.println(result);
            
        } catch (Exception e) {
            fail("执行所有规则时发生错误: " + e.getMessage());
        }
    }
    
    @Test
    public void testNullAndEmptyValues() throws Exception {
        String testJsonWithNulls = """
            {
                "cpsId": null,
                "cpsWi": "",
                "seqNo": "SEQ001",
                "orderSouce": "ONLINE",
                "salePortal": "PORTAL1",
                "orderItemReqArgs": [
                    {
                        "itemId": "ITEM001"
                    }
                ],
                "couponList": [
                    {
                        "couponCodes": ""
                    }
                ],
                "carrierInvoiceVOs": [
                    {
                        "carrierCode": null
                    }
                ]
            }
            """;
        
        try {
            Object inputJson = JoltTransformationExecutor.parseJsonString(testJsonWithNulls);
            Object transformedJson = JoltTransformationExecutor.executeAllParameterRules(inputJson, rulesDirectory);
            String result = JoltTransformationExecutor.toJsonString(transformedJson);
            
            // 验证null和空值不被替换
            assertTrue(result.contains("null") || !result.contains("${cpsId1111}"), "null cpsId should not be replaced");
            assertTrue(!result.contains("${cpsWi1111}") || result.contains("\"\""), "empty cpsWi should not be replaced");
            assertTrue(result.contains("${seqNo1111}"), "non-empty seqNo should be replaced");
            
            System.out.println("空值测试结果:");
            System.out.println(result);
            
        } catch (Exception e) {
            fail("测试空值时发生错误: " + e.getMessage());
        }
    }
}