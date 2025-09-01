package com.example.jolt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JOLT转换示例类
 * 演示如何使用JoltTransformationService进行参数替换
 */
public class JoltTransformationExample {
    
    public static void main(String[] args) {
        try {
            // 初始化转换服务
            JoltTransformationService transformationService = 
                JoltTransformationService.fromFile("/workspace/jolt-transformation-rules.json");
            
            // 创建测试数据
            String inputJson = createTestInputJson();
            
            System.out.println("=== 原始输入JSON ===");
            System.out.println(formatJson(inputJson));
            
            // 执行转换
            String transformedJson = transformationService.transform(inputJson);
            
            System.out.println("\n=== 转换后的JSON ===");
            System.out.println(formatJson(transformedJson));
            
            // 验证特定字段的转换结果
            validateTransformation(transformedJson);
            
        } catch (Exception e) {
            System.err.println("转换过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建测试输入JSON数据
     * @return 测试用的JSON字符串
     */
    private static String createTestInputJson() {
        return """
        {
          "cpsId": "CPS123456",
          "cpsWi": "WI789012",
          "seqNo": "SEQ345678",
          "orderItemReqArgs": [
            {
              "itemId": "ITEM001",
              "itemPro": {
                "dp_group": "GROUP_A",
                "dp_package_code": "PACKAGE_001"
              },
              "subOrderItemReqArgs": [
                {
                  "itemId": "SUB_ITEM_001",
                  "itemPro": {
                    "dp_group": "SUB_GROUP_A",
                    "dp_package_code": "SUB_PACKAGE_001"
                  }
                },
                {
                  "itemId": "SUB_ITEM_002",
                  "itemPro": {
                    "dp_group": "SUB_GROUP_B",
                    "dp_package_code": "SUB_PACKAGE_002"
                  }
                }
              ]
            },
            {
              "itemId": "ITEM002",
              "itemPro": {
                "dp_group": "GROUP_B",
                "dp_package_code": "PACKAGE_002"
              },
              "subOrderItemReqArgs": [
                {
                  "itemId": "SUB_ITEM_003",
                  "itemPro": {
                    "dp_group": "SUB_GROUP_C",
                    "dp_package_code": "SUB_PACKAGE_003"
                  }
                }
              ]
            }
          ],
          "couponList": [
            {
              "couponCodes": "COUPON123"
            }
          ],
          "orderSouce": "ONLINE",
          "salePortal": "WEBSITE",
          "carrierInvoiceVOs": [
            {
              "carrierCode": "CARRIER_ABC"
            }
          ]
        }
        """;
    }
    
    /**
     * 创建测试边界情况的JSON数据
     * @return 包含空值和特殊情况的测试JSON
     */
    private static String createEdgeCaseInputJson() {
        return """
        {
          "cpsId": "",
          "cpsWi": null,
          "seqNo": "SEQ999",
          "orderItemReqArgs": [
            {
              "itemId": "ITEM999",
              "itemPro": {
                "dp_group": "",
                "dp_package_code": null
              }
            }
          ],
          "couponList": [
            {
              "couponCodes": ""
            }
          ],
          "orderSouce": "MOBILE",
          "salePortal": "APP",
          "carrierInvoiceVOs": [
            {
              "carrierCode": "VMALL-HUAWEIDEVICE"
            }
          ]
        }
        """;
    }
    
    /**
     * 验证转换结果
     * @param transformedJson 转换后的JSON字符串
     */
    private static void validateTransformation(String transformedJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(transformedJson);
            
            System.out.println("\n=== 转换验证结果 ===");
            
            // 验证基本参数
            System.out.println("cpsId: " + jsonNode.get("cpsId").asText());
            System.out.println("cpsWi: " + jsonNode.get("cpsWi").asText());
            System.out.println("seqNo: " + jsonNode.get("seqNo").asText());
            
            // 验证数组参数
            JsonNode orderItems = jsonNode.get("orderItemReqArgs");
            if (orderItems != null && orderItems.isArray() && orderItems.size() > 0) {
                System.out.println("orderItemReqArgs[0].itemId: " + 
                    orderItems.get(0).get("itemId").asText());
            }
            
            // 验证嵌套参数
            JsonNode itemPro = orderItems.get(0).get("itemPro");
            if (itemPro != null) {
                System.out.println("orderItemReqArgs[0].itemPro.dp_group: " + 
                    itemPro.get("dp_group").asText());
            }
            
        } catch (Exception e) {
            System.err.println("验证过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 格式化JSON字符串用于输出
     * @param jsonString 要格式化的JSON字符串
     * @return 格式化后的JSON字符串
     */
    private static String formatJson(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonString, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return jsonString;
        }
    }
    
    /**
     * 测试边界情况
     */
    public static void testEdgeCases() {
        try {
            JoltTransformationService transformationService = 
                JoltTransformationService.fromFile("/workspace/jolt-transformation-rules.json");
            
            String edgeCaseJson = createEdgeCaseInputJson();
            
            System.out.println("\n=== 边界情况测试 ===");
            System.out.println("输入JSON:");
            System.out.println(formatJson(edgeCaseJson));
            
            String transformedJson = transformationService.transform(edgeCaseJson);
            
            System.out.println("\n转换后JSON:");
            System.out.println(formatJson(transformedJson));
            
        } catch (Exception e) {
            System.err.println("边界情况测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}