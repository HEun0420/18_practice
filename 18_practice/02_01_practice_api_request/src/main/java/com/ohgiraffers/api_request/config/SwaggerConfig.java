package com.ohgiraffers.api_request.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){

        return new OpenAPI()
                .components(new Components())
                .info(swaggerInfo());
    }

    private Info swaggerInfo(){
        return new Info()
                .title("ohgiraffers API")
                .description("외부 API 요청 테스트")
                .version("1.0.0");
//        http://localhost:8080/swagger-ui/index.html#/
    }
}
