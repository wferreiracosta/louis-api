package br.com.wferreiracosta.louis.configs;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaManagementProfileTest {

    @Nested
    @DisplayName("1. Profile Property Resolution Tests")
    class ProfilePropertyResolutionTests {

        @Test
        @DisplayName("When no profile is explicitly set, spring.profiles.default=lcl activates the lcl profile (ddl-auto=validate)")
        void defaultProfileActivatesLclWhenNoProfileIsSet() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles()
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                // With spring.profiles.default=@activatedProperties@ filtered by Maven (defaulting to lcl),
                // the lcl profile is activated when no profile is explicitly set,
                // causing application-lcl.properties (ddl-auto=validate) to be loaded.
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto"))
                        .as("Default Maven profile (lcl) should set ddl-auto=validate via application-lcl.properties")
                        .isEqualTo("validate");
                assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("true");
                assertThat(env.getProperty("spring.flyway.locations")).isEqualTo("classpath:db/migration");
                assertThat(env.getProperty("spring.flyway.baseline-on-migrate")).isEqualTo("true");
                assertThat(env.getProperty("spring.flyway.baseline-version")).isEqualTo("3");
            }
        }

        @Test
        @DisplayName("Profile 'lcl' must set spring.jpa.hibernate.ddl-auto to 'validate' with Flyway enabled")
        void lclProfileSetsDdlAutoToValidate() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("lcl")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
                assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("true");
                assertThat(env.getProperty("spring.flyway.baseline-version")).isEqualTo("3");
            }
        }

        @Test
        @DisplayName("Profile 'qa' must set spring.jpa.hibernate.ddl-auto to 'validate' with Flyway enabled")
        void qaProfileSetsDdlAutoToValidate() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("qa")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
                assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("true");
                assertThat(env.getProperty("spring.flyway.baseline-version")).isEqualTo("3");
            }
        }

        @Test
        @DisplayName("Profile 'prd' must set spring.jpa.hibernate.ddl-auto to 'validate' with Flyway enabled")
        void prdProfileSetsDdlAutoToValidate() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prd")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
                assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("true");
                assertThat(env.getProperty("spring.flyway.baseline-version")).isEqualTo("3");
            }
        }

        @Test
        @DisplayName("Profile 'test' must set spring.jpa.hibernate.ddl-auto to 'create-drop' and disable Flyway")
        void testProfileSetsDdlAutoToCreateDrop() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("test")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
                assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("false");
            }
        }

        @Test
        @DisplayName("Profile property resolution when spring.profiles.active is supplied")
        void springProfilesActiveActivatesProfileWithValidate() {
            for (String profile : new String[]{"lcl", "qa", "prd"}) {
                try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .properties("spring.profiles.active=" + profile)
                        .run()) {
                    ConfigurableEnvironment env = context.getEnvironment();
                    assertThat(env.getActiveProfiles()).contains(profile);
                    assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
                    assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("true");
                }
            }
        }
    }

    @Nested
    @DisplayName("2. Schema Management Lifecycle Tests")
    class SchemaManagementLifecycleTests {

        private DataSource createH2DataSource(String dbName) {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("");
            return ds;
        }

        private LocalContainerEntityManagerFactoryBean createEntityManagerFactory(DataSource ds, String ddlAuto) {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(ds);
            em.setPackagesToScan("br.com.wferreiracosta.louis.models.entities");

            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setShowSql(false);
            vendorAdapter.setGenerateDdl(false);
            em.setJpaVendorAdapter(vendorAdapter);

            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            em.setJpaProperties(properties);

            return em;
        }

        @Test
        @DisplayName("Profiles 'lcl', 'qa' and 'prd' with 'validate' succeed on compatible schema and preserve data")
        void appProfilesValidateSucceedsAndPreservesData() throws Exception {
            for (String profile : new String[]{"lcl", "qa", "prd"}) {
                String dbName = profile + "_compatible_db";
                DataSource ds = createH2DataSource(dbName);

                // Step 1: Initialize compatible schema (simulating Flyway-provisioned schema)
                LocalContainerEntityManagerFactoryBean setupEm = createEntityManagerFactory(ds, "create");
                setupEm.afterPropertiesSet();

                // Step 2: Insert data into the database
                try (Connection conn = ds.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("INSERT INTO users (id, name, surname, document, email, password, type) " +
                            "VALUES (10, 'Jane', 'Doe', '98765432100', '" + profile + "@test.com', 'secret', 'COMMON')");
                }
                setupEm.destroy();

                // Step 3: Start with 'validate' against compatible schema -> succeeds without DDL errors
                LocalContainerEntityManagerFactoryBean validateEm1 = createEntityManagerFactory(ds, "validate");
                validateEm1.afterPropertiesSet();

                // Step 4: Verify data is still intact
                try (Connection conn = ds.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE id = 10")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
                validateEm1.destroy();

                // Step 5: Restart application with 'validate' on the same database
                LocalContainerEntityManagerFactoryBean validateEm2 = createEntityManagerFactory(ds, "validate");
                validateEm2.afterPropertiesSet();

                try (Connection conn = ds.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE id = 10")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
                validateEm2.destroy();
            }
        }

        @Test
        @DisplayName("Profiles 'lcl', 'qa' and 'prd' with 'validate' fail on missing or incompatible schema")
        void appProfilesValidateFailsOnMissingSchema() {
            for (String profile : new String[]{"lcl", "qa", "prd"}) {
                String dbName = profile + "_empty_db";
                DataSource ds = createH2DataSource(dbName);

                // Empty database: tables do not exist
                LocalContainerEntityManagerFactoryBean em = createEntityManagerFactory(ds, "validate");

                assertThatThrownBy(em::afterPropertiesSet)
                        .hasRootCauseInstanceOf(SchemaManagementException.class)
                        .hasMessageContaining("Schema-validation: missing table");
            }
        }
    }

    @org.springframework.context.annotation.Configuration
    static class EmptyConfig {
    }
}
