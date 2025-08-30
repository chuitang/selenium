package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 修复版本的Jolt转换服务
 * 使用Java代码实现条件逻辑，避免Jolt语法兼容性问题
 */
public class FixedJoltTransformationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FixedJoltTransformationService.class);
    private final ObjectMapper objectMapper;
    private final ConditionalJoltTransformer conditionalTransformer;
    
    public FixedJoltTransformationService() {
        this.objectMapper = new ObjectMapper();
        this.conditionalTransformer = new ConditionalJoltTransformer();
    }
    
    /**
     * 应用所有数据脱敏规则
     * 
     * @param inputJson 输入JSON字符串
     * @return 脱敏后的JSON字符串
     */
    public String applyAllDataMaskingRules(String inputJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(inputJson);
            
            // 应用条件规则
            applyConditionalRules(jsonNode);
            
            // 应用无条件规则
            applyUnconditionalRules(jsonNode);
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            logger.error("数据脱敏失败", e);
            throw new RuntimeException("数据脱敏失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 应用条件规则（只有在字段不为空时才替换）
     */
    private void applyConditionalRules(JsonNode jsonNode) {
        ObjectNode objectNode = (ObjectNode) jsonNode;
        
        // cpsId规则
        if (jsonNode.has("cpsId") && !jsonNode.get("cpsId").isNull() && 
            !jsonNode.get("cpsId").asText().isEmpty()) {
            objectNode.put("cpsId", "${cpsId1111}");
        }
        
        // cpsWi规则
        if (jsonNode.has("cpsWi") && !jsonNode.get("cpsWi").isNull() && 
            !jsonNode.get("cpsWi").asText().isEmpty()) {
            objectNode.put("cpsWi", "${cpsWi1111}");
        }
        
        // seqNo规则
        if (jsonNode.has("seqNo") && !jsonNode.get("seqNo").isNull() && 
            !jsonNode.get("seqNo").asText().isEmpty()) {
            objectNode.put("seqNo", "${seqNo1111}");
        }
        
        // orderItemReqArgs条件规则
        applyOrderItemConditionalRules(jsonNode);
        
        // couponList条件规则
        applyCouponListConditionalRules(jsonNode);
        
        // carrierInvoiceVOs特殊条件规则
        applyCarrierCodeConditionalRule(jsonNode);
    }
    
    /**
     * 应用orderItemReqArgs的条件规则
     */
    private void applyOrderItemConditionalRules(JsonNode jsonNode) {
        JsonNode orderItemsNode = jsonNode.get("orderItemReqArgs");
        if (orderItemsNode != null && orderItemsNode.isArray()) {
            ArrayNode orderItems = (ArrayNode) orderItemsNode;
            
            // 处理orderItemReqArgs[0]
            if (orderItems.size() > 0) {
                JsonNode item0 = orderItems.get(0);
                if (item0 != null) {
                    ObjectNode item0Obj = (ObjectNode) item0;
                    
                    // itemPro.dp_group
                    JsonNode dpGroup = item0.at("/itemPro/dp_group");
                    if (!dpGroup.isMissingNode() && !dpGroup.isNull() && !dpGroup.asText().isEmpty()) {
                        ((ObjectNode) item0.get("itemPro")).put("dp_group", "${dp_group00}");
                    }
                    
                    // itemPro.dp_package_code
                    JsonNode dpPackageCode = item0.at("/itemPro/dp_package_code");
                    if (!dpPackageCode.isMissingNode() && !dpPackageCode.isNull() && !dpPackageCode.asText().isEmpty()) {
                        ((ObjectNode) item0.get("itemPro")).put("dp_package_code", "${dp_package_code00}");
                    }
                    
                    // 处理subOrderItemReqArgs
                    applySubOrderItemConditionalRules(item0, "0");
                }
            }
            
            // 处理orderItemReqArgs[1]
            if (orderItems.size() > 1) {
                JsonNode item1 = orderItems.get(1);
                if (item1 != null) {
                    // itemPro.dp_group
                    JsonNode dpGroup = item1.at("/itemPro/dp_group");
                    if (!dpGroup.isMissingNode() && !dpGroup.isNull() && !dpGroup.asText().isEmpty()) {
                        ((ObjectNode) item1.get("itemPro")).put("dp_group", "${dp_group01}");
                    }
                    
                    // itemPro.dp_package_code
                    JsonNode dpPackageCode = item1.at("/itemPro/dp_package_code");
                    if (!dpPackageCode.isMissingNode() && !dpPackageCode.isNull() && !dpPackageCode.asText().isEmpty()) {
                        ((ObjectNode) item1.get("itemPro")).put("dp_package_code", "${dp_package_code01}");
                    }
                    
                    // 处理subOrderItemReqArgs
                    applySubOrderItemConditionalRules(item1, "1");
                }
            }
        }
    }
    
    /**
     * 应用subOrderItemReqArgs的条件规则
     */
    private void applySubOrderItemConditionalRules(JsonNode orderItem, String parentIndex) {
        JsonNode subOrderItemsNode = orderItem.get("subOrderItemReqArgs");
        if (subOrderItemsNode != null && subOrderItemsNode.isArray()) {
            ArrayNode subOrderItems = (ArrayNode) subOrderItemsNode;
            
            for (int i = 0; i < subOrderItems.size(); i++) {
                JsonNode subItem = subOrderItems.get(i);
                if (subItem != null) {
                    ObjectNode subItemObj = (ObjectNode) subItem;
                    String suffix = parentIndex + String.format("%02d", i);
                    
                    // itemId
                    JsonNode itemId = subItem.get("itemId");
                    if (itemId != null && !itemId.isNull() && !itemId.asText().isEmpty()) {
                        subItemObj.put("itemId", "${itemId" + parentIndex + String.format("%02d", i) + "}");
                    }
                    
                    // itemPro.dp_group
                    JsonNode dpGroup = subItem.at("/itemPro/dp_group");
                    if (!dpGroup.isMissingNode() && !dpGroup.isNull() && !dpGroup.asText().isEmpty()) {
                        ((ObjectNode) subItem.get("itemPro")).put("dp_group", "${dp_group" + parentIndex + String.format("%02d", i) + "}");
                    }
                    
                    // itemPro.dp_package_code
                    JsonNode dpPackageCode = subItem.at("/itemPro/dp_package_code");
                    if (!dpPackageCode.isMissingNode() && !dpPackageCode.isNull() && !dpPackageCode.asText().isEmpty()) {
                        ((ObjectNode) subItem.get("itemPro")).put("dp_package_code", "${dp_package_code" + parentIndex + String.format("%02d", i) + "}");
                    }
                }
            }
        }
    }
    
    /**
     * 应用couponList条件规则
     */
    private void applyCouponListConditionalRules(JsonNode jsonNode) {
        JsonNode couponListNode = jsonNode.get("couponList");
        if (couponListNode != null && couponListNode.isArray()) {
            ArrayNode couponList = (ArrayNode) couponListNode;
            
            if (couponList.size() > 0) {
                JsonNode coupon0 = couponList.get(0);
                if (coupon0 != null) {
                    JsonNode couponCodes = coupon0.get("couponCodes");
                    if (couponCodes != null && !couponCodes.isNull() && !couponCodes.asText().isEmpty()) {
                        ((ObjectNode) coupon0).put("couponCodes", "${couponCodes0000}");
                    }
                }
            }
        }
    }
    
    /**
     * 应用carrierCode特殊条件规则
     */
    private void applyCarrierCodeConditionalRule(JsonNode jsonNode) {
        JsonNode carrierArrayNode = jsonNode.get("carrierInvoiceVOs");
        if (carrierArrayNode != null && carrierArrayNode.isArray()) {
            ArrayNode carrierArray = (ArrayNode) carrierArrayNode;
            
            if (carrierArray.size() > 0) {
                JsonNode carrier0 = carrierArray.get(0);
                if (carrier0 != null) {
                    JsonNode carrierCode = carrier0.get("carrierCode");
                    if (carrierCode != null && !carrierCode.isNull()) {
                        String carrierCodeValue = carrierCode.asText();
                        // 只有在不为空且不等于VMALL-HUAWEIDEVICE时才替换
                        if (!carrierCodeValue.isEmpty() && !"VMALL-HUAWEIDEVICE".equals(carrierCodeValue)) {
                            ((ObjectNode) carrier0).put("carrierCode", "${carrierCode0000}");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 应用无条件规则（总是替换）
     */
    private void applyUnconditionalRules(JsonNode jsonNode) {
        ObjectNode objectNode = (ObjectNode) jsonNode;
        
        // orderSouce - 总是替换
        if (jsonNode.has("orderSouce")) {
            objectNode.put("orderSouce", "${orderSouce00}");
        }
        
        // salePortal - 总是替换
        if (jsonNode.has("salePortal")) {
            objectNode.put("salePortal", "${salePortal00}");
        }
        
        // orderItemReqArgs无条件规则
        JsonNode orderItemsNode = jsonNode.get("orderItemReqArgs");
        if (orderItemsNode != null && orderItemsNode.isArray()) {
            ArrayNode orderItems = (ArrayNode) orderItemsNode;
            
            // orderItemReqArgs[0].itemId - 总是替换
            if (orderItems.size() > 0) {
                JsonNode item0 = orderItems.get(0);
                if (item0 != null) {
                    ((ObjectNode) item0).put("itemId", "${itemId00}");
                }
            }
            
            // orderItemReqArgs[1].itemId - 总是替换
            if (orderItems.size() > 1) {
                JsonNode item1 = orderItems.get(1);
                if (item1 != null) {
                    ((ObjectNode) item1).put("itemId", "${itemId01}");
                }
            }
        }
    }
}