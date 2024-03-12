FROM openjdk:17-jdk-slim
ADD ./target/demojdbc-1.0.0-executable.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
EXPOSE 8080