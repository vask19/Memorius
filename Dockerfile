FROM openjdk:17-jdk-slim

LABEL description="memorius-redis-like-db"
LABEL provider="vask19"

RUN mkdir -p /opt/memorius
WORKDIR /opt/memorius

COPY target/memorius-1.0.0.jar /opt/memorius/memorius.jar
RUN chmod 755 /opt/memorius/memorius.jar

EXPOSE 6379

RUN chown -R 185:185 /opt/memorius
USER 185

ENTRYPOINT ["java", "-jar", "/opt/memorius/memorius.jar"]
