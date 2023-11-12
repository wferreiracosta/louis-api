package br.com.wferreiracosta.louis.configs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("info.app")
public class ApplicationProperties {

    private String name;
    private String version;
    private String javaVersion;
    private String description;
    private String encoding;
    private String contactName;
    private String contactSite;
    private String license;
    private String licenseUrl;

}
