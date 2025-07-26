FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY target/qiyu-live-user-provider-docker.jar /app/app.jar
# 还可以增加一些JVM参数配置
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
