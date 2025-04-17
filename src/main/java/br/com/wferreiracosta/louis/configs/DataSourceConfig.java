package br.com.wferreiracosta.louis.configs;

import br.com.wferreiracosta.louis.configs.properties.SecretsManagerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static java.lang.String.format;

@Slf4j
@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final SecretsManagerProperties properties;

    @Bean
    public DriverManagerDataSource dataSource() {
        final var url = format("jdbc:%s://%s:%s/%s",
                properties.engine(), properties.host(),
                properties.port(), properties.database());

        final var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(properties.driverClassName());
        dataSource.setUrl(url);
        dataSource.setUsername(properties.username());
        dataSource.setPassword(properties.password());

        return dataSource;
    }

}
