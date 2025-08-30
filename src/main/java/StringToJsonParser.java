import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Enhanced String to JSON Parser with improved logic
 * Handles complex nested structures, inheritance, and various bracket notations
 */
public class StringToJsonParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_RECURSION_DEPTH = 50;
    
    /**
     * Main parsing method
     */
    public static String parseToJson(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            return "{}";
        }
        
        ObjectNode result = parseObject(input.trim(), 0);
        return objectMapper.writeValueAsString(result);
    }
    
    /**
     * Parse object with recursion depth tracking
     */
    private static ObjectNode parseObject(String input, int depth) throws Exception {
        if (depth > MAX_RECURSION_DEPTH) {
            throw new RuntimeException("Maximum recursion depth exceeded");
        }
        
        ObjectNode result = objectMapper.createObjectNode();
        
        // Extract content from brackets
        String content = extractObjectContent(input);
        if (content.isEmpty()) {
            return result;
        }
        
        // Parse content using improved field extraction
        parseObjectContent(content, result, depth);
        
        return result;
    }
    
    /**
     * Extract content between the outermost brackets
     */
    private static String extractObjectContent(String input) {
        input = input.trim();
        
        // Find first opening bracket after class name
        int openPos = -1;
        char openBracket = 0;
        char closeBracket = 0;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '{') {
                openPos = i;
                openBracket = '{';
                closeBracket = '}';
                break;
            } else if (c == '(') {
                openPos = i;
                openBracket = '(';
                closeBracket = ')';
                break;
            }
        }
        
        if (openPos == -1) {
            return "";
        }
        
        // Find matching closing bracket
        int depth = 0;
        for (int i = openPos; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == openBracket) {
                depth++;
            } else if (c == closeBracket) {
                depth--;
                if (depth == 0) {
                    return input.substring(openPos + 1, i);
                }
            }
        }
        
        return "";
    }
    
    /**
     * Parse object content into fields
     */
    private static void parseObjectContent(String content, ObjectNode result, int depth) throws Exception {
        if (content.trim().isEmpty()) {
            return;
        }
        
        // Handle leading comma
        content = content.trim();
        if (content.startsWith(",")) {
            content = content.substring(1).trim();
        }
        
        // Use a more robust field extraction approach
        List<FieldEntry> fields = extractFields(content);
        
        for (FieldEntry field : fields) {
            if (field.isInheritedObject) {
                // Parse and merge inherited object
                ObjectNode inheritedObj = parseObject(field.value, depth + 1);
                mergeObjects(result, inheritedObj);
            } else {
                // Regular field
                addFieldToObject(result, field.name, field.value, depth);
            }
        }
    }
    
    /**
     * Extract fields from content using improved logic
     */
    private static List<FieldEntry> extractFields(String content) {
        List<FieldEntry> fields = new ArrayList<>();
        
        int pos = 0;
        while (pos < content.length()) {
            // Skip whitespace and commas
            while (pos < content.length() && (Character.isWhitespace(content.charAt(pos)) || content.charAt(pos) == ',')) {
                pos++;
            }
            
            if (pos >= content.length()) break;
            
            // Check for inherited object (ClassName{...} without field name)
            FieldParseResult inheritedResult = tryParseInheritedObject(content, pos);
            if (inheritedResult.found) {
                fields.add(new FieldEntry("", inheritedResult.value, true));
                pos = inheritedResult.nextPos;
                continue;
            }
            
            // Parse regular field
            FieldParseResult fieldResult = parseRegularField(content, pos);
            if (fieldResult.found) {
                fields.add(new FieldEntry(fieldResult.name, fieldResult.value, false));
                pos = fieldResult.nextPos;
            } else {
                pos++; // Skip problematic character
            }
        }
        
        return fields;
    }
    
    /**
     * Try to parse inherited object (ClassName{...})
     */
    private static FieldParseResult tryParseInheritedObject(String content, int pos) {
        // Must start with uppercase letter (class name)
        if (pos >= content.length() || !Character.isUpperCase(content.charAt(pos))) {
            return new FieldParseResult(false, "", "", pos);
        }
        
        // Find end of class name
        int classEnd = pos;
        while (classEnd < content.length() && 
               (Character.isLetterOrDigit(content.charAt(classEnd)) || content.charAt(classEnd) == '_')) {
            classEnd++;
        }
        
        // Skip whitespace
        int bracketPos = classEnd;
        while (bracketPos < content.length() && Character.isWhitespace(content.charAt(bracketPos))) {
            bracketPos++;
        }
        
        // Must be followed by opening bracket
        if (bracketPos >= content.length()) {
            return new FieldParseResult(false, "", "", pos);
        }
        
        char bracket = content.charAt(bracketPos);
        if (bracket != '{' && bracket != '(' && bracket != '[') {
            return new FieldParseResult(false, "", "", pos);
        }
        
        // Find matching bracket
        int endPos = findMatchingBracket(content, bracketPos);
        if (endPos == -1) {
            return new FieldParseResult(false, "", "", pos);
        }
        
        String objStr = content.substring(pos, endPos + 1);
        return new FieldParseResult(true, "", objStr, endPos + 1);
    }
    
    /**
     * Parse regular field (fieldName=value or fieldName:value)
     */
    private static FieldParseResult parseRegularField(String content, int pos) {
        // Find field separator
        int separatorPos = findFieldSeparator(content, pos);
        if (separatorPos == -1) {
            return new FieldParseResult(false, "", "", pos);
        }
        
        String fieldName = content.substring(pos, separatorPos).trim();
        if (fieldName.isEmpty()) {
            return new FieldParseResult(false, "", "", pos);
        }
        
        // Find value
        int valueStart = separatorPos + 1;
        while (valueStart < content.length() && Character.isWhitespace(content.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= content.length()) {
            return new FieldParseResult(true, fieldName, "", content.length());
        }
        
        int valueEnd = findValueEnd(content, valueStart);
        String value = content.substring(valueStart, valueEnd).trim();
        
        return new FieldParseResult(true, fieldName, value, valueEnd);
    }
    
    /**
     * Find field separator (= or :) at top level
     */
    private static int findFieldSeparator(String content, int start) {
        int depth = 0;
        
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{' || c == '[' || c == '(') {
                depth++;
            } else if (c == '}' || c == ']' || c == ')') {
                depth--;
            } else if ((c == '=' || c == ':') && depth == 0) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Find end of field value
     */
    private static int findValueEnd(String content, int start) {
        if (start >= content.length()) {
            return start;
        }
        
        char firstChar = content.charAt(start);
        
        // Handle structured values (arrays, objects)
        if (firstChar == '[' || firstChar == '{' || firstChar == '(') {
            int end = findMatchingBracket(content, start);
            return (end == -1) ? content.length() : end + 1;
        }
        
        // Check for class name followed by brackets
        if (Character.isUpperCase(firstChar)) {
            int classEnd = start;
            while (classEnd < content.length() && 
                   (Character.isLetterOrDigit(content.charAt(classEnd)) || content.charAt(classEnd) == '_')) {
                classEnd++;
            }
            
            // Skip whitespace
            int bracketPos = classEnd;
            while (bracketPos < content.length() && Character.isWhitespace(content.charAt(bracketPos))) {
                bracketPos++;
            }
            
            if (bracketPos < content.length() && 
                (content.charAt(bracketPos) == '{' || content.charAt(bracketPos) == '(' || content.charAt(bracketPos) == '[')) {
                int end = findMatchingBracket(content, bracketPos);
                return (end == -1) ? content.length() : end + 1;
            }
        }
        
        // Simple value - find next comma at top level
        return findNextTopLevelComma(content, start);
    }
    
    /**
     * Find next comma at top level
     */
    private static int findNextTopLevelComma(String content, int start) {
        int depth = 0;
        
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{' || c == '[' || c == '(') {
                depth++;
            } else if (c == '}' || c == ']' || c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                return i;
            }
        }
        
        return content.length();
    }
    
    /**
     * Find matching bracket
     */
    private static int findMatchingBracket(String content, int start) {
        if (start >= content.length()) {
            return -1;
        }
        
        char openChar = content.charAt(start);
        char closeChar;
        
        switch (openChar) {
            case '{': closeChar = '}'; break;
            case '[': closeChar = ']'; break;
            case '(': closeChar = ')'; break;
            default: return -1;
        }
        
        int depth = 1;
        for (int i = start + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == openChar) {
                depth++;
            } else if (c == closeChar) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Add field to object
     */
    private static void addFieldToObject(ObjectNode obj, String fieldName, String fieldValue, int depth) throws Exception {
        fieldValue = fieldValue.trim();
        
        if (fieldValue.equals("null")) {
            obj.putNull(fieldName);
        } else if (fieldValue.isEmpty()) {
            obj.put(fieldName, "");
        } else if (fieldValue.startsWith("[") && fieldValue.endsWith("]")) {
            // Handle array or special bracket notation
            String arrayContent = fieldValue.substring(1, fieldValue.length() - 1).trim();
            
            // Check if this is object notation with [] (like RecycleInfo [field=value])
            // But exclude cases where it's clearly an array like [ClassName(...)]
            boolean isObjectWithBrackets = arrayContent.contains("=") && 
                                         !arrayContent.contains("{") && 
                                         !isClassNameWithBrackets(arrayContent.trim()) &&
                                         countTopLevelCommas(arrayContent) <= 3;
            
            if (isObjectWithBrackets) {
                // This is an object with [] notation
                ObjectNode objNode = objectMapper.createObjectNode();
                parseObjectContent(arrayContent, objNode, depth + 1);
                obj.set(fieldName, objNode);
            } else {
                // Regular array
                ArrayNode arrayNode = parseArray(fieldValue, depth);
                obj.set(fieldName, arrayNode);
            }
        } else if (fieldValue.startsWith("{") && fieldValue.endsWith("}")) {
            // Object literal
            ObjectNode nestedObj = parseObject(fieldValue, depth + 1);
            obj.set(fieldName, nestedObj);
        } else if (isClassNameWithBrackets(fieldValue)) {
            // Object with class name
            ObjectNode nestedObj = parseObject(fieldValue, depth + 1);
            obj.set(fieldName, nestedObj);
        } else {
            // Simple value
            obj.set(fieldName, parseSimpleValue(fieldValue));
        }
    }
    
    /**
     * Parse array
     */
    private static ArrayNode parseArray(String arrayStr, int depth) throws Exception {
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
                ObjectNode nestedObj = parseObject(element, depth + 1);
                arrayNode.add(nestedObj);
            } else if (element.startsWith("{") && element.endsWith("}")) {
                ObjectNode nestedObj = parseObject(element, depth + 1);
                arrayNode.add(nestedObj);
            } else {
                arrayNode.add(parseSimpleValue(element));
            }
        }
        
        return arrayNode;
    }
    
    /**
     * Split array elements properly
     */
    private static List<String> splitArrayElements(String content) {
        List<String> elements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (c == '{' || c == '[' || c == '(') {
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
     * Check if string is class name with brackets
     */
    private static boolean isClassNameWithBrackets(String str) {
        str = str.trim();
        if (str.isEmpty() || !Character.isUpperCase(str.charAt(0))) {
            return false;
        }
        
        int i = 0;
        while (i < str.length() && (Character.isLetterOrDigit(str.charAt(i)) || str.charAt(i) == '_')) {
            i++;
        }
        
        while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
            i++;
        }
        
        return i < str.length() && (str.charAt(i) == '{' || str.charAt(i) == '(' || str.charAt(i) == '[');
    }
    
    /**
     * Count top-level commas
     */
    private static int countTopLevelCommas(String content) {
        int count = 0;
        int depth = 0;
        
        for (char c : content.toCharArray()) {
            if (c == '{' || c == '[' || c == '(') {
                depth++;
            } else if (c == '}' || c == ']' || c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Parse simple value
     */
    private static JsonNode parseSimpleValue(String value) {
        value = value.trim();
        
        if (value.isEmpty()) {
            return objectMapper.valueToTree("");
        }
        
        if (value.equals("true") || value.equals("false")) {
            return objectMapper.valueToTree(Boolean.parseBoolean(value));
        }
        
        try {
            if (value.contains(".")) {
                return objectMapper.valueToTree(Double.parseDouble(value));
            } else {
                return objectMapper.valueToTree(Long.parseLong(value));
            }
        } catch (NumberFormatException e) {
            // Not a number, treat as string
        }
        
        return objectMapper.valueToTree(value);
    }
    
    /**
     * Merge objects
     */
    private static void mergeObjects(ObjectNode target, ObjectNode source) {
        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            target.set(field.getKey(), field.getValue());
        }
    }
    
    /**
     * Field entry class
     */
    private static class FieldEntry {
        String name;
        String value;
        boolean isInheritedObject;
        
        FieldEntry(String name, String value, boolean isInheritedObject) {
            this.name = name;
            this.value = value;
            this.isInheritedObject = isInheritedObject;
        }
    }
    
    /**
     * Field parse result class
     */
    private static class FieldParseResult {
        boolean found;
        String name;
        String value;
        int nextPos;
        
        FieldParseResult(boolean found, String name, String value, int nextPos) {
            this.found = found;
            this.name = name;
            this.value = value;
            this.nextPos = nextPos;
        }
    }
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        try {
            // Test Sample 3
            String sample3 = "CreateOrderReq{,cpsId=258288,cpsWi=null,isCpsCreate=false,messageCode=null,loginName=null,seqNo=null,BuildOrderReq{orderItemReqArgs=[OrderItemArg{itemId=500801002343157, prdShowType=null, itemType=S0, qty=1, mainSkuCode=, gifts=null, itemProp=CreateOrderReq{,cpsId=258288,seqNo=null,BuildOrderReq{orderItemReqArgs=[OrderItemArg{itemId=500801002343157, subOrderItemReqArgs=null}], carrierInvoiceVOs=[CarrierInvoice{                taxOffice=***}], orderSubPortal=2.1, carInfo=CarInfo(licenseType=1, carStoreInfoList=[CarStoreInfo{storeName:用户中心•深圳光明}, CarStoreInfo{storeId:CNSCN322941}], carContactInfoList=[CarContactInfo{userName:魏**, mobile:136****2196, idType:, contactType:5}], carRightsInfoList=[CarRightsInfo(activityCode=AR5SJ60CT9X1DVGP6Q28, rightsPackageCode=RP5SJ60CT9XPHTG35829, rightsCode=R5SJ60C4RX1D4B30O17)]), recycleReserveCode=, vSceneActivityCode=, timeConstraintLogistics=null, serviceId=, serviceName=,McpRequestBase{portal=2, version=null, lang=zh_CN, country=CN, euid=***}}}, subOrderItemReqArgs=null}]}}";
            
            System.out.println("=== Enhanced Parser Test ===");
            System.out.println("Input length: " + sample3.length());
            
            String result = StringToJsonParser.parseToJson(sample3);
            System.out.println("✅ Success! Result length: " + result.length());
            
            // Pretty print result
            ObjectMapper mapper = new ObjectMapper();
            Object jsonObj = mapper.readValue(result, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
            System.out.println("\n" + prettyJson);
            
        } catch (Exception e) {
            System.err.println("❌ Error:");
            e.printStackTrace();
        }
    }
}