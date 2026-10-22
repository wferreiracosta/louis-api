package br.com.wferreiracosta.louis.configs;

import br.com.wferreiracosta.louis.configs.properties.AwsProperties;
import br.com.wferreiracosta.louis.configs.properties.SecretsManagerProperties;
import br.com.wferreiracosta.louis.exceptions.ConfigurationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@JsonTest
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class SecretsManagerConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"5432", "\"5432\""})
    void mapsAllFieldsAndIgnoresAdditionalProperties(String port) {
        final var json = """
                {"username":"test-user","password":"test-password","engine":"postgresql",
                 "host":"localhost","port":%s,"dbInstanceIdentifier":"test-instance",
                 "driverClassName":"org.postgresql.Driver","database":"test-db",
                 "additionalProperty":"ignored"}
                """.formatted(port);

        assertThat(load(json)).isEqualTo(new SecretsManagerProperties(
                "test-user", "test-password", "postgresql", "localhost", 5432,
                "test-instance", "org.postgresql.Driver", "test-db"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"username\":null,\"port\":null}"})
    void preservesMissingAndNullFields(String json) {
        assertThat(load(json)).isEqualTo(new SecretsManagerProperties(
                null, null, null, null, null, null, null, null));
    }

    @Test
    void wrapsMalformedJsonWithoutLoggingSecret(CapturedOutput output) {
        assertConfigurationFailure("{\"password\":\"SENSITIVE_SENTINEL\",broken}");
        assertThat(output.getAll()).contains("Failed to parse Secrets Manager response")
                .doesNotContain("SENSITIVE_SENTINEL");
    }

    @Test
    void wrapsInvalidPortWithoutLoggingSecret(CapturedOutput output) {
        assertConfigurationFailure("{\"port\":\"SENSITIVE_SENTINEL\"}");
        assertThat(output.getAll()).contains("Failed to parse Secrets Manager response")
                .doesNotContain("SENSITIVE_SENTINEL");
    }

    @Test
    void rejectsMissingSecretString(CapturedOutput output) {
        assertConfigurationFailure(null);
        assertThat(output.getAll()).contains("Error in obtaining information from Secrets Manager");
    }

    private void assertConfigurationFailure(String json) {
        assertThatThrownBy(() -> load(json)).isInstanceOf(ConfigurationException.class)
                .hasMessage("An error occurred when obtaining information from the secrets manager test-secret");
    }

    private SecretsManagerProperties load(String json) {
        final var client = mock(SecretsManagerClient.class);
        final var builder = mock(SecretsManagerClientBuilder.class, RETURNS_SELF);
        when(builder.build()).thenReturn(client);
        when(client.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(GetSecretValueResponse.builder().secretString(json).build());
        final var properties = new AwsProperties("us-east-1", "test-secret",
                "http://localhost:9090", "test-key", "test-key");

        try (final var factory = mockStatic(SecretsManagerClient.class)) {
            factory.when(SecretsManagerClient::builder).thenReturn(builder);
            try {
                return new SecretsManagerConfig(properties, objectMapper).secretsManager();
            } finally {
                verify(client).getSecretValue(GetSecretValueRequest.builder().secretId("test-secret").build());
                verify(client).close();
            }
        }
    }
}
