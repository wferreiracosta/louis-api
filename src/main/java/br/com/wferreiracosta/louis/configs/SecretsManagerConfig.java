package br.com.wferreiracosta.louis.configs;

import br.com.wferreiracosta.louis.configs.properties.AwsProperties;
import br.com.wferreiracosta.louis.configs.properties.SecretsManagerProperties;
import br.com.wferreiracosta.louis.exceptions.ConfigurationException;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;

import static java.lang.String.format;
import static software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create;
import static software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create;

@Slf4j
@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class SecretsManagerConfig {

    private final AwsProperties awsProperties;

    @Bean
    public SecretsManagerProperties secretsManager() {
        final var client = SecretsManagerClient.builder()
                .region(Region.US_EAST_1);

        if (!awsProperties.url().isEmpty() && !awsProperties.accessKeyid().isEmpty()
                && !awsProperties.secretAccessKeyid().isEmpty()) {
            client.endpointOverride(URI.create(awsProperties.url()))
                    .credentialsProvider(create(
                            create(awsProperties.accessKeyid(), awsProperties.secretAccessKeyid())));
        } else {
            client.credentialsProvider(DefaultCredentialsProvider.create());
        }

        final var request = GetSecretValueRequest.builder()
                .secretId(awsProperties.secretsManagerName())
                .build();

        try (final var clientBuild = client.build()) {
            final var response = clientBuild.getSecretValue(request);
            return map(response);
        } catch (Exception e) {
            final var message = format("An error occurred when obtaining information from the secrets manager %s",
                    awsProperties.secretsManagerName());
            log.error(e.getLocalizedMessage());
            throw new ConfigurationException(message);
        }
    }

    private SecretsManagerProperties map(final GetSecretValueResponse response) {
        final var value = response.getValueForField("SecretString", String.class);

        if (value.isEmpty()) {
            final var message = "Error in obtaining information from Secrets Manager";
            log.error(message);
            throw new ConfigurationException(message);
        }

        final var gson = new Gson();

        return gson.fromJson(value.get(), SecretsManagerProperties.class);
    }

}
