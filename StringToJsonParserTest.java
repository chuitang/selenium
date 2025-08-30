import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringToJsonParserTest {
    
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }
    
    @Test
    public void testFormat1WithSuperString() throws Exception {
        // Format 1: 类名{field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString}}
        String input = "User{id=123,name=\"John Doe\",roles=[\"admin\",\"user\"],age=30,active=true,${Person{firstName=\"Jane\",lastName=\"Doe\"}}}";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("User", result.get("className").asText());
        assertEquals(123, result.get("id").asInt());
        assertEquals("John Doe", result.get("name").asText());
        assertEquals(30, result.get("age").asInt());
        assertTrue(result.get("active").asBoolean());
        
        // Check array
        JsonNode roles = result.get("roles");
        assertTrue(roles.isArray());
        assertEquals(2, roles.size());
        assertEquals("admin", roles.get(0).asText());
        assertEquals("user", roles.get(1).asText());
        
        // Check merged fields from superString
        assertEquals("Jane", result.get("firstName").asText());
        assertEquals("Doe", result.get("lastName").asText());
    }
    
    @Test
    public void testFormat2WithComma() throws Exception {
        // Format 2: 类名(,field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....)
        String input = "Product(,id=456,title=\"Gaming Laptop\",price=1299.99,categories=[\"electronics\",\"computers\",\"gaming\"],inStock=true,rating=null)";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("Product", result.get("className").asText());
        assertEquals(456, result.get("id").asInt());
        assertEquals("Gaming Laptop", result.get("title").asText());
        assertEquals(1299.99, result.get("price").asDouble(), 0.01);
        assertTrue(result.get("inStock").asBoolean());
        assertTrue(result.get("rating").isNull());
        
        // Check categories array
        JsonNode categories = result.get("categories");
        assertTrue(categories.isArray());
        assertEquals(3, categories.size());
        assertEquals("electronics", categories.get(0).asText());
        assertEquals("computers", categories.get(1).asText());
        assertEquals("gaming", categories.get(2).asText());
    }
    
    @Test
    public void testFormat3WithSpace() throws Exception {
        // Format 3: 类名 {field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....}
        String input = "Order {orderId=789,customerName=\"Alice Johnson\",items=[\"laptop\",\"mouse\",\"keyboard\"],totalAmount=1450.75,orderDate=\"2024-01-15\",shipped=false}";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("Order", result.get("className").asText());
        assertEquals(789, result.get("orderId").asInt());
        assertEquals("Alice Johnson", result.get("customerName").asText());
        assertEquals(1450.75, result.get("totalAmount").asDouble(), 0.01);
        assertEquals("2024-01-15", result.get("orderDate").asText());
        assertFalse(result.get("shipped").asBoolean());
        
        // Check items array
        JsonNode items = result.get("items");
        assertTrue(items.isArray());
        assertEquals(3, items.size());
        assertEquals("laptop", items.get(0).asText());
        assertEquals("mouse", items.get(1).asText());
        assertEquals("keyboard", items.get(2).asText());
    }
    
    @Test
    public void testFormat4WithSpaceAndSuperString() throws Exception {
        // Format 4: 类名 (field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString})
        String input = "Customer (customerId=101,email=\"customer@example.com\",preferences=[\"email\",\"sms\"],vip=true,${ContactInfo{phone=\"555-1234\",address=\"123 Main St\",city=\"Boston\"}})";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("Customer", result.get("className").asText());
        assertEquals(101, result.get("customerId").asInt());
        assertEquals("customer@example.com", result.get("email").asText());
        assertTrue(result.get("vip").asBoolean());
        
        // Check preferences array
        JsonNode preferences = result.get("preferences");
        assertTrue(preferences.isArray());
        assertEquals(2, preferences.size());
        assertEquals("email", preferences.get(0).asText());
        assertEquals("sms", preferences.get(1).asText());
        
        // Check merged fields from superString
        assertEquals("555-1234", result.get("phone").asText());
        assertEquals("123 Main St", result.get("address").asText());
        assertEquals("Boston", result.get("city").asText());
    }
    
    @Test
    public void testNestedSuperString() throws Exception {
        // Test nested ${superString} parsing
        String input = "Employee{empId=200,name=\"Bob\",${Department{deptId=10,deptName=\"Engineering\",${Location{building=\"A\",floor=3}}}}}";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("Employee", result.get("className").asText());
        assertEquals(200, result.get("empId").asInt());
        assertEquals("Bob", result.get("name").asText());
        assertEquals(10, result.get("deptId").asInt());
        assertEquals("Engineering", result.get("deptName").asText());
        assertEquals("A", result.get("building").asText());
        assertEquals(3, result.get("floor").asInt());
    }
    
    @Test
    public void testEmptyFields() throws Exception {
        String input = "EmptyClass{}";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("EmptyClass", result.get("className").asText());
        assertEquals(1, result.size()); // Only className field
    }
    
    @Test
    public void testNumberArrays() throws Exception {
        String input = "Numbers{integers=[1,2,3,4,5],decimals=[1.1,2.2,3.3],mixed=[1,2.5,3]}";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("Numbers", result.get("className").asText());
        
        JsonNode integers = result.get("integers");
        assertTrue(integers.isArray());
        assertEquals(5, integers.size());
        assertEquals(1, integers.get(0).asInt());
        assertEquals(5, integers.get(4).asInt());
        
        JsonNode decimals = result.get("decimals");
        assertTrue(decimals.isArray());
        assertEquals(3, decimals.size());
        assertEquals(1.1, decimals.get(0).asDouble(), 0.01);
        assertEquals(3.3, decimals.get(2).asDouble(), 0.01);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() throws Exception {
        String input = "InvalidFormat[field=value]"; // Square brackets not supported as main brackets
        StringToJsonParser.parseToJson(input);
    }
    
    @Test
    public void testSpecialCharactersInStrings() throws Exception {
        String input = "Special{message=\"Hello, World! @#$%^&*()_+-={}[]|\\:;'<>?,./ \",emoji=\"😀🎉\"}";
        
        JsonNode result = StringToJsonParser.parseToJson(input);
        
        assertEquals("Special", result.get("className").asText());
        assertEquals("Hello, World! @#$%^&*()_+-={}[]|\\:;'<>?,./ ", result.get("message").asText());
        assertEquals("😀🎉", result.get("emoji").asText());
    }
}