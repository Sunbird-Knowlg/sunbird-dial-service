FROM sunbird/openjdk-java11-alpine:latest
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && mkdir -p /home/sunbird
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
COPY ./target/sunbird-dial-service-1.0-SNAPSHOT-dist.zip /home/sunbird/
RUN unzip /home/sunbird/sunbird-dial-service-1.0-SNAPSHOT-dist.zip -d /home/sunbird/
RUN rm /home/sunbird/sunbird-dial-service-1.0-SNAPSHOT-dist.zip
WORKDIR /home/sunbird/
CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -cp '/home/sunbird/sunbird-dial-service-1.0-SNAPSHOT/lib/*' -Dconfig.file=/home/sunbird/sunbird-dial-service-1.0-SNAPSHOT/config/application.conf -Dlogger.file=/home/sunbird/sunbird-dial-service-1.0-SNAPSHOT/config/logback.xml play.core.server.ProdServerStart /home/sunbird/sunbird-dial-service-1.0-SNAPSHOT