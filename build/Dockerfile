FROM --platform=linux/x86_64 eclipse-temurin:11.0.20.1_1-jdk-focal
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y unzip curl \
    && adduser --uid 1001 --home /home/sunbird/ --disabled-login sunbird \
    && mkdir -p /home/sunbird \
    && chown -R sunbird:sunbird /home/sunbird \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
COPY ./target/sunbird-dial-service-1.0-SNAPSHOT-dist.zip /home/sunbird/