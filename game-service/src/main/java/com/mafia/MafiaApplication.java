package com.mafia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MafiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MafiaApplication.class, args);
    }
}