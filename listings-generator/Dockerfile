FROM openjdk:8-jre

CMD ["/usr/bin/java", "-jar", "/usr/share/generator/generator.jar"]

ADD target/lib           /usr/share/generator/lib
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/generator/generator.jar