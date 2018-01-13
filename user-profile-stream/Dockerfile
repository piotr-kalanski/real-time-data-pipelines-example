FROM openjdk:8-jre

CMD ["/usr/bin/java", "-jar", "/usr/share/stream/stream.jar"]

ADD target/lib           /usr/share/stream/lib
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/stream/stream.jar