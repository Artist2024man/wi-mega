FROM eclipse-temurin:21
MAINTAINER wuin

ENV PARAMS=""
ENV JAVA_OPTS="-Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"

ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD target/wi-mega.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java -jar $JAVA_OPTS /app/app.jar $PARAMS"]