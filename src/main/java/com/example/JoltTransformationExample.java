package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Jolt转换示例类
 * 
 * 支持的Java版本: Java 8+
 * 推荐版本: Java 8, 11, 17, 21
 */
public class JoltTransformationExample {
    
    private static final Logger logger = LoggerFactory.getLogger(JoltTransformationExample.class);
    private final ObjectMapper objectMapper;
    
    public JoltTransformationExample() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 从文件加载Jolt规则并执行转换
     * 
     * @param specFilePath Jolt规则文件路径
     * @param inputJson 输入JSON字符串
     * @return 转换后的JSON字符串
     */
    public String transformFromFile(String specFilePath, String inputJson) {
        try {
            // 从classpath加载Jolt规则
            InputStream specStream = getClass().getClassLoader().getResourceAsStream(specFilePath);
            if (specStream == null) {
                throw new RuntimeException("无法找到规则文件: " + specFilePath);
            }
            
            // 解析Jolt规则
            List<Object> spec = JsonUtils.jsonToList(specStream);
            
            // 创建Jolt转换链
            Chainr chainr = Chainr.fromSpec(spec);
            
            // 解析输入JSON
            Object input = JsonUtils.jsonToObject(inputJson);
            
            // 执行转换
            Object transformed = chainr.transform(input);
            
            // 转换为JSON字符串
            return JsonUtils.toJsonString(transformed);
            
        } catch (Exception e) {
            logger.error("Jolt转换失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 直接使用Jolt规则对象执行转换
     * 
     * @param spec Jolt规则列表
     * @param inputJson 输入JSON字符串
     * @return 转换后的JSON字符串
     */
    public String transformFromSpec(List<Object> spec, String inputJson) {
        try {
            // 创建Jolt转换链
            Chainr chainr = Chainr.fromSpec(spec);
            
            // 解析输入JSON
            Object input = JsonUtils.jsonToObject(inputJson);
            
            // 执行转换
            Object transformed = chainr.transform(input);
            
            // 转换为JSON字符串
            return JsonUtils.toJsonString(transformed);
            
        } catch (Exception e) {
            logger.error("Jolt转换失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用Jackson处理复杂对象转换
     * 
     * @param spec Jolt规则列表
     * @param inputObject 输入对象
     * @param outputClass 输出对象类型
     * @return 转换后的对象
     */
    public <T> T transformObject(List<Object> spec, Object inputObject, Class<T> outputClass) {
        try {
            // 将输入对象转换为JSON
            String inputJson = objectMapper.writeValueAsString(inputObject);
            
            // 执行Jolt转换
            String transformedJson = transformFromSpec(spec, inputJson);
            
            // 将结果转换为目标对象类型
            return objectMapper.readValue(transformedJson, outputClass);
            
        } catch (IOException e) {
            logger.error("对象转换失败", e);
            throw new RuntimeException("对象转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量转换多个JSON对象
     * 
     * @param spec Jolt规则列表
     * @param inputJsonList 输入JSON字符串列表
     * @return 转换后的JSON字符串列表
     */
    public java.util.List<String> batchTransform(List<Object> spec, java.util.List<String> inputJsonList) {
        // 创建Jolt转换链（复用以提高性能）
        Chainr chainr = Chainr.fromSpec(spec);
        
        return inputJsonList.stream()
                .map(inputJson -> {
                    try {
                        Object input = JsonUtils.jsonToObject(inputJson);
                        Object transformed = chainr.transform(input);
                        return JsonUtils.toJsonString(transformed);
                    } catch (Exception e) {
                        logger.error("批量转换失败，输入: " + inputJson, e);
                        throw new RuntimeException("批量转换失败: " + e.getMessage(), e);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 验证JSON是否符合预期格式
     * 
     * @param json JSON字符串
     * @return 是否有效
     */
    public boolean isValidJson(String json) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 主方法 - 演示用法
     */
    public static void main(String[] args) {
        JoltTransformationExample example = new JoltTransformationExample();
        
        // 示例1: 使用组合规则转换
        System.out.println("=== 示例1: 组合规则转换 ===");
        try {
            String inputJson = JsonUtils.classpathToString("/sample_input.json");
            String result = example.transformFromFile("combined_jolt_rule.json", inputJson);
            System.out.println("转换结果:");
            System.out.println(JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(result)));
        } catch (Exception e) {
            System.err.println("示例1执行失败: " + e.getMessage());
        }

        System.out.println("\n=== 示例2: 单个规则转换 ===");
        try {
            String inputJson = "{\"cpsId\": \"12345\", \"cpsWi\": \"67890\"}";
            String result = example.transformFromFile("rule_cpsId.json", inputJson);
            System.out.println("cpsId规则转换结果:");
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("示例2执行失败: " + e.getMessage());
        }
    }
}