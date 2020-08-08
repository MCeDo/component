package com.example.component;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class ComponentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComponentApplication.class, args);
    }

}
