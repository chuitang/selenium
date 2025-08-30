package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 带条件逻辑的Jolt转换器
 * 解决复杂条件表达式在某些Jolt版本中不兼容的问题
 */
public class ConditionalJoltTransformer {
    
    private static final Logger logger = LoggerFactory.getLogger(ConditionalJoltTransformer.class);
    private final ObjectMapper objectMapper;
    
    public ConditionalJoltTransformer() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 应用cpsId规则：如果不为空则替换为${cpsId1111}
     */
    public String applyCpsIdRule(String inputJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(inputJson);
            
            if (jsonNode.has("cpsId")) {
                JsonNode cpsIdNode = jsonNode.get("cpsId");
                if (!cpsIdNode.isNull() && !cpsIdNode.asText().isEmpty()) {
                    ((ObjectNode) jsonNode).put("cpsId", "${cpsId1111}");
                }
            }
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            logger.error("应用cpsId规则失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 应用cpsWi规则：如果不为空则替换为${cpsWi1111}
     */
    public String applyCpsWiRule(String inputJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(inputJson);
            
            if (jsonNode.has("cpsWi")) {
                JsonNode cpsWiNode = jsonNode.get("cpsWi");
                if (!cpsWiNode.isNull() && !cpsWiNode.asText().isEmpty()) {
                    ((ObjectNode) jsonNode).put("cpsWi", "${cpsWi1111}");
                }
            }
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            logger.error("应用cpsWi规则失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 应用seqNo规则：如果不为空则替换为${seqNo1111}
     */
    public String applySeqNoRule(String inputJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(inputJson);
            
            if (jsonNode.has("seqNo")) {
                JsonNode seqNoNode = jsonNode.get("seqNo");
                if (!seqNoNode.isNull() && !seqNoNode.asText().isEmpty()) {
                    ((ObjectNode) jsonNode).put("seqNo", "${seqNo1111}");
                }
            }
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            logger.error("应用seqNo规则失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 应用carrierCode特殊规则：排除VMALL-HUAWEIDEVICE
     */
    public String applyCarrierCodeRule(String inputJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(inputJson);
            
            JsonNode carrierArrayNode = jsonNode.at("/carrierInvoiceVOs");
            if (carrierArrayNode.isArray() && carrierArrayNode.size() > 0) {
                JsonNode firstCarrier = carrierArrayNode.get(0);
                if (firstCarrier.has("carrierCode")) {
                    JsonNode carrierCodeNode = firstCarrier.get("carrierCode");
                    String carrierCode = carrierCodeNode.asText();
                    
                    // 只有在不为空且不等于VMALL-HUAWEIDEVICE时才替换
                    if (!carrierCodeNode.isNull() && 
                        !carrierCode.isEmpty() && 
                        !"VMALL-HUAWEIDEVICE".equals(carrierCode)) {
                        ((ObjectNode) firstCarrier).put("carrierCode", "${carrierCode0000}");
                    }
                }
            }
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            logger.error("应用carrierCode规则失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 应用所有条件规则
     */
    public String applyAllConditionalRules(String inputJson) {
        String result = inputJson;
        
        // 按顺序应用所有条件规则
        result = applyCpsIdRule(result);
        result = applyCpsWiRule(result);
        result = applySeqNoRule(result);
        result = applyCarrierCodeRule(result);
        
        // 应用无条件规则（使用简单的Jolt规则）
        result = applyUnconditionalRules(result);
        
        return result;
    }
    
    /**
     * 应用无条件规则（总是替换的规则）
     */
    private String applyUnconditionalRules(String inputJson) {
        // 使用简单的Jolt规则处理无条件替换
        List<Object> spec = JsonUtils.jsonToList(
            "[{" +
            "\"operation\": \"shift\"," +
            "\"spec\": {" +
            "\"orderItemReqArgs\": {" +
            "\"0\": {" +
            "\"itemId\": \"orderItemReqArgs[0].itemId\"," +
            "\"*\": \"orderItemReqArgs[0].&\"" +
            "}," +
            "\"1\": {" +
            "\"itemId\": \"orderItemReqArgs[1].itemId\"," +
            "\"*\": \"orderItemReqArgs[1].&\"" +
            "}," +
            "\"*\": \"orderItemReqArgs[&]\"" +
            "}," +
            "\"orderSouce\": \"orderSouce\"," +
            "\"salePortal\": \"salePortal\"," +
            "\"*\": \"&\"" +
            "}" +
            "}," +
            "{" +
            "\"operation\": \"default\"," +
            "\"spec\": {" +
            "\"orderItemReqArgs[0].itemId\": \"${itemId00}\"," +
            "\"orderItemReqArgs[1].itemId\": \"${itemId01}\"," +
            "\"orderSouce\": \"${orderSouce00}\"," +
            "\"salePortal\": \"${salePortal00}\"" +
            "}" +
            "}]"
        );
        
        try {
            Chainr chainr = Chainr.fromSpec(spec);
            Object input = JsonUtils.jsonToObject(inputJson);
            Object transformed = chainr.transform(input);
            return JsonUtils.toJsonString(transformed);
        } catch (Exception e) {
            logger.error("应用无条件规则失败", e);
            return inputJson; // 返回原始数据
        }
    }
}