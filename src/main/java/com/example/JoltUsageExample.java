package com.example;

/**
 * Jolt转换使用示例
 */
public class JoltUsageExample {
    
    public static void main(String[] args) {
        try {
            // 示例1: 执行单个规则
            System.out.println("=== 示例1: 执行单个规则 ===");
            String inputJson = """
                {
                    "cpsId": "12345",
                    "cpsWi": "67890",
                    "orderSouce": "ONLINE"
                }
                """;
            
            String result1 = JoltTransformationExecutor.executeSingleRuleTest(inputJson, "rule-cpsId.json");
            System.out.println("执行cpsId规则后:");
            System.out.println(result1);
            
            // 示例2: 执行所有规则
            System.out.println("\n=== 示例2: 执行所有规则 ===");
            String complexInputJson = """
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
                                }
                            ]
                        },
                        {
                            "itemId": "ITEM002",
                            "itemPro": {
                                "dp_group": "GROUP2"
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
                """;
            
            Object inputJsonObj = JoltTransformationExecutor.parseJsonString(complexInputJson);
            Object transformedJson = JoltTransformationExecutor.executeAllParameterRules(inputJsonObj, "/workspace/jolt-rules");
            String result2 = JoltTransformationExecutor.toJsonString(transformedJson);
            
            System.out.println("执行所有规则后:");
            System.out.println(result2);
            
            // 示例3: 测试特殊情况 - VMALL-HUAWEIDEVICE不被替换
            System.out.println("\n=== 示例3: 测试VMALL-HUAWEIDEVICE特殊情况 ===");
            String specialCaseJson = """
                {
                    "carrierInvoiceVOs": [
                        {
                            "carrierCode": "VMALL-HUAWEIDEVICE"
                        }
                    ]
                }
                """;
            
            String result3 = JoltTransformationExecutor.executeSingleRuleTest(specialCaseJson, "rule-carrierInvoiceVOs0-carrierCode.json");
            System.out.println("VMALL-HUAWEIDEVICE测试结果:");
            System.out.println(result3);
            
            // 示例4: 测试空值和null值
            System.out.println("\n=== 示例4: 测试空值和null值 ===");
            String nullTestJson = """
                {
                    "cpsId": null,
                    "cpsWi": "",
                    "seqNo": "SEQ001",
                    "orderItemReqArgs": [
                        {
                            "itemId": "ITEM001"
                        }
                    ]
                }
                """;
            
            Object nullInputJson = JoltTransformationExecutor.parseJsonString(nullTestJson);
            Object nullTransformedJson = JoltTransformationExecutor.executeAllParameterRules(nullInputJson, "/workspace/jolt-rules");
            String result4 = JoltTransformationExecutor.toJsonString(nullTransformedJson);
            
            System.out.println("空值测试结果:");
            System.out.println(result4);
            
        } catch (Exception e) {
            System.err.println("执行示例时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}