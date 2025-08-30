package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jolt转换服务类
 * 提供高性能的JSON转换服务，支持规则缓存和批量处理
 * 
 * 兼容Java版本: 8, 11, 17, 21
 */
public class JoltTransformationService {
    
    private static final Logger logger = LoggerFactory.getLogger(JoltTransformationService.class);
    private final ObjectMapper objectMapper;
    private final Map<String, Chainr> chainrCache;
    
    public JoltTransformationService() {
        this.objectMapper = new ObjectMapper();
        this.chainrCache = new ConcurrentHashMap<>();
    }

    /**
     * 加载并缓存Jolt规则
     * 
     * @param ruleKey 规则缓存键
     * @param ruleFilePath 规则文件路径
     * @return Chainr转换器
     */
    public Chainr loadAndCacheRule(String ruleKey, String ruleFilePath) {
        return chainrCache.computeIfAbsent(ruleKey, key -> {
            try {
                logger.info("加载Jolt规则: {}", ruleFilePath);
                
                // 从文件系统加载规则
                File ruleFile = new File(ruleFilePath);
                if (!ruleFile.exists()) {
                    throw new RuntimeException("规则文件不存在: " + ruleFilePath);
                }
                
                try (InputStream inputStream = new FileInputStream(ruleFile)) {
                    List<Object> spec = JsonUtils.jsonToList(inputStream);
                    return Chainr.fromSpec(spec);
                }
                
            } catch (IOException e) {
                logger.error("加载规则文件失败: {}", ruleFilePath, e);
                throw new RuntimeException("加载规则失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 使用缓存的规则执行转换
     * 
     * @param ruleKey 规则缓存键
     * @param inputJson 输入JSON字符串
     * @return 转换后的JSON字符串
     */
    public String transform(String ruleKey, String inputJson) {
        Chainr chainr = chainrCache.get(ruleKey);
        if (chainr == null) {
            throw new RuntimeException("规则未找到或未加载: " + ruleKey);
        }
        
        try {
            Object input = JsonUtils.jsonToObject(inputJson);
            Object transformed = chainr.transform(input);
            return JsonUtils.toJsonString(transformed);
        } catch (Exception e) {
            logger.error("转换失败，规则: {}, 输入: {}", ruleKey, inputJson, e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 应用所有参数脱敏规则
     * 
     * @param inputJson 输入JSON字符串
     * @return 脱敏后的JSON字符串
     */
    public String applyDataMaskingRules(String inputJson) {
        // 加载组合规则
        String ruleKey = "combined_rule";
        loadAndCacheRule(ruleKey, "combined_jolt_rule.json");
        
        return transform(ruleKey, inputJson);
    }

    /**
     * 应用单个参数脱敏规则
     * 
     * @param parameterName 参数名称
     * @param inputJson 输入JSON字符串
     * @return 脱敏后的JSON字符串
     */
    public String applySingleRule(String parameterName, String inputJson) {
        String ruleKey = "rule_" + parameterName;
        String ruleFilePath = "rule_" + parameterName + ".json";
        
        loadAndCacheRule(ruleKey, ruleFilePath);
        return transform(ruleKey, inputJson);
    }

    /**
     * 预加载所有规则到缓存
     */
    public void preloadAllRules() {
        String[] ruleFiles = {
            "rule_cpsId.json",
            "rule_cpsWi.json", 
            "rule_seqNo.json",
            "rule_orderItemReqArgs_0_itemId.json",
            "rule_orderItemReqArgs_0_itemPro_dp_group.json",
            "rule_orderItemReqArgs_0_itemPro_dp_package_code.json",
            "rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemId.json",
            "rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemPro_dp_group.json",
            "rule_orderItemReqArgs_0_subOrderItemReqArgs_0_itemPro_dp_package_code.json",
            "rule_orderItemReqArgs_0_subOrderItemReqArgs_1_itemId.json",
            "rule_orderItemReqArgs_0_subOrderItemReqArgs_1_itemPro_dp_group.json",
            "rule_orderItemReqArgs_0_subOrderItemReqArgs_1_itemPro_dp_package_code.json",
            "rule_orderItemReqArgs_1_itemId.json",
            "rule_orderItemReqArgs_1_itemPro_dp_group.json",
            "rule_orderItemReqArgs_1_itemPro_dp_package_code.json",
            "rule_orderItemReqArgs_1_subOrderItemReqArgs_0_itemId.json",
            "rule_orderItemReqArgs_1_subOrderItemReqArgs_0_itemPro_dp_group.json",
            "rule_orderItemReqArgs_1_subOrderItemReqArgs_0_itemPro_dp_package_code.json",
            "rule_couponList_0_couponCodes.json",
            "rule_orderSouce.json",
            "rule_salePortal.json",
            "rule_carrierInvoiceVOs_0_carrierCode.json",
            "combined_jolt_rule.json"
        };
        
        for (String ruleFile : ruleFiles) {
            String ruleKey = ruleFile.replace(".json", "");
            try {
                loadAndCacheRule(ruleKey, ruleFile);
                logger.info("预加载规则成功: {}", ruleFile);
            } catch (Exception e) {
                logger.warn("预加载规则失败: {}", ruleFile, e);
            }
        }
    }

    /**
     * 清除规则缓存
     */
    public void clearCache() {
        chainrCache.clear();
        logger.info("规则缓存已清除");
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存大小
     */
    public int getCacheSize() {
        return chainrCache.size();
    }

    /**
     * 检查规则是否已缓存
     * 
     * @param ruleKey 规则键
     * @return 是否已缓存
     */
    public boolean isRuleCached(String ruleKey) {
        return chainrCache.containsKey(ruleKey);
    }
}