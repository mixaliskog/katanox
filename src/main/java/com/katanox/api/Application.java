package com.katanox.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

import java.util.TimeZone;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, R2dbcAutoConfiguration.class})
@PropertySource("classpath:application.properties")
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }
}
