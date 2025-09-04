import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 简单代码替换工具类
 * 替换"code":"非DEVICE的值" 为 "code":123
 */
public class SimpleCodeReplacer {
    
    /**
     * 方法1：使用负向前瞻断言（推荐）
     * 匹配"code":"任意字符串"，但排除"DEVICE"
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithNegativeLookahead(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 正则表达式：匹配"code":"任意字符串"，但不是"DEVICE"
        // (?!DEVICE") 负向前瞻断言，确保不是DEVICE
        String regex = "\"code\"\\s*:\\s*\"(?!DEVICE\")[^\"]*\"";
        
        return jsonString.replaceAll(regex, "\"code\":123");
    }
    
    /**
     * 方法2：使用字符类排除法
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithCharacterClass(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 匹配"code":"任意非DEVICE的字符串"
        // 使用更复杂的模式来确保不匹配DEVICE
        String regex = "\"code\"\\s*:\\s*\"(?:(?!DEVICE)[^\"]+)\"";
        
        return jsonString.replaceAll(regex, "\"code\":123");
    }
    
    /**
     * 方法3：使用Matcher进行精确控制
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithMatcher(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 匹配"code":"任意字符串"
        String regex = "\"code\"\\s*:\\s*\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonString);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String value = matcher.group(1);
            
            // 如果值不是"DEVICE"，则替换
            if (!"DEVICE".equals(value)) {
                matcher.appendReplacement(result, "\"code\":123");
            } else {
                // 保持原值
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 方法4：最简单的正则表达式（如果确定格式固定）
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeSimple(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 先用简单正则匹配所有"code":"任意字符串"，再排除DEVICE
        String temp = jsonString.replaceAll("\"code\"\\s*:\\s*\"[^\"]*\"", "\"code\":123");
        
        // 如果原来有"code":"DEVICE"，需要恢复
        return temp.replaceAll("\"code\":123", "\"code\":\"DEVICE\"")
                  .replaceAll("\"code\"\\s*:\\s*\"(?!DEVICE\")[^\"]*\"", "\"code\":123");
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
            // 应该被替换的情况
            "{\"name\":\"test\", \"code\":\"ABC123\", \"type\":\"normal\"}",
            "{\"code\":\"USER_TYPE\", \"status\":\"active\"}",
            "{\"code\":\"ADMIN\", \"role\":\"administrator\"}",
            "{\"code\":\"GUEST\", \"permissions\":[]}",
            "{\"items\":[{\"code\":\"ITEM_A\"}, {\"code\":\"ITEM_B\"}]}",
            
            // 不应该被替换的情况
            "{\"code\":\"DEVICE\", \"type\":\"device\"}",
            "{\"device\":{\"code\":\"DEVICE\"}, \"user\":{\"code\":\"USER123\"}}",
            
            // 包含空格的情况
            "{\"code\" : \"SPACE_TEST\" , \"other\" : \"value\"}",
            "{\"code\" : \"DEVICE\" , \"test\": true}",
            
            // 混合情况
            "{\"primary\":{\"code\":\"PRIMARY\"}, \"secondary\":{\"code\":\"DEVICE\"}}"
        };
        
        System.out.println("=== 方法1：负向前瞻断言 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeWithNegativeLookahead(testCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法2：字符类排除法 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeWithCharacterClass(testCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法3：Matcher精确控制 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeWithMatcher(testCases[i]));
            System.out.println();
        }
    }
}