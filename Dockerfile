FROM openjdk:8

RUN mkdir /red
WORKDIR /red

COPY ./target/scala-2.12/killmailscraper.jar killmailscraper.jar

ADD killmailscraper.sv.conf /etc/supervisor/conf.d/

CMD ["/usr/bin/supervisord"]