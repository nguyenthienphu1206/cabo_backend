package cabo.backend.vehicle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringDocConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI driverOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle")
                        .description("Spring Boot REST API Documentation")
                        .version("1")
                        .contact(new Contact()
                                .name("Nguyen Thien Phu")
                                .email("ntphu1206@gmail.com")
                        )
                        .license(new License().name("License of API"))
                );
    }

    @Bean
    public GroupedOpenApi driverApi() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomiser(openApi -> openApi.info(driverOpenAPI().getInfo()))
                .group("Vehicle")
                .packagesToScan("cabo.backend.vehicle.controller")
                .build();
    }
}
