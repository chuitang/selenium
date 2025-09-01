package com.example.jolt;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * JOLT转换服务类
 * 用于执行JSON数据的参数替换转换
 */
public class JoltTransformationService {
    
    private final Chainr chainr;
    private final ObjectMapper objectMapper;
    
    /**
     * 构造函数，初始化JOLT转换链
     * @param rulesResourcePath JOLT规则文件的资源路径
     * @throws IOException 当读取规则文件失败时抛出
     */
    public JoltTransformationService(String rulesResourcePath) throws IOException {
        this.objectMapper = new ObjectMapper();
        
        // 从资源文件加载JOLT规则
        InputStream rulesStream = getClass().getClassLoader().getResourceAsStream(rulesResourcePath);
        if (rulesStream == null) {
            throw new IOException("无法找到JOLT规则文件: " + rulesResourcePath);
        }
        
        List<Object> chainrSpecJSON = JsonUtils.jsonToList(rulesStream);
        this.chainr = Chainr.fromSpec(chainrSpecJSON);
    }
    
    /**
     * 使用JOLT规则文件路径构造
     * @param rulesFilePath 规则文件的绝对路径
     * @throws IOException 当读取规则文件失败时抛出
     */
    public static JoltTransformationService fromFile(String rulesFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Object> chainrSpecJSON = JsonUtils.jsonToList(
            mapper.readTree(new java.io.File(rulesFilePath))
        );
        
        JoltTransformationService service = new JoltTransformationService();
        service.chainr = Chainr.fromSpec(chainrSpecJSON);
        return service;
    }
    
    private JoltTransformationService() {
        this.objectMapper = new ObjectMapper();
        this.chainr = null;
    }
    
    /**
     * 执行JSON转换
     * @param inputJson 输入的JSON字符串
     * @return 转换后的JSON字符串
     * @throws IOException 当JSON解析或转换失败时抛出
     */
    public String transform(String inputJson) throws IOException {
        // 将输入JSON字符串转换为Object
        Object inputJsonObject = JsonUtils.jsonToObject(inputJson);
        
        // 执行JOLT转换
        Object transformedObject = chainr.transform(inputJsonObject);
        
        // 将转换结果转换回JSON字符串
        return JsonUtils.toJsonString(transformedObject);
    }
    
    /**
     * 执行JSON转换（接受JsonNode输入）
     * @param inputJsonNode 输入的JsonNode对象
     * @return 转换后的JSON字符串
     * @throws IOException 当转换失败时抛出
     */
    public String transform(JsonNode inputJsonNode) throws IOException {
        return transform(inputJsonNode.toString());
    }
    
    /**
     * 执行JSON转换并返回JsonNode
     * @param inputJson 输入的JSON字符串
     * @return 转换后的JsonNode对象
     * @throws IOException 当JSON解析或转换失败时抛出
     */
    public JsonNode transformToJsonNode(String inputJson) throws IOException {
        String transformedJson = transform(inputJson);
        return objectMapper.readTree(transformedJson);
    }
    
    /**
     * 批量转换多个JSON对象
     * @param inputJsonList 输入的JSON字符串列表
     * @return 转换后的JSON字符串列表
     * @throws IOException 当任何转换失败时抛出
     */
    public List<String> batchTransform(List<String> inputJsonList) throws IOException {
        return inputJsonList.stream()
            .map(json -> {
                try {
                    return transform(json);
                } catch (IOException e) {
                    throw new RuntimeException("转换失败: " + e.getMessage(), e);
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 验证输入JSON是否有效
     * @param jsonString 要验证的JSON字符串
     * @return 如果JSON有效返回true，否则返回false
     */
    public boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}