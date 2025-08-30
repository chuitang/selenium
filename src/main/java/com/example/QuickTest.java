package com.example;

/**
 * 快速测试修复后的Jolt转换
 */
public class QuickTest {
    
    public static void main(String[] args) {
        System.out.println("=== 快速测试修复后的转换 ===");
        
        // 测试数据
        String testJson = "{\n" +
                "  \"cpsId\": \"12345\",\n" +
                "  \"cpsWi\": \"67890\",\n" +
                "  \"seqNo\": \"SEQ001\",\n" +
                "  \"orderItemReqArgs\": [\n" +
                "    {\n" +
                "      \"itemId\": \"ITEM001\",\n" +
                "      \"itemPro\": {\n" +
                "        \"dp_group\": \"GROUP001\",\n" +
                "        \"dp_package_code\": \"PKG001\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"itemId\": \"ITEM002\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"orderSouce\": \"ONLINE\",\n" +
                "  \"salePortal\": \"PORTAL001\",\n" +
                "  \"carrierInvoiceVOs\": [\n" +
                "    {\n" +
                "      \"carrierCode\": \"CARRIER001\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        
        // 测试空值数据
        String testJsonWithNulls = "{\n" +
                "  \"cpsId\": null,\n" +
                "  \"cpsWi\": \"\",\n" +
                "  \"seqNo\": \"SEQ001\",\n" +
                "  \"orderSouce\": \"MOBILE\",\n" +
                "  \"carrierInvoiceVOs\": [\n" +
                "    {\n" +
                "      \"carrierCode\": \"VMALL-HUAWEIDEVICE\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        
        try {
            FixedJoltTransformationService service = new FixedJoltTransformationService();
            
            System.out.println("测试1: 正常数据转换");
            System.out.println("输入:");
            System.out.println(testJson);
            System.out.println("\\n输出:");
            String result1 = service.applyAllDataMaskingRules(testJson);
            System.out.println(result1);
            
            System.out.println("\\n" + "=".repeat(50));
            
            System.out.println("测试2: 空值数据转换");
            System.out.println("输入:");
            System.out.println(testJsonWithNulls);
            System.out.println("\\n输出:");
            String result2 = service.applyAllDataMaskingRules(testJsonWithNulls);
            System.out.println(result2);
            
            System.out.println("\\n✓ 所有测试通过！");
            
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}