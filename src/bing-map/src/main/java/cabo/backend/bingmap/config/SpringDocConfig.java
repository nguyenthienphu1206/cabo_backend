package cabo.backend.bingmap.config;

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
    public OpenAPI bingmapOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bing Map")
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
    public GroupedOpenApi bingmapApi() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomiser(openApi -> openApi.info(bingmapOpenAPI().getInfo()))
                .group("Bing-Map")
                .pathsToMatch("/api/v1/bing-map/**")
                .build();
    }
}
