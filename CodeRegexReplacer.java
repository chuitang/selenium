import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 代码正则替换工具类
 * 用于替换JSON字符串中的"code":占位符模式
 */
public class CodeRegexReplacer {
    
    /**
     * 方法1：使用负向前瞻断言的正则表达式（推荐）
     * 匹配"code":占位符，但排除 "", null, "DEVICE"
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithRegex(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 正则表达式：匹配"code":后面的值，但排除特定值
        // (?!...) 是负向前瞻断言，表示后面不能是指定的模式
        String regex = "\"code\"\\s*:\\s*(?!(\"\"|null|\"DEVICE\")(?:\\s*[,}]))([^,}]+)";
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonString);
        
        return matcher.replaceAll("\"code\":\"\\${code}\"");
    }
    
    /**
     * 方法2：使用更精确的正则表达式
     * 分别匹配字符串值和非字符串值
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodePrecise(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        String result = jsonString;
        
        // 匹配"code":"任意非空字符串"（但不是"DEVICE"）
        String stringRegex = "\"code\"\\s*:\\s*\"(?!\"|DEVICE\")[^\"]+\"";
        result = result.replaceAll(stringRegex, "\"code\":\"\\${code}\"");
        
        // 匹配"code":非字符串值（数字、布尔值等，但不是null）
        String nonStringRegex = "\"code\"\\s*:\\s*(?!null\\s*[,}])([^,}\"\\s]+)";
        result = result.replaceAll(nonStringRegex, "\"code\":\"\\${code}\"");
        
        return result;
    }
    
    /**
     * 方法3：使用Java代码逻辑处理（最精确）
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithLogic(String jsonString) {
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
                
                // 替换为${code}
                matcher.appendReplacement(result, "\"code\":\"\\${code}\"");
            } else {
                // 保持原值不变
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
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
            "{\"code\":123, \"description\":\"numeric code\"}",
            "{\"code\":true, \"enabled\":false}",
            "{\"items\":[{\"code\":\"ITEM_A\"}, {\"code\":\"ITEM_B\"}]}",
            
            // 不应该被替换的情况
            "{\"name\":\"test\", \"code\":\"\", \"type\":\"empty\"}",
            "{\"code\":null, \"status\":\"inactive\"}",
            "{\"code\":\"DEVICE\", \"type\":\"device\"}",
            
            // 混合情况
            "{\"user\":{\"code\":\"USER123\"}, \"device\":{\"code\":\"DEVICE\"}, \"empty\":{\"code\":\"\"}}",
            
            // 包含空格的情况
            "{\"code\" : \"SPACE_TEST\" , \"other\" : \"value\"}",
            "{\"code\" :   null   , \"test\": true}"
        };
        
        System.out.println("=== 方法1：使用负向前瞻断言 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeWithRegex(testCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法2：精确匹配 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodePrecise(testCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法3：逻辑处理 ===");
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + testCases[i]);
            System.out.println("结果: " + replaceCodeWithLogic(testCases[i]));
            System.out.println();
        }
    }
}