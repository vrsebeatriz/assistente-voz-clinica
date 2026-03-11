package util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    private static final Path DATABASE_PATH = Path.of("data", "clinic-voice.db");
    private static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_PATH.toString().replace('\\', '/');

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        ensureDirectory();
        Connection connection = DriverManager.getConnection(JDBC_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private static void ensureDirectory() {
        try {
            Files.createDirectories(DATABASE_PATH.getParent());
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel preparar a pasta do banco de dados.", exception);
        }
    }
}
