package com.lab.sample.config;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI labSampleOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Laboratuvar Numune Yönetim Sistemi API")
                .description("Müşteri, test kataloğu, numune kabul/işleme ve sonuç yönetimi için REST API.")
                .version("v1")
                .contact(new Contact().name("Lab Sample")));
    }
}
