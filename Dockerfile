FROM openjdk:8-jre-alpine

RUN mkdir /red
WORKDIR /red

COPY ./target/scala-2.12/killmailscraper.jar killmailscraper.jar

ADD killmailscraper.sv.conf /etc/supervisor/conf.d/

RUN apt-get update && apt-get -y -q install supervisor

CMD ["/usr/bin/supervisord"]
