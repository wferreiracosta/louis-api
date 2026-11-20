package br.com.wferreiracosta.louis.configs;

import br.com.wferreiracosta.louis.configs.properties.SecretsManagerProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConfigTest {

    @Test
    @DisplayName("Applies default HikariCP properties from application.properties while preserving Secrets Manager credentials")
    void appliesDefaultHikariPropertiesAndPreservesSecretsManagerCredentials() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestBootstrapConfig.class)
                .web(WebApplicationType.NONE)
                .profiles("prd")
                .run()) {
            HikariDataSource dataSource = context.getBean(HikariDataSource.class);

            assertThat(dataSource.getMaximumPoolSize()).isEqualTo(10);
            assertThat(dataSource.getMinimumIdle()).isEqualTo(5);
            assertThat(dataSource.getConnectionTimeout()).isEqualTo(20_000L);
            assertThat(dataSource.getIdleTimeout()).isEqualTo(300_000L);
            assertThat(dataSource.getMaxLifetime()).isEqualTo(1_200_000L);
            assertThat(dataSource.getPoolName()).isEqualTo("LouisHikariPool");

            assertThat(dataSource.getDriverClassName()).isEqualTo("org.postgresql.Driver");
            assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:postgresql://db.internal:5432/louis");
            assertThat(dataSource.getUsername()).isEqualTo("secret-user");
            assertThat(dataSource.getPassword()).isEqualTo("secret-pass");
        }
    }

    @Test
    @DisplayName("Allows environment overrides of spring.datasource.hikari.* properties")
    void allowsOverridingHikariPropertiesPerEnvironment() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestBootstrapConfig.class)
                .web(WebApplicationType.NONE)
                .profiles("qa")
                .run(
                        "--spring.datasource.hikari.maximum-pool-size=25",
                        "--spring.datasource.hikari.minimum-idle=8",
                        "--spring.datasource.hikari.connection-timeout=15000",
                        "--spring.datasource.hikari.idle-timeout=180000",
                        "--spring.datasource.hikari.max-lifetime=900000",
                        "--spring.datasource.hikari.pool-name=CustomQaPool"
                )) {
            HikariDataSource dataSource = context.getBean(HikariDataSource.class);

            assertThat(dataSource.getMaximumPoolSize()).isEqualTo(25);
            assertThat(dataSource.getMinimumIdle()).isEqualTo(8);
            assertThat(dataSource.getConnectionTimeout()).isEqualTo(15_000L);
            assertThat(dataSource.getIdleTimeout()).isEqualTo(180_000L);
            assertThat(dataSource.getMaxLifetime()).isEqualTo(900_000L);
            assertThat(dataSource.getPoolName()).isEqualTo("CustomQaPool");

            assertThat(dataSource.getDriverClassName()).isEqualTo("org.postgresql.Driver");
            assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:postgresql://db.internal:5432/louis");
            assertThat(dataSource.getUsername()).isEqualTo("secret-user");
            assertThat(dataSource.getPassword()).isEqualTo("secret-pass");
        }
    }

    @Test
    @DisplayName("Does not register DataSourceConfig when 'test' profile is active")
    void excludesDataSourceConfigOnTestProfile() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestBootstrapConfig.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run()) {
            assertThat(context.getBeansOfType(DataSourceConfig.class)).isEmpty();
            assertThat(context.getEnvironment().containsProperty("spring.datasource.hikari.username")).isFalse();
            assertThat(context.getEnvironment().containsProperty("spring.datasource.hikari.password")).isFalse();
        }
    }

    @Configuration
    @Import({ConfigurationPropertiesAutoConfiguration.class, DataSourceConfig.class})
    static class TestBootstrapConfig {

        @Bean
        SecretsManagerProperties secretsManagerProperties() {
            return new SecretsManagerProperties(
                    "secret-user",
                    "secret-pass",
                    "postgresql",
                    "db.internal",
                    5432,
                    "louis-instance",
                    "org.postgresql.Driver",
                    "louis"
            );
        }
    }
}
