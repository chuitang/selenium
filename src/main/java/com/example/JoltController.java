package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Jolt转换REST API控制器
 * 提供HTTP接口进行JSON数据转换
 */
@RestController
@RequestMapping("/api/jolt")
public class JoltController {
    
    private static final Logger logger = LoggerFactory.getLogger(JoltController.class);
    
    @Autowired
    private SpringBootJoltService joltService;
    
    /**
     * 数据脱敏接口
     * 
     * @param request 包含原始JSON数据的请求
     * @return 脱敏后的JSON数据
     */
    @PostMapping("/mask")
    public ResponseEntity<?> maskData(@RequestBody Map<String, Object> request) {
        try {
            String inputJson = (String) request.get("data");
            if (inputJson == null || inputJson.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("输入数据不能为空");
            }
            
            String maskedData = joltService.maskSensitiveData(inputJson);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "maskedData", maskedData,
                "message", "数据脱敏成功"
            ));
            
        } catch (Exception e) {
            logger.error("数据脱敏失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "数据脱敏失败"
            ));
        }
    }
    
    /**
     * 使用指定规则转换数据
     * 
     * @param ruleName 规则名称
     * @param request 包含JSON数据的请求
     * @return 转换后的数据
     */
    @PostMapping("/transform/{ruleName}")
    public ResponseEntity<?> transformWithRule(
            @PathVariable String ruleName, 
            @RequestBody Map<String, Object> request) {
        try {
            String inputJson = (String) request.get("data");
            if (inputJson == null || inputJson.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("输入数据不能为空");
            }
            
            String transformedData = joltService.transform(ruleName, inputJson);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transformedData", transformedData,
                "ruleName", ruleName,
                "message", "转换成功"
            ));
            
        } catch (Exception e) {
            logger.error("转换失败，规则: {}", ruleName, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "ruleName", ruleName,
                "message", "转换失败"
            ));
        }
    }
    
    /**
     * 批量转换接口
     * 
     * @param ruleName 规则名称
     * @param request 包含JSON数据列表的请求
     * @return 批量转换结果
     */
    @PostMapping("/batch/{ruleName}")
    public ResponseEntity<?> batchTransform(
            @PathVariable String ruleName,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> inputJsonList = (List<String>) request.get("dataList");
            
            if (inputJsonList == null || inputJsonList.isEmpty()) {
                return ResponseEntity.badRequest().body("输入数据列表不能为空");
            }
            
            List<String> transformedDataList = joltService.batchTransform(ruleName, inputJsonList);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transformedDataList", transformedDataList,
                "ruleName", ruleName,
                "processedCount", transformedDataList.size(),
                "message", "批量转换成功"
            ));
            
        } catch (Exception e) {
            logger.error("批量转换失败，规则: {}", ruleName, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "ruleName", ruleName,
                "message", "批量转换失败"
            ));
        }
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = joltService.getCacheStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 清除缓存
     * 
     * @return 操作结果
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearCache() {
        try {
            joltService.clearCache();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "缓存清除成功"
            ));
        } catch (Exception e) {
            logger.error("清除缓存失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "清除缓存失败"
            ));
        }
    }
    
    /**
     * 重新加载规则
     * 
     * @return 操作结果
     */
    @PostMapping("/rules/reload")
    public ResponseEntity<?> reloadRules() {
        try {
            joltService.reloadRules();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "规则重新加载成功"
            ));
        } catch (Exception e) {
            logger.error("重新加载规则失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "重新加载规则失败"
            ));
        }
    }
    
    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "JoltTransformationService",
            "cacheStats", joltService.getCacheStats(),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(health);
    }
}