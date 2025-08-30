package com.example;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Boot集成的Jolt转换服务
 * 
 * 使用方式:
 * 1. 在Spring Boot应用中注入此服务
 * 2. 自动加载classpath下的规则文件
 * 3. 提供RESTful API接口
 */
@Service
public class SpringBootJoltService {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringBootJoltService.class);
    
    private final ResourceLoader resourceLoader;
    private final Map<String, Chainr> ruleCache;
    
    @Value("${jolt.rules.path:classpath:rules/}")
    private String rulesBasePath;
    
    @Value("${jolt.cache.enabled:true}")
    private boolean cacheEnabled;
    
    public SpringBootJoltService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.ruleCache = new ConcurrentHashMap<>();
    }
    
    @PostConstruct
    public void initialize() {
        if (cacheEnabled) {
            preloadRules();
        }
        logger.info("Jolt转换服务初始化完成，缓存状态: {}", cacheEnabled ? "启用" : "禁用");
    }
    
    /**
     * 预加载所有规则文件
     */
    private void preloadRules() {
        String[] ruleNames = {
            "combined_jolt_rule",
            "rule_cpsId", "rule_cpsWi", "rule_seqNo",
            "rule_orderItemReqArgs_0_itemId",
            "rule_orderItemReqArgs_0_itemPro_dp_group",
            "rule_orderItemReqArgs_0_itemPro_dp_package_code",
            "rule_orderSouce", "rule_salePortal",
            "rule_carrierInvoiceVOs_0_carrierCode"
        };
        
        for (String ruleName : ruleNames) {
            try {
                loadRule(ruleName);
                logger.debug("预加载规则成功: {}", ruleName);
            } catch (Exception e) {
                logger.warn("预加载规则失败: {}, 错误: {}", ruleName, e.getMessage());
            }
        }
        
        logger.info("预加载完成，已缓存规则数量: {}", ruleCache.size());
    }
    
    /**
     * 加载单个规则
     * 
     * @param ruleName 规则名称（不含.json后缀）
     * @return Chainr转换器
     */
    private Chainr loadRule(String ruleName) {
        if (cacheEnabled && ruleCache.containsKey(ruleName)) {
            return ruleCache.get(ruleName);
        }
        
        try {
            String resourcePath = rulesBasePath + ruleName + ".json";
            Resource resource = resourceLoader.getResource(resourcePath);
            
            if (!resource.exists()) {
                throw new RuntimeException("规则文件不存在: " + resourcePath);
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                List<Object> spec = JsonUtils.jsonToList(inputStream);
                Chainr chainr = Chainr.fromSpec(spec);
                
                if (cacheEnabled) {
                    ruleCache.put(ruleName, chainr);
                }
                
                return chainr;
            }
            
        } catch (IOException e) {
            logger.error("加载规则失败: {}", ruleName, e);
            throw new RuntimeException("加载规则失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行数据脱敏转换
     * 
     * @param inputJson 输入JSON字符串
     * @return 脱敏后的JSON字符串
     */
    public String maskSensitiveData(String inputJson) {
        return transform("combined_jolt_rule", inputJson);
    }
    
    /**
     * 执行指定规则的转换
     * 
     * @param ruleName 规则名称
     * @param inputJson 输入JSON字符串
     * @return 转换后的JSON字符串
     */
    public String transform(String ruleName, String inputJson) {
        try {
            Chainr chainr = loadRule(ruleName);
            Object input = JsonUtils.jsonToObject(inputJson);
            Object transformed = chainr.transform(input);
            return JsonUtils.toJsonString(transformed);
            
        } catch (Exception e) {
            logger.error("转换失败，规则: {}, 输入: {}", ruleName, inputJson, e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量转换
     * 
     * @param ruleName 规则名称
     * @param inputJsonList 输入JSON列表
     * @return 转换后的JSON列表
     */
    public List<String> batchTransform(String ruleName, List<String> inputJsonList) {
        Chainr chainr = loadRule(ruleName);
        
        return inputJsonList.stream()
                .map(inputJson -> {
                    try {
                        Object input = JsonUtils.jsonToObject(inputJson);
                        Object transformed = chainr.transform(input);
                        return JsonUtils.toJsonString(transformed);
                    } catch (Exception e) {
                        logger.error("批量转换失败，规则: {}, 输入: {}", ruleName, inputJson, e);
                        throw new RuntimeException("批量转换失败: " + e.getMessage(), e);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheEnabled", cacheEnabled);
        stats.put("cachedRulesCount", ruleCache.size());
        stats.put("cachedRuleNames", ruleCache.keySet());
        return stats;
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        ruleCache.clear();
        logger.info("规则缓存已清除");
    }
    
    /**
     * 重新加载所有规则
     */
    public void reloadRules() {
        clearCache();
        if (cacheEnabled) {
            preloadRules();
        }
        logger.info("规则重新加载完成");
    }
}