import org.junit.Test;
import static org.junit.Assert.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * 测试ObjectStringParser的各种输入格式
 */
public class ObjectStringParserTest {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testSample1() throws Exception {
        String input = "OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, imeiCode=*,invoiceVOs=[Invoice{                carrierCode=GGGG-SERVICE,                 invoiceType=61,                 invoiceTitle=***,                 vat=VatInfo{                isInvoicePayer=null,                 industry=***,                 province=null,                 city=null},                 vatInvoiceIndia=null,                 delivery=DeliveryInfo{                zipCode=*,                 updateTime=null},                 invoceExtParams={}}], voucher=VoucherVo {usingVoucher:false}, recycleInfo=RecycleInfo [recycleType=null, recycleAppCode=null], custInfoKey=, McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}";
        
        String expected = "{\"cpsId\":999,\"isCpsCreate\":false,\"messageCode\":null,\"seqNo\":\"\",\"itemReqArgs\":[{\"itemId\":213548,\"mainSkuCode\":\"\",\"itemProp\":{},\"subOrderItemReqArgs\":null}],\"paymentType\":\"n***\",\"couponList\":[],\"salePortal\":3,\"imeiCode\":\"*\",\"invoiceVOs\":[{\"carrierCode\":\"GGGG-SERVICE\",\"invoiceType\":61,\"invoiceTitle\":\"***\",\"vat\":{\"isInvoicePayer\":null,\"industry\":\"***\",\"province\":null,\"city\":null},\"vatInvoiceIndia\":null,\"delivery\":{\"zipCode\":\"*\",\"updateTime\":null},\"invoceExtParams\":{}}],\"voucher\":{\"usingVoucher\":false},\"recycleInfo\":{\"recycleType\":null,\"recycleAppCode\":null},\"custInfoKey\":\"\",\"portal\":2,\"version\":1,\"lang\":\"zh-CN\",\"country\":\"CN\",\"eueid\":\"***\"}";
        
        String result = ObjectStringParser.parseToJson(input);
        
        // 将两个JSON字符串转换为Map进行比较，避免字段顺序问题
        Map<String, Object> expectedMap = objectMapper.readValue(expected, Map.class);
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        
        System.out.println("Sample 1 Input:");
        System.out.println(input);
        System.out.println("\nSample 1 Result:");
        System.out.println(result);
        System.out.println("\nSample 1 Expected:");
        System.out.println(expected);
        
        assertEquals(expectedMap, resultMap);
    }
    
    @Test
    public void testSample2() throws Exception {
        String input = "OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, imeiCode=*,invoiceVOs=[Invoice{                carrierCode=GGGG-SERVICE,                 invoiceType=61,                 invoiceTitle=***,                 vat=VatInfo{                isInvoicePayer=null,                 industry=***,                 province=null,                 city=null},                 vatInvoiceIndia=null,                 delivery=DeliveryInfo{                zipCode=*,                 updateTime=null},                 invoceExtParams={}}], voucher=VoucherVo {usingVoucher:false}, recycleInfo=RecycleInfo [recycleType=null, recycleAppCode=null], custInfoKey=, cardList=[CardInfo(activityCode=AAA, packageCode=BBB)],McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}";
        
        String expected = "{\"cpsId\":999,\"isCpsCreate\":false,\"messageCode\":null,\"seqNo\":\"\",\"itemReqArgs\":[{\"itemId\":213548,\"mainSkuCode\":\"\",\"itemProp\":{},\"subOrderItemReqArgs\":null}],\"paymentType\":\"n***\",\"couponList\":[],\"salePortal\":3,\"imeiCode\":\"*\",\"invoiceVOs\":[{\"carrierCode\":\"GGGG-SERVICE\",\"invoiceType\":61,\"invoiceTitle\":\"***\",\"vat\":{\"isInvoicePayer\":null,\"industry\":\"***\",\"province\":null,\"city\":null},\"vatInvoiceIndia\":null,\"delivery\":{\"zipCode\":\"*\",\"updateTime\":null},\"invoceExtParams\":{}}],\"voucher\":{\"usingVoucher\":false},\"recycleInfo\":{\"recycleType\":null,\"recycleAppCode\":null},\"custInfoKey\":\"\",\"cardList\":[{\"activityCode\":\"AAA\",\"packageCode\":\"BBB\"}],\"portal\":2,\"version\":1,\"lang\":\"zh-CN\",\"country\":\"CN\",\"eueid\":\"***\"}";
        
        String result = ObjectStringParser.parseToJson(input);
        
        Map<String, Object> expectedMap = objectMapper.readValue(expected, Map.class);
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        
        System.out.println("\nSample 2 Input:");
        System.out.println(input);
        System.out.println("\nSample 2 Result:");
        System.out.println(result);
        
        assertEquals(expectedMap, resultMap);
    }
    
    @Test
    public void testSimpleFormats() throws Exception {
        // 测试简单格式
        
        // 格式5: 类名 {field1:val1}
        String input5 = "TestClass {field1:value1, field2:123}";
        String result5 = ObjectStringParser.parseToJson(input5);
        System.out.println("\nSimple format test 1:");
        System.out.println("Input: " + input5);
        System.out.println("Result: " + result5);
        
        // 格式6: 类名 [field1=val1]
        String input6 = "TestClass [field1=value1, field2=456]";
        String result6 = ObjectStringParser.parseToJson(input6);
        System.out.println("\nSimple format test 2:");
        System.out.println("Input: " + input6);
        System.out.println("Result: " + result6);
        
        // 格式7: 类名[field1=val1]
        String input7 = "TestClass[field1=value1, field2=789]";
        String result7 = ObjectStringParser.parseToJson(input7);
        System.out.println("\nSimple format test 3:");
        System.out.println("Input: " + input7);
        System.out.println("Result: " + result7);
    }
    
    @Test
    public void testComplexNestedObjects() throws Exception {
        // 测试复杂嵌套对象
        String input = "OuterClass{field1=value1, InnerClass{innerField=innerValue, DeepClass{deepField=deepValue}}, field2=value2}";
        String result = ObjectStringParser.parseToJson(input);
        
        System.out.println("\nComplex nested test:");
        System.out.println("Input: " + input);
        System.out.println("Result: " + result);
        
        // 验证结果包含所有字段
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertTrue(resultMap.containsKey("field1"));
        assertTrue(resultMap.containsKey("innerField"));
        assertTrue(resultMap.containsKey("deepField"));
        assertTrue(resultMap.containsKey("field2"));
    }
    
    public static void main(String[] args) {
        ObjectStringParserTest test = new ObjectStringParserTest();
        try {
            System.out.println("Running tests...\n");
            test.testSample1();
            test.testSample2();
            test.testSimpleFormats();
            test.testComplexNestedObjects();
            System.out.println("\nAll tests completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}