package com.example.jolt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JOLT转换服务测试类
 */
public class JoltTransformationServiceTest {
    
    private JoltTransformationService transformationService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws Exception {
        transformationService = JoltTransformationService.fromFile("/workspace/jolt-transformation-rules.json");
        objectMapper = new ObjectMapper();
    }
    
    @Test
    @DisplayName("测试基本参数替换")
    void testBasicParameterReplacement() throws Exception {
        String inputJson = """
        {
          "cpsId": "CPS123456",
          "cpsWi": "WI789012",
          "seqNo": "SEQ345678",
          "orderSouce": "ONLINE",
          "salePortal": "WEBSITE"
        }
        """;
        
        String transformedJson = transformationService.transform(inputJson);
        JsonNode result = objectMapper.readTree(transformedJson);
        
        assertEquals("${cpsId1111}", result.get("cpsId").asText());
        assertEquals("${cpsWi1111}", result.get("cpsWi").asText());
        assertEquals("${seqNo1111}", result.get("seqNo").asText());
        assertEquals("${orderSouce00}", result.get("orderSouce").asText());
        assertEquals("${salePortal00}", result.get("salePortal").asText());
    }
    
    @Test
    @DisplayName("测试空值和null值处理")
    void testNullAndEmptyValueHandling() throws Exception {
        String inputJson = """
        {
          "cpsId": "",
          "cpsWi": null,
          "seqNo": "SEQ123",
          "orderSouce": "MOBILE",
          "salePortal": "APP"
        }
        """;
        
        String transformedJson = transformationService.transform(inputJson);
        JsonNode result = objectMapper.readTree(transformedJson);
        
        // 空字符串和null值不应该被替换
        assertEquals("", result.get("cpsId").asText());
        assertTrue(result.get("cpsWi").isNull());
        // 非空值应该被替换
        assertEquals("${seqNo1111}", result.get("seqNo").asText());
        assertEquals("${orderSouce00}", result.get("orderSouce").asText());
        assertEquals("${salePortal00}", result.get("salePortal").asText());
    }
    
    @Test
    @DisplayName("测试数组参数替换")
    void testArrayParameterReplacement() throws Exception {
        String inputJson = """
        {
          "orderItemReqArgs": [
            {
              "itemId": "ITEM001",
              "itemPro": {
                "dp_group": "GROUP_A",
                "dp_package_code": "PACKAGE_001"
              }
            },
            {
              "itemId": "ITEM002",
              "itemPro": {
                "dp_group": "GROUP_B",
                "dp_package_code": "PACKAGE_002"
              }
            }
          ]
        }
        """;
        
        String transformedJson = transformationService.transform(inputJson);
        JsonNode result = objectMapper.readTree(transformedJson);
        
        JsonNode orderItems = result.get("orderItemReqArgs");
        
        // 验证第一个元素
        assertEquals("${itemId00}", orderItems.get(0).get("itemId").asText());
        assertEquals("${dp_group00}", orderItems.get(0).get("itemPro").get("dp_group").asText());
        assertEquals("${dp_package_code00}", orderItems.get(0).get("itemPro").get("dp_package_code").asText());
        
        // 验证第二个元素
        assertEquals("${itemId01}", orderItems.get(1).get("itemId").asText());
        assertEquals("${dp_group01}", orderItems.get(1).get("itemPro").get("dp_group").asText());
        assertEquals("${dp_package_code01}", orderItems.get(1).get("itemPro").get("dp_package_code").asText());
    }
    
    @Test
    @DisplayName("测试嵌套数组参数替换")
    void testNestedArrayParameterReplacement() throws Exception {
        String inputJson = """
        {
          "orderItemReqArgs": [
            {
              "itemId": "ITEM001",
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
            }
          ]
        }
        """;
        
        String transformedJson = transformationService.transform(inputJson);
        JsonNode result = objectMapper.readTree(transformedJson);
        
        JsonNode subOrderItems = result.get("orderItemReqArgs").get(0).get("subOrderItemReqArgs");
        
        // 验证第一个子订单项
        assertEquals("${itemId0000}", subOrderItems.get(0).get("itemId").asText());
        assertEquals("${dp_group0000}", subOrderItems.get(0).get("itemPro").get("dp_group").asText());
        assertEquals("${dp_package_code0000}", subOrderItems.get(0).get("itemPro").get("dp_package_code").asText());
        
        // 验证第二个子订单项
        assertEquals("${itemId0001}", subOrderItems.get(1).get("itemId").asText());
        assertEquals("${dp_group0001}", subOrderItems.get(1).get("itemPro").get("dp_group").asText());
        assertEquals("${dp_package_code0001}", subOrderItems.get(1).get("itemPro").get("dp_package_code").asText());
    }
    
    @Test
    @DisplayName("测试特殊条件：carrierCode特殊值处理")
    void testCarrierCodeSpecialCondition() throws Exception {
        // 测试VMALL-HUAWEIDEVICE值不被替换
        String inputJson1 = """
        {
          "carrierInvoiceVOs": [
            {
              "carrierCode": "VMALL-HUAWEIDEVICE"
            }
          ]
        }
        """;
        
        String transformedJson1 = transformationService.transform(inputJson1);
        JsonNode result1 = objectMapper.readTree(transformedJson1);
        
        assertEquals("VMALL-HUAWEIDEVICE", 
            result1.get("carrierInvoiceVOs").get(0).get("carrierCode").asText());
        
        // 测试其他值被替换
        String inputJson2 = """
        {
          "carrierInvoiceVOs": [
            {
              "carrierCode": "OTHER_CARRIER"
            }
          ]
        }
        """;
        
        String transformedJson2 = transformationService.transform(inputJson2);
        JsonNode result2 = objectMapper.readTree(transformedJson2);
        
        assertEquals("${carrierCode0000}", 
            result2.get("carrierInvoiceVOs").get(0).get("carrierCode").asText());
    }
    
    @Test
    @DisplayName("测试优惠券代码替换")
    void testCouponCodeReplacement() throws Exception {
        String inputJson = """
        {
          "couponList": [
            {
              "couponCodes": "COUPON123456"
            }
          ]
        }
        """;
        
        String transformedJson = transformationService.transform(inputJson);
        JsonNode result = objectMapper.readTree(transformedJson);
        
        assertEquals("${couponCodes0000}", 
            result.get("couponList").get(0).get("couponCodes").asText());
    }
    
    @Test
    @DisplayName("测试完整数据转换")
    void testCompleteDataTransformation() throws Exception {
        String inputJson = """
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
        
        String transformedJson = transformationService.transform(inputJson);
        JsonNode result = objectMapper.readTree(transformedJson);
        
        // 验证所有转换都按预期进行
        assertNotNull(result);
        assertTrue(result.get("cpsId").asText().contains("${cpsId1111}"));
        assertTrue(result.get("orderItemReqArgs").get(0).get("itemId").asText().contains("${itemId00}"));
        assertTrue(result.get("carrierInvoiceVOs").get(0).get("carrierCode").asText().contains("${carrierCode0000}"));
    }
}