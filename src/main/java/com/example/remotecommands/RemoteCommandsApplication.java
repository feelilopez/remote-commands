package com.example.remotecommands;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemoteCommandsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteCommandsApplication.class, args);
    }
}
