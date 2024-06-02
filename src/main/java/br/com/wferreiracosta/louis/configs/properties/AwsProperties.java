package br.com.wferreiracosta.louis.configs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aws")
public record AwsProperties(

        String region,
        String secretsManagerName,
        String url,
        String accessKeyid,
        String secretAccessKeyid

) {
}
