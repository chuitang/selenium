# ObjectStringParser

一个用Java实现的复杂对象字符串解析器，能够将多种格式的对象字符串转换为JSON格式。

## 功能特性

该解析器支持以下格式的字符串解析：

1. `类名{field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null,...,${superString}}`
2. `类名(,field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....)`
3. `类名 {field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....}`
4. `类名 (field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString})`
5. `类名 {field1:val1}`
6. `类名 [field1=val1]` 和 `类名[field1=val1]`

## 核心特性

- **嵌套对象支持**: 自动识别和解析嵌套的类对象
- **继承字段展开**: 将`${superString}`中的字段展开到当前层级
- **多种数据类型**: 支持字符串、数字、布尔值、null、数组和对象
- **递归深度保护**: 防止无限递归，最大递归深度50层
- **容错处理**: 对格式错误进行优雅处理

## 使用方法

### Maven依赖

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

### 基本用法

```java
import ObjectStringParser;

// 解析为JSON字符串
String input = "OrderReq{cpsId=999,isCpsCreate=false,messageCode=null}";
String json = ObjectStringParser.parseToJson(input);
System.out.println(json);

// 解析为Map对象
Map<String, Object> result = ObjectStringParser.parseToMap(input);
```

### 示例

#### 输入
```
OrderReq{,cpsId=999,isCpsCreate=false,messageCode=null,seqNo=,ComOrderReq{itemReqArgs=[ItemArg{itemId=213548, mainSkuCode=, itemProp={}, subOrderItemReqArgs=null}], paymentType=n***, couponList=[], salePortal=3, McpRequestBase{portal=2, version=1, lang=zh-CN, country=CN, eueid=***}}}
```

#### 输出
```json
{
  "cpsId": 999,
  "isCpsCreate": false,
  "messageCode": null,
  "seqNo": "",
  "itemReqArgs": [
    {
      "itemId": 213548,
      "mainSkuCode": "",
      "itemProp": {},
      "subOrderItemReqArgs": null
    }
  ],
  "paymentType": "n***",
  "couponList": [],
  "salePortal": 3,
  "portal": 2,
  "version": 1,
  "lang": "zh-CN",
  "country": "CN",
  "eueid": "***"
}
```

## 运行测试

```bash
# 编译项目
mvn compile

# 运行基本测试
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" ObjectStringParser

# 运行演示程序
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" ObjectStringParserDemo

# 运行单元测试
java -cp "target/classes:target/test-classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" ObjectStringParserTest
```

## 实现原理

1. **词法分析**: 使用状态机进行字符串分词，正确处理嵌套括号和引号
2. **语法分析**: 识别类名和字段格式，支持多种分隔符（=, :）
3. **递归解析**: 自动识别嵌套对象并递归解析
4. **类型推断**: 自动识别数据类型（数字、布尔值、null、数组、对象）
5. **字段展开**: 将继承类的字段合并到当前对象中

## 注意事项

- 支持Java 8及以上版本
- 使用Jackson库进行JSON序列化
- 最大递归深度为50层，防止栈溢出
- 对于格式错误的输入会进行容错处理

## 许可证

本项目使用MIT许可证。