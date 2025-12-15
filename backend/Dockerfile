FROM gradle:8.4-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon

FROM amazoncorretto:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]