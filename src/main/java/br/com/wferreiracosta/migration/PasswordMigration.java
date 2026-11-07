package br.com.wferreiracosta.migration;

import br.com.wferreiracosta.louis.validation.PasswordLengthValidator;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/** Offline operation: all application writers must be stopped by the operator. */
public final class PasswordMigration {
    static final long LOCK_KEY = 0x4c4f554953504153L;
    private static final Pattern HASH = Pattern.compile(
            "\\$2[aby]\\$(0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{21}[.Oeu][./A-Za-z0-9]{30}[.CGKOSWaeimquy26]");
    private final DataSource dataSource;
    private final PasswordEncoder encoder;
    private final Consumer<String> report;

    public PasswordMigration(DataSource dataSource, PasswordEncoder encoder, Consumer<String> report) {
        this.dataSource = dataSource;
        this.encoder = encoder;
        this.report = report;
    }

    public record Result(long converted, long alreadyConverted, long pending) { }
    private record Credential(long id, String password, String encoding) {
        @Override public String toString() { return "Credential[id=" + id + "]"; }
    }
    private record Scan(long legacy, long converted, long invalid) { }

    public Result run(boolean checkOnly, int batchSize) {
        if (batchSize < 1 || batchSize > 10000) {
            throw new IllegalArgumentException("Batch size must be between 1 and 10000");
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            if (!lock(connection, "pg_try_advisory_lock")) {
                throw new MigrationException("Migration already running");
            }
            try {
                Scan before = scan(connection, batchSize);
                if (before.invalid() != 0) {
                    throw new MigrationException("Preflight failed: invalid=" + before.invalid());
                }
                if (checkOnly) {
                    return finish(new Result(0, before.converted(), before.legacy()));
                }
                long converted = convert(connection, batchSize);
                Scan after = scan(connection, batchSize);
                if (after.legacy() != 0 || after.invalid() != 0) {
                    throw new MigrationException("Final verification failed");
                }
                return finish(new Result(converted, before.converted(), 0));
            } finally {
                // Explicit unlock is essential when close() returns a session to a pool.
                try {
                    if (!connection.getAutoCommit()) connection.rollback();
                    connection.setAutoCommit(true);
                    lock(connection, "pg_advisory_unlock");
                } catch (SQLException e) {
                    // Never return a session that might still own the lock to the pool.
                    connection.abort(Runnable::run);
                    throw e;
                }
            }
        } catch (MigrationException e) {
            throw e;
        } catch (Exception e) {
            // JDBC/encoder exceptions may contain a credential. Do not retain the cause.
            throw new MigrationException("Migration failed; inspect database availability and retry in maintenance");
        }
    }

    private Result finish(Result result) {
        report.accept("converted=" + result.converted() + " alreadyConverted="
                + result.alreadyConverted() + " pending=" + result.pending());
        return result;
    }

    private boolean lock(Connection connection, String function) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select " + function + "(?)")) {
            statement.setLong(1, LOCK_KEY);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private Scan scan(Connection connection, int batchSize) throws SQLException {
        long cursor = Long.MIN_VALUE;
        long legacy = 0, converted = 0, invalid = 0;
        while (true) {
            List<Credential> rows = page(connection, cursor, batchSize, false);
            if (rows.isEmpty()) break;
            for (Credential row : rows) {
                String problem = problem(row);
                if (problem != null) {
                    invalid++;
                    report.accept("id=" + row.id() + " reason=" + problem);
                } else if (row.encoding() == null) {
                    legacy++;
                } else {
                    converted++;
                }
                cursor = row.id();
            }
        }
        return new Scan(legacy, converted, invalid);
    }

    private String problem(Credential row) {
        if (row.encoding() == null) {
            if (row.password() == null) return "null-password";
            if (!PasswordLengthValidator.withinLimit(row.password())) return "password-over-72-bytes";
        } else if (!"BCRYPT".equals(row.encoding())) {
            return "unknown-encoding";
        } else if (row.password() == null || !HASH.matcher(row.password()).matches()) {
            return "invalid-bcrypt";
        }
        return null;
    }

    private List<Credential> page(Connection connection, long cursor, int size, boolean forUpdate)
            throws SQLException {
        String sql = "select id, password, password_encoding from users where id > ?"
                + (forUpdate ? " and password_encoding is null" : "")
                + " order by id limit ?" + (forUpdate ? " for update" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cursor);
            statement.setInt(2, size);
            try (ResultSet result = statement.executeQuery()) {
                List<Credential> rows = new ArrayList<>();
                while (result.next()) {
                    rows.add(new Credential(result.getLong(1), result.getString(2), result.getString(3)));
                }
                return rows;
            }
        }
    }

    private long convert(Connection connection, int batchSize) throws SQLException {
        long cursor = Long.MIN_VALUE, converted = 0;
        while (true) {
            connection.setAutoCommit(false);
            try {
                List<Credential> rows = page(connection, cursor, batchSize, true);
                if (rows.isEmpty()) {
                    connection.commit();
                    connection.setAutoCommit(true);
                    return converted;
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "update users set password = ?, password_encoding = 'BCRYPT' where id = ? and password_encoding is null")) {
                    for (Credential row : rows) {
                        if (problem(row) != null) throw new MigrationException("Data changed after preflight");
                        String hash = encoder.encode(row.password());
                        if (hash == null || !HASH.matcher(hash).matches()) {
                            throw new MigrationException("Encoder produced an invalid hash");
                        }
                        update.setString(1, hash);
                        update.setLong(2, row.id());
                        if (update.executeUpdate() != 1) throw new MigrationException("Data changed after preflight");
                        cursor = row.id();
                    }
                }
                connection.commit();
                converted += rows.size();
                connection.setAutoCommit(true);
                report.accept("committed=" + converted);
            } catch (Exception e) {
                connection.rollback();
                throw new MigrationException("Batch failed and was rolled back; resume in maintenance");
            }
        }
    }

    public static final class MigrationException extends RuntimeException {
        public MigrationException(String message) { super(message); }
    }
}
