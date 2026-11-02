package com.subhasis.cloudops;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudopsApplication {

    public static void main(String[] args) {
        // Must run before Spring starts, because the DB driver reads the JVM time zone on connect
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(CloudopsApplication.class, args);
    }
}