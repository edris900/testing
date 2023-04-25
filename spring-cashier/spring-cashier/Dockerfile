FROM openjdk:11
EXPOSE 9090
ADD ./target/spring-cashier-1.0.jar /srv/spring-cashier-1.0.jar
CMD java -jar /srv/spring-cashier-1.0.jar

ARG REDIS_PASSWORD
ENV REDIS_PASSWORD=${REDIS_PASSWORD}