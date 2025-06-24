# syntax=docker/dockerfile:1.2

FROM ubuntu:latest AS build

RUN apt-get update && apt-get install -y openjdk-17-jdk maven

RUN --mount=type=secret,id=credentials_json,dst=/etc/secrets/credentials.json \
    --mount=type=secret,id=firebase_service_account_json,dst=/etc/secrets/firebase-service-account.json \
    --mount=type=secret,id=homologacao_564808_concentraPayDesenvolvimento_p12,dst=/etc/secrets/homologacao-564808-concentraPayDesenvolvimento.p12 \
    mkdir /secrets && \
    cp /etc/secrets/* /secrets/

COPY . .

RUN mvn clean install

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /target/concentrapay-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
