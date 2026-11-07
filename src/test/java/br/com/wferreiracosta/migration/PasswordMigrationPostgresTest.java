package br.com.wferreiracosta.migration;

import br.com.wferreiracosta.louis.configs.PasswordConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/** Requires an explicitly supplied disposable PostgreSQL database; never uses application credentials. */
@EnabledIfEnvironmentVariable(named = "BCRYPT_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
class PasswordMigrationPostgresTest {
    HikariDataSource admin, dataSource;
    JdbcTemplate jdbc;
    String schema;
    PasswordEncoder encoder = new PasswordConfig().passwordEncoder();
    List<String> output = new ArrayList<>();

    @BeforeEach
    void setup() {
        admin = source();
        schema = "bcrypt_test_" + UUID.randomUUID().toString().replace("-", "");
        new JdbcTemplate(admin).execute("create schema " + schema);
        dataSource = source();
        dataSource.setConnectionInitSql("set search_path to " + schema);
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("create table users (id bigint primary key, name varchar(255), surname varchar(255), document varchar(255), email varchar(255), type varchar(16), password varchar(255), password_encoding varchar(16))");
        jdbc.execute("create table wallets (id bigint primary key, user_id bigint references users(id), amount numeric(12,2))");
        jdbc.execute("create table transactions (id bigint primary key, transferring_id bigint references wallets(id), receiving_id bigint references wallets(id), amount numeric(12,2))");
    }

    HikariDataSource source() {
        var source = new HikariDataSource();
        source.setJdbcUrl(System.getenv("BCRYPT_TEST_JDBC_URL"));
        source.setUsername(System.getenv().getOrDefault("BCRYPT_TEST_USER", "postgres"));
        source.setPassword(System.getenv().getOrDefault("BCRYPT_TEST_PASSWORD", "postgres"));
        source.setMaximumPoolSize(3);
        return source;
    }

    @AfterEach
    void cleanup() {
        if (dataSource != null) dataSource.close();
        if (admin != null) {
            new JdbcTemplate(admin).execute("drop schema " + schema + " cascade");
            admin.close();
        }
    }

    void insert(long id, String password, String encoding) {
        jdbc.update("insert into users values (?, ?, ?, ?, ?, ?, ?, ?)", id, "Name " + id,
                "Surname " + id, "doc-" + id, id + "@mail.com", id % 2 == 0 ? "MERCHANT" : "COMMON", password, encoding);
    }

    PasswordMigration migration(PasswordEncoder passwordEncoder) {
        return new PasswordMigration(dataSource, passwordEncoder, output::add);
    }

    String password(long id) {
        return jdbc.queryForObject("select password from users where id = ?", String.class, id);
    }

    @Test
    void convertsMixedDataWithoutTouchingFinancialStateAndCanRepeat() {
        String existingHash = encoder.encode("already-converted");
        insert(1, "PRIVATE_MIGRATION_SENTINEL", null);
        insert(2, existingHash, "BCRYPT");
        insert(3, existingHash, null);
        insert(4, "", null);
        insert(5, "é".repeat(36), null);
        insert(6, "  ", null);
        jdbc.update("insert into wallets values (1, 1, 42), (2, 2, 58)");
        jdbc.update("insert into transactions values (1, 1, 2, 10)");
        var names = jdbc.queryForList("select id, name, surname, document, email, type from users order by id");
        var wallets = jdbc.queryForList("select * from wallets order by id");
        var transactions = jdbc.queryForList("select * from transactions order by id");
        assertThat(migration(encoder).run(true, 2).pending()).isEqualTo(5);
        assertThat(password(1)).isEqualTo("PRIVATE_MIGRATION_SENTINEL");
        assertThat(migration(encoder).run(false, 2)).isEqualTo(new PasswordMigration.Result(5, 1, 0));
        assertThat(encoder.matches("PRIVATE_MIGRATION_SENTINEL", password(1))).isTrue();
        assertThat(password(2)).isEqualTo(existingHash);
        assertThat(encoder.matches(existingHash, password(3))).isTrue();
        assertThat(encoder.matches("", password(4))).isTrue();
        assertThat(encoder.matches("é".repeat(36), password(5))).isTrue();
        assertThat(encoder.matches("  ", password(6))).isTrue();
        var hashes = jdbc.queryForList("select password from users order by id");
        assertThat(migration(encoder).run(false, 2)).isEqualTo(new PasswordMigration.Result(0, 6, 0));
        assertThat(jdbc.queryForList("select password from users order by id")).isEqualTo(hashes);
        assertThat(jdbc.queryForList("select id, name, surname, document, email, type from users order by id")).isEqualTo(names);
        assertThat(jdbc.queryForList("select * from wallets order by id")).isEqualTo(wallets);
        assertThat(jdbc.queryForList("select * from transactions order by id")).isEqualTo(transactions);
        assertThat(String.join("\n", output)).doesNotContain("PRIVATE_MIGRATION_SENTINEL", existingHash);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "long", "unknown", "invalid", "marked-null"})
    void finalPageInvalidDataPreventsAllWrites(String problem) {
        insert(1, "valid-secret", null);
        insert(2, "another-secret", null);
        insert(3, switch (problem) {
            case "null", "marked-null" -> null;
            case "long" -> "é".repeat(37);
            default -> "PRIVATE_INVALID_SENTINEL";
        }, switch (problem) {
            case "unknown" -> "PRIVATE_FORMAT";
            case "invalid", "marked-null" -> "BCRYPT";
            default -> null;
        });
        var before = jdbc.queryForList("select * from users order by id");
        assertThatThrownBy(() -> migration(encoder).run(false, 1))
                .hasMessage("Preflight failed: invalid=1");
        assertThat(jdbc.queryForList("select * from users order by id")).isEqualTo(before);
        assertThat(String.join("\n", output)).contains("id=3 reason=")
                .doesNotContain("PRIVATE_INVALID_SENTINEL", "PRIVATE_FORMAT", "valid-secret");
    }

    @Test
    void rollsBackWholeFailedBatchAndResumesCommittedBatches() {
        for (int i = 1; i <= 5; i++) insert(i, "secret-" + i, null);
        AtomicInteger calls = new AtomicInteger();
        PasswordEncoder failing = new PasswordEncoder() {
            public String encode(CharSequence raw) {
                if (calls.incrementAndGet() == 4) throw new IllegalStateException("PRIVATE_EXCEPTION_SENTINEL");
                return encoder.encode(raw);
            }
            public boolean matches(CharSequence raw, String hash) { return encoder.matches(raw, hash); }
        };
        assertThatThrownBy(() -> migration(failing).run(false, 2))
                .hasMessage("Batch failed and was rolled back; resume in maintenance").hasNoCause();
        String committed = password(1);
        assertThat(encoder.matches("secret-1", committed)).isTrue();
        assertThat(password(3)).isEqualTo("secret-3");
        assertThat(password(4)).isEqualTo("secret-4");
        assertThat(jdbc.queryForObject("select password_encoding from users where id = 3", String.class)).isNull();
        assertThat(migration(encoder).run(false, 2)).isEqualTo(new PasswordMigration.Result(3, 2, 0));
        assertThat(password(1)).isEqualTo(committed);
        assertThat(String.join("\n", output)).doesNotContain("PRIVATE_EXCEPTION_SENTINEL");
    }

    @Test
    void rejectsSecondMigratorAndReleasesLockOnPhysicalDisconnect() throws Exception {
        insert(1, "secret", null);
        // Unpooled connection guarantees actual session closure for this assertion.
        try (Connection connection = java.sql.DriverManager.getConnection(admin.getJdbcUrl(), admin.getUsername(), admin.getPassword());
             var lock = connection.prepareStatement("select pg_try_advisory_lock(?)")) {
            lock.setLong(1, PasswordMigration.LOCK_KEY);
            try (var result = lock.executeQuery()) { result.next(); assertThat(result.getBoolean(1)).isTrue(); }
            assertThatThrownBy(() -> migration(encoder).run(false, 1)).hasMessage("Migration already running");
            assertThat(password(1)).isEqualTo("secret");
        }
        assertThat(migration(encoder).run(false, 1).converted()).isEqualTo(1);
        assertThat(migration(encoder).run(true, 1).pending()).isZero();
    }

    @Test
    void emptyDatabaseAndInvalidBatchSize() {
        assertThat(migration(encoder).run(false, 100)).isEqualTo(new PasswordMigration.Result(0, 0, 0));
        assertThatThrownBy(() -> migration(encoder).run(false, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expansionIsIdempotentAndHibernateValidateAcceptsIt() throws Exception {
        jdbc.execute("drop table transactions, wallets, users");
        var setup = entityManager("create");
        setup.destroy();
        jdbc.execute("alter table users drop column password_encoding");
        jdbc.update("insert into users (id, name, password) values (1, 'Preserved', 'secret')");
        String script = "alter table users add column if not exists password_encoding varchar(16)";
        jdbc.execute(script);
        jdbc.execute(script);
        var validation = entityManager("validate");
        validation.destroy();
        assertThat(password(1)).isEqualTo("secret");
        assertThat(jdbc.queryForObject("select password_encoding from users where id = 1", String.class)).isNull();
    }

    LocalContainerEntityManagerFactoryBean entityManager(String mode) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("br.com.wferreiracosta.louis.models.entities");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", mode));
        factory.afterPropertiesSet();
        return factory;
    }

    @Test
    void packagedCommandUsesSecretsConfigurationAndNeverStartsWebServer() throws Exception {
        insert(1, "CLI_PRIVATE_SENTINEL", null);
        var mapper = new ObjectMapper();
        URI uri = URI.create(System.getenv("BCRYPT_TEST_JDBC_URL").substring(5));
        String secret = mapper.writeValueAsString(Map.of("username", admin.getUsername(), "password", admin.getPassword(),
                "engine", "postgresql", "host", uri.getHost(), "port", uri.getPort(),
                "driverClassName", "org.postgresql.Driver", "database", uri.getPath().substring(1) + "?currentSchema=" + schema));
        byte[] response = mapper.writeValueAsBytes(Map.of("SecretString", secret));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/x-amz-json-1.1");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            String check = command(server.getAddress().getPort(), "--check");
            assertThat(check).contains("pending=1");
            assertThat(password(1)).isEqualTo("CLI_PRIVATE_SENTINEL");
            assertThat(command(server.getAddress().getPort(), "--migrate")).contains("converted=1", "pending=0");
            assertThat(command(server.getAddress().getPort(), "--migrate")).contains("converted=0", "alreadyConverted=1");
            assertThat(encoder.matches("CLI_PRIVATE_SENTINEL", password(1))).isTrue();
        } finally { server.stop(0); }
    }

    String command(int port, String mode) throws Exception {
        Path jar = Path.of("target/louis-1.0.0-SNAPSHOT.jar").toAbsolutePath();
        assertThat(jar).exists();
        Path log = Files.createTempFile("bcrypt-cli-test-", ".log");
        ProcessBuilder builder = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dloader.main=br.com.wferreiracosta.migration.PasswordMigrationMain", "-cp", jar.toString(),
                "org.springframework.boot.loader.launch.PropertiesLauncher", mode, "1");
        builder.environment().putAll(Map.of("AWS_URL", "http://127.0.0.1:" + port,
                "AWS_ACCESS_KEYID", "test", "AWS_SECRET_ACCESS_KEYID", "test", "SPRING_PROFILES_ACTIVE", "lcl"));
        Process process = builder.redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertThat(process.waitFor(40, TimeUnit.SECONDS)).isTrue();
            String output = Files.readString(log);
            assertThat(process.exitValue()).withFailMessage(output).isZero();
            assertThat(output).doesNotContain("CLI_PRIVATE_SENTINEL", "Tomcat", "Started PasswordMigration");
            return output;
        } finally {
            if (process.isAlive()) process.destroyForcibly();
            Files.deleteIfExists(log);
        }
    }
}
