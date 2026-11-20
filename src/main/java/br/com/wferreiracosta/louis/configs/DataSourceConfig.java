package br.com.wferreiracosta.louis.configs;

import br.com.wferreiracosta.louis.configs.properties.SecretsManagerProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static java.lang.String.format;

@Slf4j
@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final SecretsManagerProperties properties;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource() {
        final var url = format("jdbc:%s://%s:%s/%s",
                properties.engine(), properties.host(),
                properties.port(), properties.database());

        final var dataSource = new HikariDataSource();
        dataSource.setDriverClassName(properties.driverClassName());
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(properties.username());
        dataSource.setPassword(properties.password());

        return dataSource;
    }

}
