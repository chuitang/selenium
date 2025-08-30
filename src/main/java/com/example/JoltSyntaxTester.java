package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

/**
 * Jolt语法测试器
 * 用于验证不同的Jolt规则语法是否正确
 */
public class JoltSyntaxTester {
    
    public static void main(String[] args) {
        System.out.println("=== Jolt语法测试 ===");
        
        // 测试输入数据
        String[] testInputs = {
            "{\"cpsId\": \"12345\"}",           // 有值
            "{\"cpsId\": null}",                // null
            "{\"cpsId\": \"\"}",                // 空字符串
            "{\"otherField\": \"test\"}"        // 缺失字段
        };
        
        // 测试1: 简单的default操作
        System.out.println("测试1: 简单default操作");
        testRule("simple_rule_cpsId.json", testInputs);
        
        // 测试2: 使用shift+default的条件逻辑
        System.out.println("测试2: shift+default条件逻辑");
        testRule("working_rules/rule_cpsId.json", testInputs);
        
        // 测试3: 使用Java条件逻辑
        System.out.println("测试3: Java条件逻辑");
        testJavaConditionalLogic(testInputs);
    }
    
    private static void testRule(String ruleFile, String[] testInputs) {
        try {
            System.out.println("规则文件: " + ruleFile);
            
            // 读取规则文件
            java.io.InputStream ruleStream = JoltSyntaxTester.class
                .getClassLoader().getResourceAsStream(ruleFile);
            
            if (ruleStream == null) {
                // 尝试从文件系统读取
                java.io.File file = new java.io.File(ruleFile);
                if (file.exists()) {
                    ruleStream = new java.io.FileInputStream(file);
                } else {
                    System.out.println("规则文件不存在: " + ruleFile);
                    return;
                }
            }
            
            List<Object> spec = JsonUtils.jsonToList(ruleStream);
            Chainr chainr = Chainr.fromSpec(spec);
            
            for (String input : testInputs) {
                try {
                    Object inputObj = JsonUtils.jsonToObject(input);
                    Object transformed = chainr.transform(inputObj);
                    System.out.println("  输入: " + input);
                    System.out.println("  输出: " + JsonUtils.toJsonString(transformed));
                } catch (Exception e) {
                    System.out.println("  输入: " + input);
                    System.out.println("  错误: " + e.getMessage());
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            System.out.println("规则测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("----------------------------------------");
    }
    
    private static void testJavaConditionalLogic(String[] testInputs) {
        ConditionalJoltTransformer transformer = new ConditionalJoltTransformer();
        
        for (String input : testInputs) {
            try {
                String result = transformer.applyCpsIdRule(input);
                System.out.println("  输入: " + input);
                System.out.println("  输出: " + result);
            } catch (Exception e) {
                System.out.println("  输入: " + input);
                System.out.println("  错误: " + e.getMessage());
            }
            System.out.println();
        }
        
        System.out.println("----------------------------------------");
    }
}