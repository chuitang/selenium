# JOLT参数替换转换服务

基于JOLT库的JSON参数替换转换服务。

## 快速使用

```java
JoltTransformationService service = JoltTransformationService.fromFile("jolt-transformation-rules.json");
String result = service.transform(inputJson);
```

## 构建运行

```bash
mvn clean compile test
mvn exec:java -Dexec.mainClass="com.example.jolt.JoltTransformationExample"
```