# Stage: Dev (Dùng để code và chạy trực tiếp)
FROM maven:3.9.5-eclipse-temurin-17 AS dev
WORKDIR /app
# Copy pom trước để cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source code (Lệnh này copy ban đầu, nhưng volume trong compose sẽ ghi đè lên để cập nhật code mới)
COPY src ./src
# Lệnh mặc định cho dev
CMD ["mvn", "spring-boot:run"]

# Stage: Build (Dùng để build jar cho production)
FROM dev AS build
RUN mvn clean package -DskipTests

# Stage: Runtime (Production - chạy file jar nhẹ hơn)
FROM eclipse-temurin:17-jre-alpine AS prod
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]