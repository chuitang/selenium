import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Parser to convert custom string formats to JSON
 * Supports 4 formats:
 * 1. 类名{field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString}}
 * 2. 类名(,field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....)
 * 3. 类名 {field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....}
 * 4. 类名 (field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString})
 */
public class StringToJsonParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Pattern to match the main structure: className + optional space + bracket type + content
    private static final Pattern MAIN_PATTERN = Pattern.compile(
        "^([^\\s\\{\\(]+)\\s*([\\{\\(])(.*)([\\}\\)])$"
    );
    
    /**
     * Main parsing method
     */
    public static JsonNode parseToJson(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }
        
        return parseStringToJson(input.trim());
    }
    
    private static JsonNode parseStringToJson(String input) throws Exception {
        Matcher mainMatcher = MAIN_PATTERN.matcher(input);
        
        if (!mainMatcher.matches()) {
            throw new IllegalArgumentException("Invalid format: " + input);
        }
        
        String className = mainMatcher.group(1);
        String openBracket = mainMatcher.group(2);
        String content = mainMatcher.group(3);
        String closeBracket = mainMatcher.group(4);
        
        // Validate bracket pairs
        if ((openBracket.equals("{") && !closeBracket.equals("}")) ||
            (openBracket.equals("(") && !closeBracket.equals(")"))) {
            throw new IllegalArgumentException("Mismatched brackets in: " + input);
        }
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("className", className);
        
        // Handle special case for format 2: starts with comma
        if (content.startsWith(",")) {
            content = content.substring(1).trim();
        }
        
        if (!content.isEmpty()) {
            parseFields(content, result);
        }
        
        return result;
    }
    
    private static void parseFields(String content, ObjectNode result) throws Exception {
        List<String> fieldPairs = splitFieldPairs(content);
        
        for (String fieldPair : fieldPairs) {
            fieldPair = fieldPair.trim();
            
            // Check if this is a ${superString}
            if (fieldPair.startsWith("${") && fieldPair.endsWith("}")) {
                String superString = extractSuperString(fieldPair);
                if (superString != null) {
                    JsonNode superJson = parseStringToJson(superString);
                    
                    // Merge all fields from superString into current result
                    if (superJson.isObject()) {
                        ObjectNode superObject = (ObjectNode) superJson;
                        superObject.fields().forEachRemaining(entry -> {
                            if (!"className".equals(entry.getKey())) {
                                result.set(entry.getKey(), entry.getValue());
                            }
                        });
                    }
                    continue;
                }
            }
            
            // Parse regular field=value pairs
            int equalIndex = findTopLevelEquals(fieldPair);
            if (equalIndex == -1) {
                continue; // Skip invalid field pairs
            }
            
            String fieldName = fieldPair.substring(0, equalIndex).trim();
            String fieldValue = fieldPair.substring(equalIndex + 1).trim();
            
            JsonNode valueNode = parseValue(fieldValue);
            result.set(fieldName, valueNode);
        }
    }
    
    private static List<String> splitFieldPairs(String content) {
        List<String> pairs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int bracketDepth = 0;
        int arrayDepth = 0;
        boolean inString = false;
        char prevChar = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (!inString) {
                if (c == '"' && prevChar != '\\') {
                    inString = true;
                } else if (c == '{' || c == '(') {
                    bracketDepth++;
                } else if (c == '}' || c == ')') {
                    bracketDepth--;
                } else if (c == '[') {
                    arrayDepth++;
                } else if (c == ']') {
                    arrayDepth--;
                } else if (c == ',' && bracketDepth == 0 && arrayDepth == 0) {
                    pairs.add(current.toString());
                    current = new StringBuilder();
                    prevChar = c;
                    continue;
                }
            } else {
                if (c == '"' && prevChar != '\\') {
                    inString = false;
                }
            }
            
            current.append(c);
            prevChar = c;
        }
        
        if (current.length() > 0) {
            pairs.add(current.toString());
        }
        
        return pairs;
    }
    
    private static int findTopLevelEquals(String fieldPair) {
        int bracketDepth = 0;
        int arrayDepth = 0;
        boolean inString = false;
        char prevChar = 0;
        
        for (int i = 0; i < fieldPair.length(); i++) {
            char c = fieldPair.charAt(i);
            
            if (!inString) {
                if (c == '"' && prevChar != '\\') {
                    inString = true;
                } else if (c == '{' || c == '(') {
                    bracketDepth++;
                } else if (c == '}' || c == ')') {
                    bracketDepth--;
                } else if (c == '[') {
                    arrayDepth++;
                } else if (c == ']') {
                    arrayDepth--;
                } else if (c == '=' && bracketDepth == 0 && arrayDepth == 0) {
                    return i;
                }
            } else {
                if (c == '"' && prevChar != '\\') {
                    inString = false;
                }
            }
            
            prevChar = c;
        }
        
        return -1;
    }
    
    private static JsonNode parseValue(String value) throws Exception {
        value = value.trim();
        
        if (value.equals("null")) {
            return objectMapper.nullNode();
        }
        
        // Handle arrays
        if (value.startsWith("[") && value.endsWith("]")) {
            return parseArray(value);
        }
        
        // Handle nested objects
        if ((value.startsWith("{") && value.endsWith("}")) || 
            (value.startsWith("(") && value.endsWith(")"))) {
            // This might be a nested object in the custom format
            try {
                return parseStringToJson(value);
            } catch (Exception e) {
                // If parsing as custom format fails, treat as string
                return objectMapper.valueToTree(value);
            }
        }
        
        // Handle strings (quoted)
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return objectMapper.valueToTree(value.substring(1, value.length() - 1));
        }
        
        // Handle numbers
        try {
            if (value.contains(".")) {
                return objectMapper.valueToTree(Double.parseDouble(value));
            } else {
                return objectMapper.valueToTree(Long.parseLong(value));
            }
        } catch (NumberFormatException e) {
            // Not a number, treat as string
        }
        
        // Handle booleans
        if (value.equals("true") || value.equals("false")) {
            return objectMapper.valueToTree(Boolean.parseBoolean(value));
        }
        
        // Default to string
        return objectMapper.valueToTree(value);
    }
    
    private static JsonNode parseArray(String arrayStr) throws Exception {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        
        // Remove brackets
        String content = arrayStr.substring(1, arrayStr.length() - 1).trim();
        
        if (content.isEmpty()) {
            return arrayNode;
        }
        
        List<String> elements = splitArrayElements(content);
        
        for (String element : elements) {
            JsonNode elementNode = parseValue(element.trim());
            arrayNode.add(elementNode);
        }
        
        return arrayNode;
    }
    
    private static List<String> splitArrayElements(String content) {
        List<String> elements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int bracketDepth = 0;
        int arrayDepth = 0;
        boolean inString = false;
        char prevChar = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (!inString) {
                if (c == '"' && prevChar != '\\') {
                    inString = true;
                } else if (c == '{' || c == '(') {
                    bracketDepth++;
                } else if (c == '}' || c == ')') {
                    bracketDepth--;
                } else if (c == '[') {
                    arrayDepth++;
                } else if (c == ']') {
                    arrayDepth--;
                } else if (c == ',' && bracketDepth == 0 && arrayDepth == 0) {
                    elements.add(current.toString());
                    current = new StringBuilder();
                    prevChar = c;
                    continue;
                }
            } else {
                if (c == '"' && prevChar != '\\') {
                    inString = false;
                }
            }
            
            current.append(c);
            prevChar = c;
        }
        
        if (current.length() > 0) {
            elements.add(current.toString());
        }
        
        return elements;
    }
    
    /**
     * Extract the content inside ${...} handling nested braces properly
     */
    private static String extractSuperString(String fieldPair) {
        if (!fieldPair.startsWith("${") || !fieldPair.endsWith("}")) {
            return null;
        }
        
        // Remove ${ and the final }
        String content = fieldPair.substring(2, fieldPair.length() - 1);
        return content;
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        try {
            // Test cases for all 4 formats
            String[] testCases = {
                // Format 1
                "User{id=123,name=\"John\",roles=[\"admin\",\"user\"],age=30,active=true,${Person{firstName=\"Jane\",lastName=\"Doe\"}}}",
                
                // Format 2  
                "Product(,id=456,title=\"Laptop\",price=999.99,categories=[\"electronics\",\"computers\"],available=true)",
                
                // Format 3
                "Order {orderId=789,items=[\"item1\",\"item2\"],total=150.50,status=\"pending\"}",
                
                // Format 4
                "Customer (customerId=101,email=\"test@example.com\",preferences=[\"email\",\"sms\"],${Address{street=\"123 Main St\",city=\"Boston\"}})"
            };
            
            for (int i = 0; i < testCases.length; i++) {
                System.out.println("=== Test Case " + (i + 1) + " ===");
                System.out.println("Input: " + testCases[i]);
                
                try {
                    JsonNode result = parseToJson(testCases[i]);
                    System.out.println("Output: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}