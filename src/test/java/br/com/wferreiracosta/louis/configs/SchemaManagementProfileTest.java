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
        @DisplayName("Common configuration (no active profile) must NOT define spring.jpa.hibernate.ddl-auto")
        void commonConfigurationDoesNotDefineDdlAuto() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles()
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto"))
                        .as("Common configuration should not define ddl-auto")
                        .isNull();
            }
        }

        @Test
        @DisplayName("Profile 'lcl' must set spring.jpa.hibernate.ddl-auto to 'update'")
        void lclProfileSetsDdlAutoToUpdate() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("lcl")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
            }
        }

        @Test
        @DisplayName("Profile 'qa' must set spring.jpa.hibernate.ddl-auto to 'validate'")
        void qaProfileSetsDdlAutoToValidate() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("qa")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
            }
        }

        @Test
        @DisplayName("Profile 'prd' must set spring.jpa.hibernate.ddl-auto to 'validate'")
        void prdProfileSetsDdlAutoToValidate() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prd")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
            }
        }

        @Test
        @DisplayName("Profile 'test' must set spring.jpa.hibernate.ddl-auto to 'create-drop'")
        void testProfileSetsDdlAutoToCreateDrop() {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("test")
                    .run()) {
                ConfigurableEnvironment env = context.getEnvironment();
                assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
            }
        }

        @Test
        @DisplayName("Profile property resolution when spring.profiles.active is supplied")
        void springProfilesActiveActivatesProfileWithValidate() {
            for (String profile : new String[]{"qa", "prd"}) {
                try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .properties("spring.profiles.active=" + profile)
                        .run()) {
                    ConfigurableEnvironment env = context.getEnvironment();
                    assertThat(env.getActiveProfiles()).contains(profile);
                    assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
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
        @DisplayName("2.2 - Profile 'lcl' with 'update' initializes schema without dropping existing tables")
        void lclUpdateCreatesAndPreservesSchema() throws Exception {
            String dbName = "lcl_test_db";
            DataSource ds = createH2DataSource(dbName);

            // First run: update creates the tables
            LocalContainerEntityManagerFactoryBean em1 = createEntityManagerFactory(ds, "update");
            em1.afterPropertiesSet();

            // Insert data
            try (Connection conn = ds.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO users (id, name, surname, document, email, password, type) " +
                        "VALUES (1, 'John', 'Doe', '12345678901', 'john@test.com', 'pass', 'COMMON')");
            }
            em1.destroy();

            // Second run: update preserves data and doesn't drop schema
            LocalContainerEntityManagerFactoryBean em2 = createEntityManagerFactory(ds, "update");
            em2.afterPropertiesSet();

            try (Connection conn = ds.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE id = 1")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
            em2.destroy();
        }

        @Test
        @DisplayName("2.3 & 2.4 - Profiles 'qa' and 'prd' with 'validate' succeed on compatible schema and preserve data")
        void qaAndPrdValidateSucceedsAndPreservesData() throws Exception {
            for (String profile : new String[]{"qa", "prd"}) {
                String dbName = profile + "_compatible_db";
                DataSource ds = createH2DataSource(dbName);

                // Step 1: Initialize compatible schema using update (simulating provisioned schema)
                LocalContainerEntityManagerFactoryBean setupEm = createEntityManagerFactory(ds, "update");
                setupEm.afterPropertiesSet();

                // Step 2: Insert data into the database
                try (Connection conn = ds.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("INSERT INTO users (id, name, surname, document, email, password, type) " +
                            "VALUES (10, 'Jane', 'Doe', '98765432100', '" + profile + "@test.com', 'secret', 'COMMON')");
                }
                setupEm.destroy();

                // Step 3 (2.3): Start with 'validate' against compatible schema -> succeeds without DDL errors
                LocalContainerEntityManagerFactoryBean validateEm1 = createEntityManagerFactory(ds, "validate");
                validateEm1.afterPropertiesSet();

                // Step 4 (2.4): Verify data is still intact
                try (Connection conn = ds.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE id = 10")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
                validateEm1.destroy();

                // Step 5 (2.4): Restart application with 'validate' on the same database
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
        @DisplayName("2.5 - Profiles 'qa' and 'prd' with 'validate' fail on missing or incompatible schema")
        void qaAndPrdValidateFailsOnMissingSchema() {
            for (String profile : new String[]{"qa", "prd"}) {
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
