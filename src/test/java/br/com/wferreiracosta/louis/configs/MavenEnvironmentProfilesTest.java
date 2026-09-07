package br.com.wferreiracosta.louis.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates that the Maven profiles (lcl, qa, prd) declared in {@code pom.xml}
 * are correctly integrated with Spring Boot environment configuration.
 *
 * <p>Each declared Maven profile maps to a Spring Boot profile-specific properties
 * file ({@code application-{profile}.properties}). These tests assert:
 * <ol>
 *   <li>All three profile-specific properties files exist on the classpath.</li>
 *   <li>Each file defines the expected {@code spring.jpa.hibernate.ddl-auto} value.</li>
 * </ol>
 *
 * <p>The {@link SchemaManagementProfileTest} covers the full Spring context profile
 * activation. These tests focus on the profile file presence and content that is
 * specific to the Maven profiles declared in this change.
 */
@DisplayName("Maven Environment Profiles - pom.xml Profile Configuration")
class MavenEnvironmentProfilesTest {

    private static final Set<String> DECLARED_PROFILES = Set.of("lcl", "qa", "prd");

    /**
     * Starts a lightweight Spring context with the given profile and returns the
     * value of {@code spring.jpa.hibernate.ddl-auto} from the loaded environment.
     */
    private String ddlAutoForProfile(String profile) {
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyConfig.class)
                .web(WebApplicationType.NONE)
                .profiles(profile)
                .run()) {
            return ctx.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        }
    }

    @Test
    @DisplayName("All three Maven environment profile property files must exist on classpath")
    void allEnvironmentProfileFilesExistOnClasspath() {
        for (String profile : DECLARED_PROFILES) {
            URL resource = getClass().getClassLoader()
                    .getResource("application-" + profile + ".properties");
            assertThat(resource)
                    .as("application-%s.properties must be present on classpath", profile)
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Profile 'lcl' configures spring.jpa.hibernate.ddl-auto=update")
    void lclProfileConfiguresDdlAutoUpdate() {
        assertThat(ddlAutoForProfile("lcl"))
                .as("application-lcl.properties must define ddl-auto=update")
                .isEqualTo("update");
    }

    @Test
    @DisplayName("Profile 'qa' configures spring.jpa.hibernate.ddl-auto=validate")
    void qaProfileConfiguresDdlAutoValidate() {
        assertThat(ddlAutoForProfile("qa"))
                .as("application-qa.properties must define ddl-auto=validate")
                .isEqualTo("validate");
    }

    @Test
    @DisplayName("Profile 'prd' configures spring.jpa.hibernate.ddl-auto=validate")
    void prdProfileConfiguresDdlAutoValidate() {
        assertThat(ddlAutoForProfile("prd"))
                .as("application-prd.properties must define ddl-auto=validate")
                .isEqualTo("validate");
    }

    @org.springframework.context.annotation.Configuration
    static class EmptyConfig {
    }
}
