package br.com.wferreiracosta.migration;

import br.com.wferreiracosta.louis.configs.DataSourceConfig;
import br.com.wferreiracosta.louis.configs.PasswordConfig;
import br.com.wferreiracosta.louis.configs.SecretsManagerConfig;
import br.com.wferreiracosta.louis.configs.properties.AwsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

/** Deliberately outside the application's component-scan package. */
public final class PasswordMigrationMain {
    private PasswordMigrationMain() { }

    public static void main(String[] args) {
        System.exit(execute(args));
    }

    static int execute(String[] args) {
        if (args.length < 1 || args.length > 2
                || !(args[0].equals("--check") || args[0].equals("--migrate"))) {
            System.err.println("Usage: PasswordMigrationMain --check|--migrate [batch-size:1..10000]");
            return 2;
        }
        final int batchSize;
        try {
            batchSize = args.length == 2 ? Integer.parseInt(args[1]) : 100;
            if (batchSize < 1 || batchSize > 10000) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.err.println("Batch size must be between 1 and 10000");
            return 2;
        }
        // An offline tool must never log framework/JDBC exceptions containing row values.
        System.setProperty("logging.level.root", "OFF");
        try (var context = new SpringApplicationBuilder(MigrationConfiguration.class)
                .web(WebApplicationType.NONE).bannerMode(Banner.Mode.OFF)
                .logStartupInfo(false).run()) {
            new PasswordMigration(context.getBean(DataSource.class), context.getBean(PasswordEncoder.class),
                    System.out::println).run(args[0].equals("--check"), batchSize);
            return 0;
        } catch (PasswordMigration.MigrationException e) {
            System.err.println(e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Migration could not start; check configuration and database availability");
            return 1;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AwsProperties.class)
    @Import({DataSourceConfig.class, SecretsManagerConfig.class, PasswordConfig.class})
    static class MigrationConfiguration {
        @Bean ObjectMapper objectMapper() { return new ObjectMapper(); }
    }
}
