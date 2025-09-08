import java.util.*;
import java.util.regex.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 解析复杂对象字符串为JSON的工具类
 * 支持多种格式：
 * 1. 类名{field1=val1,...,${superString}}
 * 2. 类名(,field1=val1,...)
 * 3. 类名 {field1=val1,...}
 * 4. 类名 (field1=val1,...,${superString})
 * 5. 类名 {field1:val1}
 * 6. 类名 [field1=val1] 和 类名[field1=val1]
 */
public class ObjectStringParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析对象字符串为JSON字符串
     */
    public static String parseToJson(String input) throws JsonProcessingException {
        if (input == null || input.trim().isEmpty()) {
            return "{}";
        }
        
        Map<String, Object> result = parseToMap(input.trim());
        return objectMapper.writeValueAsString(result);
    }
    
    /**
     * 解析对象字符串为Map
     */
    public static Map<String, Object> parseToMap(String input) {
        return parseToMap(input, 0);
    }
    
    /**
     * 解析对象字符串为Map（带递归深度限制）
     */
    private static Map<String, Object> parseToMap(String input, int depth) {
        if (input == null || input.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        // 防止无限递归
        if (depth > 50) {
            System.err.println("Warning: Maximum recursion depth reached, returning empty map for: " + 
                             (input.length() > 100 ? input.substring(0, 100) + "..." : input));
            return new HashMap<>();
        }
        
        input = input.trim();
        
        // 找到类名和内容部分
        ParsedObject parsed = parseClassAndContent(input);
        
        // 解析字段内容
        Map<String, Object> result = new HashMap<>();
        if (!parsed.content.isEmpty()) {
            parseFields(parsed.content, result, depth + 1);
        }
        
        return result;
    }
    
    /**
     * 解析类名和内容
     */
    private static ParsedObject parseClassAndContent(String input) {
        // 匹配不同的格式模式，使用非贪婪匹配避免栈溢出
        String[] patterns = {
            "^([\\w.]+)\\s*\\{(.*)\\}$",  // 类名{...}
            "^([\\w.]+)\\s*\\((.*)\\)$",  // 类名(...)
            "^([\\w.]+)\\s*\\[(.*)\\]$"   // 类名[...]
        };
        
        for (String patternStr : patterns) {
            try {
                Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    String className = matcher.group(1);
                    String content = matcher.group(2);
                    return new ParsedObject(className, content);
                }
            } catch (Exception e) {
                // 如果正则表达式出错，继续尝试下一个模式
                continue;
            }
        }
        
        // 如果没有匹配到，返回空内容
        return new ParsedObject("", input);
    }
    
    /**
     * 解析字段内容
     */
    private static void parseFields(String content, Map<String, Object> result, int depth) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        content = content.trim();
        
        // 处理开头的逗号
        if (content.startsWith(",")) {
            content = content.substring(1).trim();
        }
        
        List<String> tokens = tokenize(content);
        
        for (String token : tokens) {
            if (token.trim().isEmpty()) continue;
            
            // 检查是否是嵌套对象（包含类名的字符串）
            if (isNestedObject(token)) {
                Map<String, Object> nestedMap = parseToMap(token, depth);
                result.putAll(nestedMap); // 将嵌套对象的字段展开到当前层级
            } else {
                // 解析单个字段
                parseField(token, result, depth);
            }
        }
    }
    
    /**
     * 词法分析，将内容分割为字段tokens
     */
    private static List<String> tokenize(String content) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuotes = false;
        char quoteChar = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            // 处理引号
            if ((c == '"' || c == '\'') && !inQuotes) {
                inQuotes = true;
                quoteChar = c;
                current.append(c);
            } else if (c == quoteChar && inQuotes) {
                inQuotes = false;
                current.append(c);
            } else if (inQuotes) {
                current.append(c);
            } else {
                // 不在引号内
                if (c == '{' || c == '(' || c == '[') {
                    depth++;
                    current.append(c);
                } else if (c == '}' || c == ')' || c == ']') {
                    depth--;
                    current.append(c);
                } else if (c == ',' && depth == 0) {
                    // 顶层逗号，分割token
                    String token = current.toString().trim();
                    if (!token.isEmpty()) {
                        tokens.add(token);
                    }
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        
        // 添加最后一个token
        String token = current.toString().trim();
        if (!token.isEmpty()) {
            tokens.add(token);
        }
        
        return tokens;
    }
    
    /**
     * 检查是否是嵌套对象（包含类名）
     */
    private static boolean isNestedObject(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        token = token.trim();
        
        // 简单检查：是否包含类名和括号结构
        // 避免复杂的正则表达式导致栈溢出
        boolean hasClassName = false;
        boolean hasBrackets = false;
        
        // 检查是否有类名（字母开头）
        if (token.length() > 0 && Character.isLetter(token.charAt(0))) {
            int i = 0;
            while (i < token.length() && (Character.isLetterOrDigit(token.charAt(i)) || token.charAt(i) == '.')) {
                i++;
            }
            if (i > 0) {
                hasClassName = true;
                // 跳过空格
                while (i < token.length() && Character.isWhitespace(token.charAt(i))) {
                    i++;
                }
                // 检查是否有括号
                if (i < token.length() && (token.charAt(i) == '{' || token.charAt(i) == '(' || token.charAt(i) == '[')) {
                    hasBrackets = true;
                }
            }
        }
        
        return hasClassName && hasBrackets;
    }
    
    /**
     * 解析单个字段
     */
    private static void parseField(String fieldStr, Map<String, Object> result, int depth) {
        fieldStr = fieldStr.trim();
        if (fieldStr.isEmpty()) return;
        
        // 查找分隔符 = 或 :
        int separatorIndex = findSeparator(fieldStr);
        if (separatorIndex == -1) return;
        
        String key = fieldStr.substring(0, separatorIndex).trim();
        String valueStr = fieldStr.substring(separatorIndex + 1).trim();
        
        Object value = parseValue(valueStr, depth);
        result.put(key, value);
    }
    
    /**
     * 查找字段分隔符（= 或 :）
     */
    private static int findSeparator(String fieldStr) {
        int depth = 0;
        boolean inQuotes = false;
        char quoteChar = 0;
        
        for (int i = 0; i < fieldStr.length(); i++) {
            char c = fieldStr.charAt(i);
            
            if ((c == '"' || c == '\'') && !inQuotes) {
                inQuotes = true;
                quoteChar = c;
            } else if (c == quoteChar && inQuotes) {
                inQuotes = false;
            } else if (!inQuotes) {
                if (c == '{' || c == '(' || c == '[') {
                    depth++;
                } else if (c == '}' || c == ')' || c == ']') {
                    depth--;
                } else if ((c == '=' || c == ':') && depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * 解析值
     */
    private static Object parseValue(String valueStr, int depth) {
        if (valueStr == null) return null;
        valueStr = valueStr.trim();
        
        if (valueStr.isEmpty()) {
            return "";
        }
        
        // null值
        if ("null".equals(valueStr)) {
            return null;
        }
        
        // 布尔值
        if ("true".equals(valueStr)) {
            return true;
        }
        if ("false".equals(valueStr)) {
            return false;
        }
        
        // 数组
        if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
            return parseArray(valueStr.substring(1, valueStr.length() - 1), depth);
        }
        
        // 嵌套对象
        if (isNestedObject(valueStr)) {
            return parseToMap(valueStr, depth);
        }
        
        // 简单对象 {key:value, key:value}
        if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
            Map<String, Object> map = new HashMap<>();
            parseFields(valueStr.substring(1, valueStr.length() - 1), map, depth);
            return map;
        }
        
        // 数字
        try {
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            } else {
                return Long.parseLong(valueStr);
            }
        } catch (NumberFormatException e) {
            // 不是数字，继续处理
        }
        
        // 移除引号
        if ((valueStr.startsWith("\"") && valueStr.endsWith("\"")) ||
            (valueStr.startsWith("'") && valueStr.endsWith("'"))) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        
        // 字符串值
        return valueStr;
    }
    
    /**
     * 解析数组
     */
    private static List<Object> parseArray(String arrayContent, int depth) {
        List<Object> result = new ArrayList<>();
        if (arrayContent.trim().isEmpty()) {
            return result;
        }
        
        List<String> elements = tokenize(arrayContent);
        for (String element : elements) {
            result.add(parseValue(element.trim(), depth));
        }
        
        return result;
    }
    
    /**
     * 解析结果类
     */
    private static class ParsedObject {
        final String className;
        final String content;
        
        ParsedObject(String className, String content) {
            this.className = className;
            this.content = content;
        }
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        try {
            // 测试样例1
            String input1 = "OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, imeiCode=*,invoiceVOs=[Invoice{                carrierCode=GGGG-SERVICE,                 invoiceType=61,                 invoiceTitle=***,                 vat=VatInfo{                isInvoicePayer=null,                 industry=***,                 province=null,                 city=null},                 vatInvoiceIndia=null,                 delivery=DeliveryInfo{                zipCode=*,                 updateTime=null},                 invoceExtParams={}}], voucher=VoucherVo {usingVoucher:false}, recycleInfo=RecycleInfo [recycleType=null, recycleAppCode=null], custInfoKey=, McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}";
            
            System.out.println("输入1:");
            System.out.println(input1);
            System.out.println("\n输出1:");
            System.out.println(parseToJson(input1));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}