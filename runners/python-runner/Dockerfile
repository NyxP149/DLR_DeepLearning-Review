FROM python:3.13-alpine

RUN addgroup -S -g 10001 runner && adduser -S -D -H -u 10001 -G runner runner

ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
WORKDIR /workspace
USER 10001:10001
