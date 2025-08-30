# String to JSON Parser

A Java implementation that converts various string formats to JSON, with support for nested object inheritance.

## Supported Formats

1. **Format 1**: `类名{field1=val1,...,${superString}}`
2. **Format 2**: `类名(,field1=val1,...)`  
3. **Format 3**: `类名 {field1=val1,...}`
4. **Format 4**: `类名 (field1=val1,...,${superString})`
5. **Format 5**: `类名 {field1:val1}`

## Usage

```java
String input = "OrderReq{cpsId=999,isCpsCreate=false}";
String json = StringToJsonParser.parseToJson(input);
```

## Building

```bash
mvn compile
mvn exec:java -Dexec.mainClass="StringToJsonParser"
```