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
                .name(property.getContactName())
                .url(property.getContactSite());

        final var license = new License()
                .name(property.getLicense())
                .url(property.getLicenseUrl());

        final var info = new Info()
                .title(property.getName())
                .version(property.getVersion())
                .description(property.getDescription())
                .contact(contact)
                .license(license);

        return new OpenAPI().info(info);
    }

}
