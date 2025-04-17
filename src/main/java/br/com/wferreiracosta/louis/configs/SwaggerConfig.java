package br.com.wferreiracosta.louis.configs;

import br.com.wferreiracosta.louis.configs.properties.ApplicationProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final ApplicationProperties property;

    @Bean
    public OpenAPI info() {
        final var contact = new Contact()
                .name(property.contactName())
                .url(property.contactSite());

        final var license = new License()
                .name(property.license())
                .url(property.licenseUrl());

        final var info = new Info()
                .title(property.name())
                .version(property.version())
                .description(property.description())
                .contact(contact)
                .license(license);

        return new OpenAPI().info(info);
    }

}
