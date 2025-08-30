import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Parser to convert various string formats to JSON
 * Supports inheritance via nested objects (superString pattern)
 */
public class StringToJsonParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Main parsing method that handles all supported formats
     */
    public static String parseToJson(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            return "{}";
        }
        
        ObjectNode result = parseStringToObject(input.trim());
        return objectMapper.writeValueAsString(result);
    }
    
    /**
     * Parse string to ObjectNode
     */
    private static ObjectNode parseStringToObject(String input) throws Exception {
        ObjectNode result = objectMapper.createObjectNode();
        
        // Remove class name and extract content
        String content = extractContent(input);
        if (content.isEmpty()) {
            return result;
        }
        
        // Parse the content
        parseContent(content, result);
        
        return result;
    }
    
    /**
     * Extract content from class declaration
     */
    private static String extractContent(String input) {
        input = input.trim();
        
        // Find the first { or ( after class name
        int openIndex = -1;
        char openChar = 0;
        char closeChar = 0;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '{') {
                openIndex = i;
                openChar = '{';
                closeChar = '}';
                break;
            } else if (c == '(') {
                openIndex = i;
                openChar = '(';
                closeChar = ')';
                break;
            }
        }
        
        if (openIndex == -1) {
            return "";
        }
        
        // Find matching closing bracket
        int depth = 0;
        int closeIndex = -1;
        for (int i = openIndex; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == openChar) {
                depth++;
            } else if (c == closeChar) {
                depth--;
                if (depth == 0) {
                    closeIndex = i;
                    break;
                }
            }
        }
        
        if (closeIndex == -1) {
            return "";
        }
        
        return input.substring(openIndex + 1, closeIndex);
    }
    
    /**
     * Parse content and populate the result object
     */
    private static void parseContent(String content, ObjectNode result) throws Exception {
        if (content.trim().isEmpty()) {
            return;
        }
        
        // Handle leading comma
        content = content.trim();
        if (content.startsWith(",")) {
            content = content.substring(1).trim();
        }
        
        int i = 0;
        while (i < content.length()) {
            // Skip whitespace
            while (i < content.length() && Character.isWhitespace(content.charAt(i))) {
                i++;
            }
            
            if (i >= content.length()) break;
            
            // Skip commas
            if (content.charAt(i) == ',') {
                i++;
                continue;
            }
            
            // Check if this is a nested object (class name followed by { or ()
            int classNameEnd = findEndOfClassName(content, i);
            if (classNameEnd > i) {
                String potentialClassName = content.substring(i, classNameEnd).trim();
                
                // Skip whitespace after class name
                int j = classNameEnd;
                while (j < content.length() && Character.isWhitespace(content.charAt(j))) {
                    j++;
                }
                
                if (j < content.length() && (content.charAt(j) == '{' || content.charAt(j) == '(' || content.charAt(j) == '[')) {
                    // This is a nested object - find its end
                    int objEnd = findMatchingBracket(content, j);
                    if (objEnd != -1) {
                        String nestedObjStr = content.substring(i, objEnd + 1);
                        ObjectNode nestedObj = parseStringToObject(nestedObjStr);
                        mergeObjects(result, nestedObj);
                        i = objEnd + 1;
                        continue;
                    }
                }
            }
            
            // Parse field=value or field:value
            int separatorPos = findNextSeparator(content, i);
            if (separatorPos == -1) {
                // No separator found, skip to next comma
                i = findNextCommaOrEnd(content, i);
                continue;
            }
            
            String fieldName = content.substring(i, separatorPos).trim();
            i = separatorPos + 1;
            
            // Skip whitespace after separator
            while (i < content.length() && Character.isWhitespace(content.charAt(i))) {
                i++;
            }
            
            if (i >= content.length()) {
                // Empty value
                result.set(fieldName, parseSimpleValue(""));
                break;
            }
            
            // Parse field value
            String fieldValue;
            int valueEnd;
            
            char firstChar = content.charAt(i);
            if (firstChar == '[') {
                // Array or special bracket notation
                valueEnd = findMatchingBracket(content, i);
                fieldValue = content.substring(i, valueEnd + 1);
            } else if (firstChar == '{') {
                // Object
                valueEnd = findMatchingBracket(content, i);
                fieldValue = content.substring(i, valueEnd + 1);
            } else {
                // Check if this is a nested object with class name
                int classEnd = findEndOfClassName(content, i);
                if (classEnd > i) {
                    int j = classEnd;
                    while (j < content.length() && Character.isWhitespace(content.charAt(j))) {
                        j++;
                    }
                    if (j < content.length() && (content.charAt(j) == '{' || content.charAt(j) == '(' || content.charAt(j) == '[')) {
                        valueEnd = findMatchingBracket(content, j);
                        fieldValue = content.substring(i, valueEnd + 1);
                    } else {
                        // Simple value
                        valueEnd = findNextCommaOrEnd(content, i);
                        fieldValue = content.substring(i, valueEnd).trim();
                    }
                } else {
                    // Simple value
                    valueEnd = findNextCommaOrEnd(content, i);
                    fieldValue = content.substring(i, valueEnd).trim();
                }
            }
            
            addFieldToObject(result, fieldName, fieldValue);
            i = valueEnd + 1;
        }
    }
    
    /**
     * Find end of class name
     */
    private static int findEndOfClassName(String content, int start) {
        int i = start;
        
        // First character must be letter
        if (i >= content.length() || !Character.isLetter(content.charAt(i))) {
            return start;
        }
        
        // Continue while alphanumeric or underscore
        while (i < content.length() && 
               (Character.isLetterOrDigit(content.charAt(i)) || content.charAt(i) == '_')) {
            i++;
        }
        
        return i;
    }
    
    /**
     * Find next separator (= or :) not inside brackets
     */
    private static int findNextSeparator(String content, int start) {
        int depth = 0;
        boolean inString = false;
        char stringDelim = 0;
        
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (!inString && (c == '"' || c == '\'')) {
                inString = true;
                stringDelim = c;
            } else if (inString && c == stringDelim) {
                inString = false;
            } else if (!inString) {
                if (c == '{' || c == '[' || c == '(') {
                    depth++;
                } else if (c == '}' || c == ']' || c == ')') {
                    depth--;
                } else if ((c == '=' || c == ':') && depth == 0) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Find next comma or end of string not inside brackets
     */
    private static int findNextCommaOrEnd(String content, int start) {
        int depth = 0;
        boolean inString = false;
        char stringDelim = 0;
        
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (!inString && (c == '"' || c == '\'')) {
                inString = true;
                stringDelim = c;
            } else if (inString && c == stringDelim) {
                inString = false;
            } else if (!inString) {
                if (c == '{' || c == '[' || c == '(') {
                    depth++;
                } else if (c == '}' || c == ']' || c == ')') {
                    depth--;
                } else if (c == ',' && depth == 0) {
                    return i;
                }
            }
        }
        
        return content.length();
    }
    
    /**
     * Find matching bracket
     */
    private static int findMatchingBracket(String content, int start) {
        if (start >= content.length()) return -1;
        
        char openChar = content.charAt(start);
        char closeChar;
        
        switch (openChar) {
            case '{': closeChar = '}'; break;
            case '[': closeChar = ']'; break;
            case '(': closeChar = ')'; break;
            default: return -1;
        }
        
        int depth = 1;
        boolean inString = false;
        char stringDelim = 0;
        
        for (int i = start + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (!inString && (c == '"' || c == '\'')) {
                inString = true;
                stringDelim = c;
            } else if (inString && c == stringDelim) {
                inString = false;
            } else if (!inString) {
                if (c == openChar) {
                    depth++;
                } else if (c == closeChar) {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Add field to JSON object
     */
    private static void addFieldToObject(ObjectNode obj, String fieldName, String fieldValue) throws Exception {
        fieldValue = fieldValue.trim();
        
        if (fieldValue.equals("null")) {
            obj.putNull(fieldName);
        } else if (fieldValue.startsWith("[") && fieldValue.endsWith("]")) {
            // Always treat [] as array first, but handle special object notation
            ArrayNode arrayNode = parseArray(fieldValue);
            obj.set(fieldName, arrayNode);
        } else if (fieldValue.startsWith("{") && fieldValue.endsWith("}")) {
            // Object literal
            ObjectNode nestedObj = parseStringToObject(fieldValue);
            obj.set(fieldName, nestedObj);
        } else if (isClassNameWithBrackets(fieldValue)) {
            // Special case: ClassName [field=value] should be treated as object
            if (fieldValue.contains("[") && fieldValue.contains("=")) {
                // Extract the content inside []
                int bracketStart = fieldValue.indexOf('[');
                int bracketEnd = fieldValue.lastIndexOf(']');
                if (bracketStart != -1 && bracketEnd != -1) {
                    String objContent = fieldValue.substring(bracketStart + 1, bracketEnd);
                    ObjectNode objNode = objectMapper.createObjectNode();
                    parseContent(objContent, objNode);
                    obj.set(fieldName, objNode);
                } else {
                    // Regular nested object
                    ObjectNode nestedObj = parseStringToObject(fieldValue);
                    obj.set(fieldName, nestedObj);
                }
            } else {
                // Regular nested object with class name
                ObjectNode nestedObj = parseStringToObject(fieldValue);
                obj.set(fieldName, nestedObj);
            }
        } else {
            // Simple value
            obj.set(fieldName, parseSimpleValue(fieldValue));
        }
    }
    
    /**
     * Check if string is a class name followed by brackets
     */
    private static boolean isClassNameWithBrackets(String str) {
        str = str.trim();
        int spaceOrBracket = -1;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{' || c == '(' || c == '[' || Character.isWhitespace(c)) {
                spaceOrBracket = i;
                break;
            }
        }
        
        if (spaceOrBracket == -1) return false;
        
        String className = str.substring(0, spaceOrBracket);
        return className.matches("[A-Z][A-Za-z0-9_]*") && 
               (str.contains("{") || str.contains("(") || str.contains("["));
    }
    
    /**
     * Parse array string into ArrayNode
     */
    private static ArrayNode parseArray(String arrayStr) throws Exception {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        
        if (arrayStr.equals("[]")) {
            return arrayNode;
        }
        
        String content = arrayStr.substring(1, arrayStr.length() - 1).trim();
        if (content.isEmpty()) {
            return arrayNode;
        }
        
        List<String> elements = splitArrayElements(content);
        
        for (String element : elements) {
            element = element.trim();
            if (element.equals("null")) {
                arrayNode.addNull();
            } else if (isClassNameWithBrackets(element)) {
                ObjectNode nestedObj = parseStringToObject(element);
                arrayNode.add(nestedObj);
            } else if (element.startsWith("{") && element.endsWith("}")) {
                ObjectNode nestedObj = parseStringToObject(element);
                arrayNode.add(nestedObj);
            } else {
                arrayNode.add(parseSimpleValue(element));
            }
        }
        
        return arrayNode;
    }
    
    /**
     * Split array elements respecting nested structures
     */
    private static List<String> splitArrayElements(String content) {
        List<String> elements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        char stringDelim = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (!inString && (c == '"' || c == '\'')) {
                inString = true;
                stringDelim = c;
                current.append(c);
            } else if (inString && c == stringDelim) {
                inString = false;
                current.append(c);
            } else if (inString) {
                current.append(c);
            } else if (c == '{' || c == '[' || c == '(') {
                depth++;
                current.append(c);
            } else if (c == '}' || c == ']' || c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                if (current.length() > 0) {
                    elements.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            elements.add(current.toString().trim());
        }
        
        return elements;
    }
    
    /**
     * Parse simple value (string, number, boolean)
     */
    private static JsonNode parseSimpleValue(String value) {
        value = value.trim();
        
        // Handle empty string
        if (value.isEmpty()) {
            return objectMapper.valueToTree("");
        }
        
        // Handle boolean
        if (value.equals("true") || value.equals("false")) {
            return objectMapper.valueToTree(Boolean.parseBoolean(value));
        }
        
        // Handle number
        try {
            if (value.contains(".")) {
                return objectMapper.valueToTree(Double.parseDouble(value));
            } else {
                return objectMapper.valueToTree(Long.parseLong(value));
            }
        } catch (NumberFormatException e) {
            // Not a number, treat as string
        }
        
        // Handle string (remove quotes if present)
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        
        return objectMapper.valueToTree(value);
    }
    
    /**
     * Merge two ObjectNodes
     */
    private static void mergeObjects(ObjectNode target, ObjectNode source) {
        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            target.set(field.getKey(), field.getValue());
        }
    }
    
    /**
     * Test method with the provided sample
     */
    public static void main(String[] args) {
        try {
            String input = "OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, imeiCode=*,invoiceVOs=[Invoice{                carrierCode=GGGG-SERVICE,                 invoiceType=61,                 invoiceTitle=***,                 vat=VatInfo{                isInvoicePayer=null,                 industry=***,                 province=null,                 city=null},                 vatInvoiceIndia=null,                 delivery=DeliveryInfo{                zipCode=*,                 updateTime=null},                 invoceExtParams={}}], voucher=VoucherVo {usingVoucher:false}, recycleInfo=RecycleInfo [recycleType=null, recycleAppCode=null], custInfoKey=, McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}";
            
            String result = parseToJson(input);
            System.out.println("Parsed JSON:");
            System.out.println(result);
            
            // Pretty print
            ObjectMapper prettyMapper = new ObjectMapper();
            Object json = prettyMapper.readValue(result, Object.class);
            String prettyJson = prettyMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("\nPretty printed:");
            System.out.println(prettyJson);
            
            // Test other formats
            testOtherFormats();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Test other supported formats
     */
    private static void testOtherFormats() throws Exception {
        System.out.println("\n=== Testing Other Formats ===");
        
        // Format 2: ClassName(,field1=val1,field2=val2)
        String test2 = "TestClass(,field1=value1,field2=123,field3=true)";
        System.out.println("Format 2: " + parseToJson(test2));
        
        // Format 3: ClassName {field1=val1,field2=val2}
        String test3 = "TestClass {field1=value1,field2=123,field3=true}";
        System.out.println("Format 3: " + parseToJson(test3));
        
        // Format 4: ClassName (field1=val1,SuperClass{field4=val4})
        String test4 = "TestClass (field1=value1,SuperClass{field4=value4,field5=456})";
        System.out.println("Format 4: " + parseToJson(test4));
        
        // Format 5: ClassName {field1:val1}
        String test5 = "TestClass {field1:value1,field2:123}";
        System.out.println("Format 5: " + parseToJson(test5));
    }
}