# String to JSON Parser

A robust Java implementation that converts various string formats to JSON, with support for nested object inheritance and complex recursive structures.

## ✅ Supported Formats

1. **Format 1**: `类名{field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString}}`
2. **Format 2**: `类名(,field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....)`
3. **Format 3**: `类名 {field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....}`
4. **Format 4**: `类名 (field1=val1,field2=[obj1,obj2],field3=[1,2],field4=null....,${superString})`
5. **Format 5**: `类名 {field1:val1}`

## ✅ Enhanced Features

- **Complex Recursive Structures**: Handles deeply nested objects with recursive inheritance
- **Multiple Bracket Types**: Supports `{}`, `()`, and `[]` notations
- **Mixed Separators**: Handles both `=` and `:` field separators
- **Special Array Notation**: Correctly parses `ClassName [field=value]` as objects
- **Parentheses Objects**: Supports `ClassName(field=value)` syntax
- **Stack Overflow Protection**: Built-in recursion depth limiting

## ✅ Verified Test Cases

All samples successfully parsed and validated:

- **Sample 1**: OrderReq with ComOrderReq inheritance (871 chars → 644 chars JSON)
- **Sample 2**: OrderReq with CardInfo parentheses notation (926 chars → 700 chars JSON)  
- **Sample 3**: CreateOrderReq with complex recursive structure (4384 chars → 3412 chars JSON)

## Usage

```java
// Basic usage
String input = "OrderReq{cpsId=999,isCpsCreate=false,ComOrderReq{portal=2}}";
String json = StringToJsonParser.parseToJson(input);
// Result: {"cpsId":999,"isCpsCreate":false,"portal":2}

// Complex recursive structure
String complex = "CreateOrderReq{...itemProp=CreateOrderReq{...}...}";
String result = StringToJsonParser.parseToJson(complex);
```

## Building and Testing

```bash
# Compile
mvn compile

# Run comprehensive tests
mvn exec:java -Dexec.mainClass="StringToJsonParser"

# Run additional format tests  
mvn exec:java -Dexec.mainClass="StringToJsonParserTest"
```

## Dependencies

- Jackson Databind 2.15.2
- Java 11+