import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 精确代码替换工具类
 * 替换"code":任意值（排除空字符串、null、"DEVICE"）为"code":123
 */
public class PreciseCodeReplacer {
    
    /**
     * 方法1：使用精确的负向前瞻断言（推荐）
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodePrecise(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 正则表达式：精确匹配"code":值，但排除 "", null, "DEVICE"
        // 使用更精确的边界检测
        String regex = "\"code\"\\s*:\\s*(?!(\"\"|null|\"DEVICE\")(?=\\s*[,}]|\\s*$))[^,}]+?(?=\\s*[,}]|\\s*$)";
        
        return jsonString.replaceAll(regex, "\"code\":123");
    }
    
    /**
     * 方法2：分步处理，最精确的方法
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeStepByStep(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        String result = jsonString;
        
        // 第1步：处理非DEVICE和非空的字符串值
        String stringRegex = "\"code\"\\s*:\\s*\"(?!DEVICE\"|$)[^\"]+\"";
        result = result.replaceAll(stringRegex, "\"code\":123");
        
        // 第2步：处理非null的非字符串值（数字、布尔值等）
        String nonStringRegex = "\"code\"\\s*:\\s*(?!null(?:\\s*[,}]|\\s*$))([^,}\"\\s]+)";
        result = result.replaceAll(nonStringRegex, "\"code\":123");
        
        return result;
    }
    
    /**
     * 方法3：使用Matcher进行最精确的控制
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeWithMatcher(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 匹配所有"code":值的模式
        String regex = "\"code\"\\s*:\\s*([^,}]+?)(?=\\s*[,}]|\\s*$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonString);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String value = matcher.group(1).trim();
            
            // 检查是否为需要排除的值
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
     * 方法4：最简洁的单行正则表达式
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceCodeOneLiner(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 一行正则：排除空字符串、null、"DEVICE"的所有其他值
        return jsonString.replaceAll(
            "\"code\"\\s*:\\s*(?!(\"\"|null|\"DEVICE\")(?=\\s*[,}]|\\s*$))[^,}]+?(?=\\s*[,}]|\\s*$)", 
            "\"code\":123"
        );
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 用户提供的精确测试用例
        String[] userTestCases = {
            "\"code\":\"DEVICE\"",      // 应保持不变
            "\"code\":null",           // 应保持不变
            "\"code\":\"\"",           // 应保持不变
            "\"code\":\"CACHE\"",      // 应替换为123
            "{\"code\":\"AAAAA\"}"     // 应替换为123
        };
        
        // 额外的测试用例
        String[] additionalTestCases = {
            "{\"name\":\"test\", \"code\":\"ABC123\", \"type\":\"normal\"}",
            "{\"code\":123, \"description\":\"numeric code\"}",
            "{\"code\":true, \"enabled\":false}",
            "{\"code\":false, \"active\":true}",
            "{\"items\":[{\"code\":\"ITEM_A\"}, {\"code\":\"ITEM_B\"}]}",
            "{\"user\":{\"code\":\"USER123\"}, \"device\":{\"code\":\"DEVICE\"}, \"empty\":{\"code\":\"\"}}",
            "{\"code\" : \"SPACE_TEST\" , \"other\" : \"value\"}",
            "{\"code\" :   null   , \"test\": true}",
            "{\"code\":0, \"zero\":true}",
            "{\"code\":-1, \"negative\":true}"
        };
        
        String[] allTestCases = new String[userTestCases.length + additionalTestCases.length];
        System.arraycopy(userTestCases, 0, allTestCases, 0, userTestCases.length);
        System.arraycopy(additionalTestCases, 0, allTestCases, userTestCases.length, additionalTestCases.length);
        
        System.out.println("=== 用户示例验证 ===");
        for (int i = 0; i < userTestCases.length; i++) {
            System.out.println("用户示例 " + (i + 1) + ":");
            System.out.println("输入: " + userTestCases[i]);
            System.out.println("方法1: " + replaceCodePrecise(userTestCases[i]));
            System.out.println("方法2: " + replaceCodeStepByStep(userTestCases[i]));
            System.out.println("方法3: " + replaceCodeWithMatcher(userTestCases[i]));
            System.out.println("方法4: " + replaceCodeOneLiner(userTestCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法1：精确负向前瞻断言 ===");
        for (int i = 0; i < allTestCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + allTestCases[i]);
            System.out.println("结果: " + replaceCodePrecise(allTestCases[i]));
            System.out.println();
        }
        
        System.out.println("\n=== 方法3：Matcher精确控制（推荐）===");
        for (int i = 0; i < allTestCases.length; i++) {
            System.out.println("测试 " + (i + 1) + ":");
            System.out.println("原始: " + allTestCases[i]);
            System.out.println("结果: " + replaceCodeWithMatcher(allTestCases[i]));
            System.out.println();
        }
    }
}