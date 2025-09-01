-- ========================================
-- 场景测试系统数据库表结构设计
-- ========================================

-- 创建数据库（可选）
-- CREATE DATABASE scenario_test_system DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- USE scenario_test_system;

-- ========================================
-- 1. 场景模板生成规则表
-- ========================================
CREATE TABLE `scenario_template_rules` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `api_params` JSON COMMENT '接口参数（JSON格式存储）',
    `process_rule` TEXT COMMENT '处理规则描述',
    `rule_expression` VARCHAR(500) COMMENT '规则表达式',
    `usage_scope` VARCHAR(200) NOT NULL COMMENT '使用范围（单接口）',
    `creator` VARCHAR(50) NOT NULL COMMENT '创建人',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modifier` VARCHAR(50) COMMENT '修改人',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序字段',
    PRIMARY KEY (`id`),
    INDEX `idx_rule_name` (`rule_name`),
    INDEX `idx_usage_scope` (`usage_scope`),
    INDEX `idx_status_sort` (`status`, `sort_order`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景模板生成规则表';

-- ========================================
-- 2. 场景参数池配置表
-- ========================================
CREATE TABLE `scenario_param_pool` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `param_name` VARCHAR(100) NOT NULL COMMENT '参数名称',
    `placeholder` VARCHAR(100) NOT NULL COMMENT '占位符',
    `usage_method` VARCHAR(50) NOT NULL COMMENT '使用方式',
    `param_value` TEXT COMMENT '参数值',
    `usage_scope` VARCHAR(200) NOT NULL COMMENT '使用范围（单接口）',
    `creator` VARCHAR(50) NOT NULL COMMENT '创建人',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modifier` VARCHAR(50) COMMENT '修改人',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序字段',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_param_placeholder` (`param_name`, `placeholder`),
    INDEX `idx_usage_scope` (`usage_scope`),
    INDEX `idx_status_sort` (`status`, `sort_order`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景参数池配置表';

-- ========================================
-- 3. 场景规则表
-- ========================================
CREATE TABLE `scenario_rules` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_field` VARCHAR(100) NOT NULL COMMENT '规则字段',
    `chinese_desc` VARCHAR(200) NOT NULL COMMENT '中文描述',
    `config_enum` JSON COMMENT '配置枚举（JSON数组格式）',
    `usage_scope` VARCHAR(200) NOT NULL COMMENT '使用范围（单接口）',
    `creator` VARCHAR(50) NOT NULL COMMENT '创建人',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modifier` VARCHAR(50) COMMENT '修改人',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序字段',
    PRIMARY KEY (`id`),
    INDEX `idx_rule_field` (`rule_field`),
    INDEX `idx_usage_scope` (`usage_scope`),
    INDEX `idx_status_sort` (`status`, `sort_order`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景规则表';

-- ========================================
-- 4. 场景管理表
-- ========================================
CREATE TABLE `scenario_management` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `header_params` JSON COMMENT '头部参数（JSON格式存储）',
    `request_body_params` JSON COMMENT '请求体参数（JSON格式存储）',
    `md5_value` VARCHAR(32) NOT NULL COMMENT 'MD5值',
    `last_execute_time` TIMESTAMP NULL COMMENT '最后执行时间',
    `last_execute_status` TINYINT COMMENT '最后执行状态：0-失败，1-成功，2-执行中',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '执行成功数',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_md5_value` (`md5_value`),
    INDEX `idx_last_execute_time` (`last_execute_time`),
    INDEX `idx_last_execute_status` (`last_execute_status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景管理表';

-- ========================================
-- 5. 场景执行历史表
-- ========================================
CREATE TABLE `scenario_execute_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `scenario_id` BIGINT UNSIGNED NOT NULL COMMENT '场景ID，关联scenario_management表',
    `execute_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `execute_status` TINYINT NOT NULL COMMENT '执行状态：0-失败，1-成功，2-执行中',
    `execute_result` LONGTEXT COMMENT '执行结果报文',
    PRIMARY KEY (`id`),
    INDEX `idx_scenario_id` (`scenario_id`),
    INDEX `idx_execute_time` (`execute_time`),
    INDEX `idx_execute_status` (`execute_status`),
    CONSTRAINT `fk_scenario_execute_history_scenario_id` 
        FOREIGN KEY (`scenario_id`) REFERENCES `scenario_management` (`id`) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景执行历史表';

-- ========================================
-- 创建数据库视图（可选）
-- ========================================

-- 场景执行统计视图
CREATE VIEW `v_scenario_execute_stats` AS
SELECT 
    sm.id as scenario_id,
    sm.md5_value,
    sm.success_count,
    sm.last_execute_time,
    sm.last_execute_status,
    COUNT(seh.id) as total_execute_count,
    SUM(CASE WHEN seh.execute_status = 1 THEN 1 ELSE 0 END) as success_execute_count,
    SUM(CASE WHEN seh.execute_status = 0 THEN 1 ELSE 0 END) as failed_execute_count
FROM scenario_management sm
LEFT JOIN scenario_execute_history seh ON sm.id = seh.scenario_id
GROUP BY sm.id, sm.md5_value, sm.success_count, sm.last_execute_time, sm.last_execute_status;

-- ========================================
-- 插入示例数据（可选）
-- ========================================

-- 示例：场景模板生成规则
INSERT INTO `scenario_template_rules` (`rule_name`, `api_params`, `process_rule`, `rule_expression`, `usage_scope`, `creator`) VALUES
('用户登录规则', '{"username": "string", "password": "string"}', '验证用户名和密码格式', 'username.length >= 3 && password.length >= 6', '/api/user/login', 'admin'),
('数据查询规则', '{"page": "number", "size": "number"}', '分页参数验证', 'page >= 1 && size <= 100', '/api/data/query', 'admin');

-- 示例：场景参数池配置
INSERT INTO `scenario_param_pool` (`param_name`, `placeholder`, `usage_method`, `param_value`, `usage_scope`, `creator`) VALUES
('测试用户名', '{{username}}', 'random', '["testuser1", "testuser2", "testuser3"]', '/api/user/login', 'admin'),
('测试密码', '{{password}}', 'fixed', 'test123456', '/api/user/login', 'admin');

-- 示例：场景规则
INSERT INTO `scenario_rules` (`rule_field`, `chinese_desc`, `config_enum`, `usage_scope`, `creator`) VALUES
('response_time', '响应时间限制', '["<1000ms", "<2000ms", "<5000ms"]', '/api/user/login', 'admin'),
('status_code', '状态码检查', '["200", "400", "401", "500"]', '/api/user/login', 'admin');