# 使用OpenJDK 11作为基础镜像（推荐版本）
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制Maven构建结果
COPY target/jolt-transformation-example-1.0.0.jar app.jar

# 复制规则文件
COPY src/main/resources/ /app/resources/

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/jolt/health || exit 1