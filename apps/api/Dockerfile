FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace
COPY apps/api/pom.xml apps/api/pom.xml
COPY apps/api/src apps/api/src
COPY content content

WORKDIR /workspace/apps/api
RUN mvn --batch-mode --no-transfer-progress -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S -g 10001 dlr \
    && adduser -S -D -H -u 10001 -G dlr dlr

WORKDIR /app
COPY --from=build /workspace/apps/api/target/dlr-api-*.jar /app/dlr-api.jar

USER 10001:10001
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/dlr-api.jar"]
