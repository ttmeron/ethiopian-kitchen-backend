package com.resturant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication
@EntityScan("com.resturant.entity")
public class EthiopianKitchenApp {

    public static void main(String[] args) {
        SpringApplication.run(EthiopianKitchenApp.class, args);
    }
}
