FROM openjdk:8-jdk-alpine
EXPOSE 8080
WORKDIR /app
COPY @project.build.finalName@.jar .
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", \
"-jar", "@project.build.finalName@.jar"]