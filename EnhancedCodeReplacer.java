import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 增强版代码替换工具类
 * 替换"code":任意值（排除空、null、DEVICE）为"code":123
 */
public class EnhancedCodeReplacer {
    
    /**
     * 方法1：使用复合负向前瞻断言（推荐）
     * 匹配"code":任意值，但排除 "", null, "DEVICE"
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithComplexLookahead(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 正则表达式：匹配"code":任意值，但排除空字符串、null、"DEVICE"
        // (?!...) 负向前瞻断言，确保后面不是指定的模式
        String regex = "\"code\"\\s*:\\s*(?!(\"\"|null|\"DEVICE\")(?:\\s*[,}]|$))[^,}]+";
        
        return jsonString.replaceAll(regex, "\"code\":123");
    }
    
    /**
     * 方法2：分别处理字符串和非字符串值
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeSeparately(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        String result = jsonString;
        
        // 处理字符串值：匹配"code":"任意非空非DEVICE字符串"
        String stringRegex = "\"code\"\\s*:\\s*\"(?!\"|DEVICE\")[^\"]+\"";
        result = result.replaceAll(stringRegex, "\"code\":123");
        
        // 处理非字符串值：匹配"code":非null的其他值（数字、布尔值等）
        String nonStringRegex = "\"code\"\\s*:\\s*(?!null(?:\\s*[,}]))([^,}\"\\s]+)";
        result = result.replaceAll(nonStringRegex, "\"code\":123");
        
        return result;
    }
    
    /**
     * 方法3：使用Matcher进行精确控制（最精确）
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithMatcher(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 匹配"code":任意值的模式
        String regex = "\"code\"\\s*:\\s*([^,}]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonString);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String value = matcher.group(1).trim();
            
            // 检查是否为排除的值
            if (!value.equals("\"\"") && 
                !value.equals("null") && 
                !value.equals("\"DEVICE\"")) {
                
                // 替换为123
                matcher.appendReplacement(result, "\"code\":123");
            } else {
                // 保持原值不变
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 方法4：最简化的正则表达式
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeSimplified(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 使用更简洁的正则，排除特定值
        String regex = "\"code\"\\s*:\\s*(?!(\"\"|null|\"DEVICE\"))[^,}]+";
        
        return jsonString.replaceAll(regex, "\"code\":123");
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
            // 用户提供的示例
            "\"code\":\"DEVICE\"",
            "\"code\":\"CACHE\"", 
            "{\"code\":\"AAAAA\"}",
            
            // 应该被替换的情况
            "{\"name\":\"test\", \"code\":\"ABC123\", \"type\":\"normal\"}",
            "{\"code\":\"USER_TYPE\", \"status\":\"active\"}",
            "{\"code\":123, \"description\":\"numeric code\"}",
            "{\"code\":true, \"enabled\":false}",
            "{\"code\":false, \"active\":true}",
            "{\"items\":[{\"code\":\"ITEM_A\"}, {\"code\":\"ITEM_B\"}]}",
            
            // 不应该被替换的情况
            "{\"name\":\"test\", \"code\":\"\", \"type\":\"empty\"}",
            "{\"code\":null, \"status\":\"inactive\"}",
            "{\"code\":\"DEVICE\", \"type\":\"device\"}",
            
            // 混合情况
            "{\"user\":{\"code\":\"USER123\"}, \"device\":{\"code\":\"DEVICE\"}, \"empty\":{\"code\":\"\"}}",
            
            // 包含空格的情况
            "{\"code\" : \"SPACE_TEST\" , \"other\" : \"value\"}",
            "{\"code\" :   null   , \"test\": true}",
            "{\"code\" : \"DEVICE\" , \"test\": false}",
            
            // 边界情况
            "{\"code\":0, \"zero\":true}",
            "{\"code\":-1, \"negative\":true}",
            "{\"code\":\"123\", \"stringNumber\":true}"
        };
        
        System.out.println("=== 方法1：复合负向前瞻断言 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeWithComplexLookahead(testCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法2：分别处理 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeSeparately(testCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法3：Matcher精确控制 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeWithMatcher(testCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法4：简化版本 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeSimplified(testCases[i]));
            System.out.println();
        }
    }
}