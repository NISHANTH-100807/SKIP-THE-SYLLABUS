package com.skipthesyllabus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SkipTheSyllabusApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkipTheSyllabusApplication.class, args);
    }
}
