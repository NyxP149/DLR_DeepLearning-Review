FROM node:22-alpine

RUN npm install --global typescript@5.9.2 \
    && addgroup -S -g 10001 runner \
    && adduser -S -D -H -u 10001 -G runner runner

WORKDIR /workspace
USER 10001:10001
