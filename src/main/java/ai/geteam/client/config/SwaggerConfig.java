package ai.geteam.client.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String KEYCLOAK_SECURITY_SCHEME="keycloakAuth";
    @Value("${swagger-config.token-url}")
    private  String tokenURL;
    @Value("${swagger-config.authorization-url}")
    private String authorizationURL;
	@Bean
    public GroupedOpenApi controllerApi() {
        return GroupedOpenApi.builder()
                .group("controller-api")
                .packagesToScan("ai.geteam.client.controller")
                .build();
    }

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(KEYCLOAK_SECURITY_SCHEME))
                .components(
                        new Components()
                                .addSecuritySchemes(KEYCLOAK_SECURITY_SCHEME,securityScheme())
                );
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(KEYCLOAK_SECURITY_SCHEME)
                .type(SecurityScheme.Type.OAUTH2)
                .description("Service used to authenticate access for rest APIs")
                .flows(new OAuthFlows()
                        .password(new OAuthFlow()
                                .tokenUrl(tokenURL)
                                .authorizationUrl(authorizationURL)
                        )
                );
    }

}
