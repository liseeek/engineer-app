FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM amazoncorretto:17
RUN yum install -y curl && yum clean all
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar application.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "application.jar"]