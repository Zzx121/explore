FROM openjdk:8-jdk-alpine
WORKDIR /usr/local/eurekaServer/
COPY @project.build.finalName@.jar .
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "@project.build.finalName@.jar"]