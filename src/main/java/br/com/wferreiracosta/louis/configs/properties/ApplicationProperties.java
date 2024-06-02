package br.com.wferreiracosta.louis.configs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("info.app")
public record ApplicationProperties(

        String name,
        String version,
        String javaVersion,
        String description,
        String encoding,
        String contactName,
        String contactSite,
        String license,
        String licenseUrl

) {
}
