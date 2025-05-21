package com.donggle;

import org.springframework.boot.SpringApplication;

public class TestDonggleApplication {

    public static void main(String[] args) {
        SpringApplication.from(DonggleApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
