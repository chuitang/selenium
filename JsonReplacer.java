import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * JSON替换工具类
 * 将JSON字符串中的"xxx":null或"xxx":[]替换为"xxx":(null|[])
 */
public class JsonReplacer {
    
    /**
     * 替换JSON字符串中的null值和空数组
     * @param jsonString 原始JSON字符串
     * @return 替换后的JSON字符串
     */
    public static String replaceNullAndEmptyArray(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        
        // 正则表达式匹配 "key":null 模式
        Pattern nullPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*null");
        Matcher nullMatcher = nullPattern.matcher(jsonString);
        String result = nullMatcher.replaceAll("\"$1\":\\(null\\)");
        
        // 正则表达式匹配 "key":[] 模式
        Pattern emptyArrayPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\[\\s*\\]");
        Matcher emptyArrayMatcher = emptyArrayPattern.matcher(result);
                result = emptyArrayMatcher.replaceAll("\"$1\":\\(\\[\\]\\)");
        
        return result;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试用例1：包含null值
        String json1 = "{\"name\":\"John\", \"age\":null, \"city\":\"New York\"}";
        System.out.println("原始: " + json1);
        System.out.println("替换后: " + replaceNullAndEmptyArray(json1));
        System.out.println();
        
        // 测试用例2：包含空数组
        String json2 = "{\"name\":\"Jane\", \"hobbies\":[], \"skills\":[\"Java\", \"Python\"]}";
        System.out.println("原始: " + json2);
        System.out.println("替换后: " + replaceNullAndEmptyArray(json2));
        System.out.println();
        
        // 测试用例3：同时包含null和空数组
        String json3 = "{\"id\":1, \"name\":null, \"tags\":[], \"description\":\"test\"}";
        System.out.println("原始: " + json3);
        System.out.println("替换后: " + replaceNullAndEmptyArray(json3));
        System.out.println();
        
        // 测试用例4：复杂嵌套JSON
        String json4 = "{\"user\":{\"profile\":null, \"preferences\":[]}, \"data\":[{\"value\":null, \"items\":[]}]}";
        System.out.println("原始: " + json4);
        System.out.println("替换后: " + replaceNullAndEmptyArray(json4));
        System.out.println();
        
        // 测试用例5：包含空格的情况
        String json5 = "{\"field1\" : null , \"field2\" : [ ] }";
        System.out.println("原始: " + json5);
        System.out.println("替换后: " + replaceNullAndEmptyArray(json5));
    }
}