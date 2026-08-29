FROM eclipse-temurin:21-jdk-alpine

RUN addgroup -S -g 10001 runner && adduser -S -D -H -u 10001 -G runner runner

WORKDIR /workspace
USER 10001:10001
