package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Jolt转换规则执行器
 * 用于执行参数替换的Jolt转换规则
 */
public class JoltTransformationExecutor {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 执行单个Jolt转换规则
     * 
     * @param inputJson 输入的JSON数据
     * @param ruleFilePath Jolt规则文件路径
     * @return 转换后的JSON数据
     * @throws IOException 文件读取异常
     */
    public static Object executeSingleRule(Object inputJson, String ruleFilePath) throws IOException {
        // 读取Jolt规则文件
        String ruleContent = new String(Files.readAllBytes(Paths.get(ruleFilePath)));
        List<Object> chainrSpecJSON = JsonUtils.jsonToList(ruleContent);
        
        // 创建Jolt转换链
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
        
        // 执行转换
        return chainr.transform(inputJson);
    }
    
    /**
     * 执行多个Jolt转换规则（按顺序执行）
     * 
     * @param inputJson 输入的JSON数据
     * @param ruleFilePaths Jolt规则文件路径列表
     * @return 转换后的JSON数据
     * @throws IOException 文件读取异常
     */
    public static Object executeMultipleRules(Object inputJson, List<String> ruleFilePaths) throws IOException {
        Object currentJson = inputJson;
        
        // 按顺序执行每个规则
        for (String ruleFilePath : ruleFilePaths) {
            currentJson = executeSingleRule(currentJson, ruleFilePath);
        }
        
        return currentJson;
    }
    
    /**
     * 执行所有参数替换规则
     * 
     * @param inputJson 输入的JSON数据
     * @param rulesDirectory Jolt规则文件目录
     * @return 转换后的JSON数据
     * @throws IOException 文件读取异常
     */
    public static Object executeAllParameterRules(Object inputJson, String rulesDirectory) throws IOException {
        List<String> ruleFiles = new ArrayList<>();
        
        // 定义规则文件执行顺序（按照参数层级从浅到深）
        ruleFiles.add(rulesDirectory + "/rule-cpsId.json");
        ruleFiles.add(rulesDirectory + "/rule-cpsWi.json");
        ruleFiles.add(rulesDirectory + "/rule-seqNo.json");
        ruleFiles.add(rulesDirectory + "/rule-orderSouce.json");
        ruleFiles.add(rulesDirectory + "/rule-salePortal.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-itemId.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-itemPro-dp_group.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-itemPro-dp_package_code.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-subOrderItemReqArgs0-itemId.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-subOrderItemReqArgs0-itemPro-dp_group.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-subOrderItemReqArgs0-itemPro-dp_package_code.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-subOrderItemReqArgs1-itemId.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-subOrderItemReqArgs1-itemPro-dp_group.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs0-subOrderItemReqArgs1-itemPro-dp_package_code.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs1-itemId.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs1-itemPro-dp_group.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs1-itemPro-dp_package_code.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs1-subOrderItemReqArgs0-itemId.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs1-subOrderItemReqArgs0-itemPro-dp_group.json");
        ruleFiles.add(rulesDirectory + "/rule-orderItemReqArgs1-subOrderItemReqArgs0-itemPro-dp_package_code.json");
        ruleFiles.add(rulesDirectory + "/rule-couponList0-couponCodes.json");
        ruleFiles.add(rulesDirectory + "/rule-carrierInvoiceVOs0-carrierCode.json");
        
        return executeMultipleRules(inputJson, ruleFiles);
    }
    
    /**
     * 将JSON字符串转换为Object
     * 
     * @param jsonString JSON字符串
     * @return JSON对象
     * @throws JsonProcessingException JSON解析异常
     */
    public static Object parseJsonString(String jsonString) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, Object.class);
    }
    
    /**
     * 将Object转换为JSON字符串
     * 
     * @param jsonObject JSON对象
     * @return JSON字符串
     * @throws JsonProcessingException JSON序列化异常
     */
    public static String toJsonString(Object jsonObject) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    }
    
    /**
     * 主方法 - 演示如何使用Jolt转换规则
     */
    public static void main(String[] args) {
        try {
            // 示例输入JSON数据
            String inputJsonString = """
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
            
            // 解析输入JSON
            Object inputJson = parseJsonString(inputJsonString);
            
            // 执行所有转换规则
            String rulesDirectory = "/workspace/jolt-rules";
            Object transformedJson = executeAllParameterRules(inputJson, rulesDirectory);
            
            // 输出结果
            System.out.println("原始JSON:");
            System.out.println(toJsonString(inputJson));
            System.out.println("\n转换后的JSON:");
            System.out.println(toJsonString(transformedJson));
            
        } catch (Exception e) {
            System.err.println("执行Jolt转换时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 执行特定的单个规则（用于测试）
     * 
     * @param inputJsonString 输入JSON字符串
     * @param ruleFileName 规则文件名
     * @return 转换后的JSON字符串
     */
    public static String executeSingleRuleTest(String inputJsonString, String ruleFileName) {
        try {
            Object inputJson = parseJsonString(inputJsonString);
            String ruleFilePath = "/workspace/jolt-rules/" + ruleFileName;
            Object transformedJson = executeSingleRule(inputJson, ruleFilePath);
            return toJsonString(transformedJson);
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
}