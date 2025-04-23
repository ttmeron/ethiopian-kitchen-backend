package com.resturant;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EntityScan("com.resturant.entity")
@OpenAPIDefinition(info = @Info(title = "Ethiopian Kitchen API", version = "v1", description = "API for Ethiopian Kitchen"))

public class EthiopianKitchenApp {

    public static void main(String[] args) {
        SpringApplication.run(EthiopianKitchenApp.class, args);
    }



    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Ethiopian Kitchen API")
                        .version("1.0")
                        .description("API for Ethiopian Kitchen Restaurant"));
    }
}
