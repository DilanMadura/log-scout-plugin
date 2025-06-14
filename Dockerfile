FROM openjdk:21-jdk
ADD target/log-scout-plugin.jar log-scout-plugin.jar
ENTRYPOINT ["java","-jar","/log-scout-plugin.jar"]