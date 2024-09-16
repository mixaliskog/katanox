FROM gcr.io/distroless/java17:latest

COPY ./target/api-impl-1.0-SNAPSHOT.jar  /usr/app/api.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/usr/app/api.jar"]

