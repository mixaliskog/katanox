## Prerequisites
To build and run this project, ensure the following prerequisites are installed:
* Java 11
* Maven
* Docker

## Build and Run Instructions

This project requires a database and RabbitMQ service to run. You can start these services using Docker Compose:

`docker-compose up -d`

This command will start the necessary containers for the database and RabbitMQ services.

After starting the services, build the project and generate the necessary JOOQ classes by running:

`mvn clean install`

This command will compile the project, run tests, and generate the required JOOQ classes based on the database schema.
Once the project is successfully built, start the Spring Boot application:

We can start the application by running:
`mvn spring-boot:run`

The application will be running on localhost at port 8080.