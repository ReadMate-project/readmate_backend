package com.readmate.ReadMate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi jwtApi(){
        return GroupedOpenApi.builder()
                .group("jwt-api")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("ReadMate API")
                        .description("ReadMate API 입니다")
                        .version("v1.0.0"));
    }

}
