package de.dicecup.classlink;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI caseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Classlink Grade Management Service")
                        .description("Foobar bla bla")
                        .version("0.0.2^")
                        .contact(new Contact().name("Dicecup").email("support@dicecup.com"))
                        .license(new License().name("Proprietary"))
                        .termsOfService("tos bla blie blub")
                        );
    }
}
