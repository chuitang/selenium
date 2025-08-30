import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class demonstrating all supported string formats
 */
public class StringToJsonParserTest {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        try {
            System.out.println("=== String to JSON Parser Test ===\n");
            
            // Test Format 1: 类名{field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString}}
            testFormat1();
            
            // Test Format 2: 类名(,field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....)
            testFormat2();
            
            // Test Format 3: 类名 {field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....}
            testFormat3();
            
            // Test Format 4: 类名 (field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString})
            testFormat4();
            
            // Test Format 5: 类名 {field1:val1}
            testFormat5();
            
            // Test the original complex sample
            testComplexSample();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void testFormat1() throws Exception {
        System.out.println("=== Format 1: ClassName{field1=val1,...,SuperClass{...}} ===");
        String input = "OrderRequest{orderId=12345,items=[Item{id=1,name=book}],total=99.99,SuperClass{userId=67890,sessionId=abc123}}";
        String result = StringToJsonParser.parseToJson(input);
        printResult(input, result);
    }
    
    private static void testFormat2() throws Exception {
        System.out.println("=== Format 2: ClassName(,field1=val1,field2=val2) ===");
        String input = "UserRequest(,username=john_doe,age=25,isActive=true,roles=[admin,user])";
        String result = StringToJsonParser.parseToJson(input);
        printResult(input, result);
    }
    
    private static void testFormat3() throws Exception {
        System.out.println("=== Format 3: ClassName {field1=val1,field2=val2} ===");
        String input = "ProductInfo {productId=P001,name=Laptop,price=1299.99,specs={cpu=Intel,ram=16GB}}";
        String result = StringToJsonParser.parseToJson(input);
        printResult(input, result);
    }
    
    private static void testFormat4() throws Exception {
        System.out.println("=== Format 4: ClassName (field1=val1,SuperClass{...}) ===");
        String input = "RequestContext (requestId=REQ001,timestamp=1234567890,BaseContext{appId=APP123,version=2.0})";
        String result = StringToJsonParser.parseToJson(input);
        printResult(input, result);
    }
    
    private static void testFormat5() throws Exception {
        System.out.println("=== Format 5: ClassName {field1:val1} ===");
        String input = "ConfigObject {host:localhost,port:8080,enabled:true,tags:[web,api]}";
        String result = StringToJsonParser.parseToJson(input);
        printResult(input, result);
    }
    
    private static void testComplexSample() throws Exception {
        System.out.println("=== Complex Sample from Requirements ===");
        String input = "OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, imeiCode=*,invoiceVOs=[Invoice{                carrierCode=GGGG-SERVICE,                 invoiceType=61,                 invoiceTitle=***,                 vat=VatInfo{                isInvoicePayer=null,                 industry=***,                 province=null,                 city=null},                 vatInvoiceIndia=null,                 delivery=DeliveryInfo{                zipCode=*,                 updateTime=null},                 invoceExtParams={}}], voucher=VoucherVo {usingVoucher:false}, recycleInfo=RecycleInfo [recycleType=null, recycleAppCode=null], custInfoKey=, McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}";
        String result = StringToJsonParser.parseToJson(input);
        printResult("Complex Sample", result);
    }
    
    private static void printResult(String input, String result) throws Exception {
        System.out.println("Input: " + (input.length() > 100 ? input.substring(0, 100) + "..." : input));
        
        // Pretty print the result
        Object json = objectMapper.readValue(result, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        System.out.println("Output JSON:");
        System.out.println(prettyJson);
        System.out.println();
    }
}