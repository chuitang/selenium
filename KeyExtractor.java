import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Key提取工具类
 * 从${key}格式的字符串中提取key值
 */
public class KeyExtractor {
    
    /**
     * 方法1：使用正则表达式提取key（推荐）
     * @param input 输入字符串
     * @return 提取的key值，如果不匹配则返回null
     */
    public static String extractKeyWithRegex(String input) {
        if (input == null) {
            return null;
        }
        
        // 正则表达式：匹配${key}格式，捕获key部分
        // ^\\$\\{(.+)\\}$ 确保整个字符串完全匹配${key}格式
        String regex = "^\\$\\{(.+)\\}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.matches()) {
            return matcher.group(1); // 返回第一个捕获组（key部分）
        }
        
        return null;
    }
    
    /**
     * 方法2：使用字符串操作提取key（高性能）
     * @param input 输入字符串
     * @return 提取的key值，如果不匹配则返回null
     */
    public static String extractKeyWithStringOps(String input) {
        if (input == null) {
            return null;
        }
        
        // 检查字符串格式：必须以${开头，以}结尾，且长度至少为4
        if (input.length() < 4 || 
            !input.startsWith("${") || 
            !input.endsWith("}")) {
            return null;
        }
        
        // 提取中间的key部分
        String key = input.substring(2, input.length() - 1);
        
        // 确保key不为空
        if (key.isEmpty()) {
            return null;
        }
        
        return key;
    }
    
    /**
     * 方法3：更严格的正则表达式（只允许字母数字下划线）
     * @param input 输入字符串
     * @return 提取的key值，如果不匹配则返回null
     */
    public static String extractKeyStrict(String input) {
        if (input == null) {
            return null;
        }
        
        // 严格的正则表达式：key只能包含字母、数字、下划线
        String regex = "^\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 方法4：支持复杂key格式（点号、中划线等）
     * @param input 输入字符串
     * @return 提取的key值，如果不匹配则返回null
     */
    public static String extractKeyComplex(String input) {
        if (input == null) {
            return null;
        }
        
        // 支持更复杂的key格式：字母、数字、下划线、点号、中划线
        String regex = "^\\$\\{([a-zA-Z_][a-zA-Z0-9_.-]*)\\}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 方法5：最宽松的提取（允许任意字符作为key）
     * @param input 输入字符串
     * @return 提取的key值，如果不匹配则返回null
     */
    public static String extractKeyPermissive(String input) {
        if (input == null) {
            return null;
        }
        
        // 最宽松的正则：key可以是任意非空字符
        String regex = "^\\$\\{(.+?)\\}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.matches()) {
            String key = matcher.group(1).trim();
            return key.isEmpty() ? null : key;
        }
        
        return null;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
            // 正常情况
            "${key}",
            "${userName}",
            "${user_name}",
            "${USER_NAME}",
            "${key123}",
            "${_private}",
            
            // 复杂key格式
            "${user.name}",
            "${user-name}",
            "${config.db.host}",
            "${app-version}",
            
            // 边界情况
            "${}",              // 空key
            "${a}",             // 单字符key
            "${  key  }",       // 包含空格的key
            "${中文键}",         // 中文key
            "${key with spaces}", // 包含空格的key
            
            // 不匹配的情况
            null,               // null输入
            "",                 // 空字符串
            "key",              // 没有${}包围
            "{key}",            // 缺少$
            "$key}",            // 缺少{
            "${key",            // 缺少}
            "$key",             // 完全错误格式
            "prefix${key}",     // 有前缀
            "${key}suffix",     // 有后缀
            "prefix${key}suffix", // 前后都有
            "${{key}}",         // 双重括号
            "${key}}",          // 多余的}
            "${{key}",          // 多余的{
        };
        
        System.out.println("=== 方法1：基础正则表达式 ===");
        for (String testCase : testCases) {
            String result = extractKeyWithRegex(testCase);
            System.out.printf("输入: %-20s => 输出: %s%n", 
                testCase == null ? "null" : "\"" + testCase + "\"", 
                result == null ? "null" : "\"" + result + "\"");
        }
        
        System.out.println("\n=== 方法2：字符串操作 ===");
        for (String testCase : testCases) {
            String result = extractKeyWithStringOps(testCase);
            System.out.printf("输入: %-20s => 输出: %s%n", 
                testCase == null ? "null" : "\"" + testCase + "\"", 
                result == null ? "null" : "\"" + result + "\"");
        }
        
        System.out.println("\n=== 方法3：严格模式 ===");
        for (String testCase : testCases) {
            String result = extractKeyStrict(testCase);
            System.out.printf("输入: %-20s => 输出: %s%n", 
                testCase == null ? "null" : "\"" + testCase + "\"", 
                result == null ? "null" : "\"" + result + "\"");
        }
        
        System.out.println("\n=== 方法4：复杂格式支持 ===");
        for (String testCase : testCases) {
            String result = extractKeyComplex(testCase);
            System.out.printf("输入: %-20s => 输出: %s%n", 
                testCase == null ? "null" : "\"" + testCase + "\"", 
                result == null ? "null" : "\"" + result + "\"");
        }
        
        System.out.println("\n=== 方法5：最宽松模式 ===");
        for (String testCase : testCases) {
            String result = extractKeyPermissive(testCase);
            System.out.printf("输入: %-20s => 输出: %s%n", 
                testCase == null ? "null" : "\"" + testCase + "\"", 
                result == null ? "null" : "\"" + result + "\"");
        }
        
        // 性能测试
        System.out.println("\n=== 性能对比测试 ===");
        String testInput = "${testKey}";
        int iterations = 1000000;
        
        // 测试方法1
        long start1 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            extractKeyWithRegex(testInput);
        }
        long end1 = System.nanoTime();
        
        // 测试方法2
        long start2 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            extractKeyWithStringOps(testInput);
        }
        long end2 = System.nanoTime();
        
        System.out.printf("正则表达式方法: %.2f ms%n", (end1 - start1) / 1_000_000.0);
        System.out.printf("字符串操作方法: %.2f ms%n", (end2 - start2) / 1_000_000.0);
        System.out.printf("字符串操作快 %.1f 倍%n", (double)(end1 - start1) / (end2 - start2));
    }
}