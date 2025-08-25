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
RUN unzip /home/sunbird/sunbird-dial-service-1.0-SNAPSHOT-dist.zip -d /home/sunbird/
RUN rm /home/sunbird/sunbird-dial-service-1.0-SNAPSHOT-dist.zip
WORKDIR /home/sunbird/
CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -cp '/home/sunbird/sunbird-dial-service-1.0-SNAPSHOT/lib/*' -Dconfig.file=/home/sunbird/sunbird-dial-service-1.0-SNAPSHOT/config/application.conf -Dlogger.file=/home/sunbird/sunbird-dial-service-1.0-SNAPSHOT/config/logback.xml play.core.server.ProdServerStart /home/sunbird/sunbird-dial-service-1.0-SNAPSHOT