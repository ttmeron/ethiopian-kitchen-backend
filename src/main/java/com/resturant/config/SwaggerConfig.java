package com.resturant.config;


import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class SwaggerConfig {

//    @Bean
//    public OpenAPI ethiopianKitchenOpenAPI(){
//        return new OpenAPI()
//                .info(new Info().title("Ethiopian Kitchen API")
//                .description("Comprehensive API for managing Ethiopian food items, ingredients, and orders")
//                .version("1.0")
//                .contact(new Contact()
//                        .name("Ethiopian Kitchen Support")
//                        .email("support@ethiopiankitchen.com")))
//                .addServersItem(new Server()
//                        .url("http://localhost:8080")
//                        .description("Local Development Server"))
//                .addServersItem(new Server()
//                        .url("https://api.ethiopiankitchen.com")
//                        .description("Production Server"));
//
//    }
}


