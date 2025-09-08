import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 演示ObjectStringParser的功能，测试所有样例输入
 */
public class ObjectStringParserDemo {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        System.out.println("=== ObjectStringParser 演示程序 ===\n");
        
        // 测试样例1
        testSample("样例1", 
            "OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, imeiCode=*,invoiceVOs=[Invoice{                carrierCode=GGGG-SERVICE,                 invoiceType=61,                 invoiceTitle=***,                 vat=VatInfo{                isInvoicePayer=null,                 industry=***,                 province=null,                 city=null},                 vatInvoiceIndia=null,                 delivery=DeliveryInfo{                zipCode=*,                 updateTime=null},                 invoceExtParams={}}], voucher=VoucherVo {usingVoucher:false}, recycleInfo=RecycleInfo [recycleType=null, recycleAppCode=null], custInfoKey=, McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}"
        );
        
        // 测试样例2
        testSample("样例2", 
            "OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, imeiCode=*,invoiceVOs=[Invoice{                carrierCode=GGGG-SERVICE,                 invoiceType=61,                 invoiceTitle=***,                 vat=VatInfo{                isInvoicePayer=null,                 industry=***,                 province=null,                 city=null},                 vatInvoiceIndia=null,                 delivery=DeliveryInfo{                zipCode=*,                 updateTime=null},                 invoceExtParams={}}], voucher=VoucherVo {usingVoucher:false}, recycleInfo=RecycleInfo [recycleType=null, recycleAppCode=null], custInfoKey=, cardList=[CardInfo(activityCode=AAA, packageCode=BBB)],McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}"
        );
        
        // 测试样例3
        testSample("样例3", 
            "CreateOrderReq{,cpsId=258288,cpsWi=null,isCpsCreate=false,messageCode=null,loginName=null,seqNo=null,BuildOrderReq{orderItemReqArgs=[OrderItemArg{itemId=500801002343157, prdShowType=null, itemType=S0, qty=1, mainSkuCode=, gifts=null, itemProp=CreateOrderReq{,cpsId=258288,seqNo=null,BuildOrderReq{orderItemReqArgs=[OrderItemArg{itemId=500801002343157, subOrderItemReqArgs=null}], carrierInvoiceVOs=[CarrierInvoice{                taxOffice=***}], orderSubPortal=2.1, carInfo=CarInfo(licenseType=1, carStoreInfoList=[CarStoreInfo{storeName:用户中心•深圳光明}, CarStoreInfo{storeId:CNSCN322941}], carContactInfoList=[CarContactInfo{userName:魏**, mobile:136****2196, idType:, contactType:5}], carRightsInfoList=[CarRightsInfo(activityCode=AR5SJ60CT9X1DVGP6Q28, rightsPackageCode=RP5SJ60CT9XPHTG35829, rightsCode=R5SJ60C4RX1D4B30O17)]), recycleReserveCode=, vSceneActivityCode=, timeConstraintLogistics=null, serviceId=, serviceName=,McpRequestBase{portal=2, version=null, lang=zh_CN, country=CN, euid=***}}}, subOrderItemReqArgs=null}]}}"
        );
        
        // 测试简单格式
        testSample("冒号分隔格式", "TestClass {field1:value1, field2:123, field3:true}");
        testSample("方括号格式", "TestClass [field1=value1, field2=456, field3=false]");
        testSample("圆括号格式", "TestClass(field1=value1, field2=789, field3=null)");
        
        // 测试复杂嵌套
        testSample("复杂嵌套", 
            "OuterClass{field1=value1, MiddleClass{middleField=middleValue, InnerClass{innerField=innerValue}}, field2=value2}"
        );
        
        System.out.println("=== 演示完成 ===");
    }
    
    private static void testSample(String name, String input) {
        System.out.println("--- " + name + " ---");
        System.out.println("输入: " + (input.length() > 100 ? input.substring(0, 100) + "..." : input));
        
        try {
            String json = ObjectStringParser.parseToJson(input);
            System.out.println("输出JSON: " + formatJson(json));
            System.out.println("字段数量: " + ObjectStringParser.parseToMap(input).size());
        } catch (Exception e) {
            System.out.println("解析错误: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static String formatJson(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return json; // 如果格式化失败，返回原始JSON
        }
    }
}