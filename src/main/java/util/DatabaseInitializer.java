package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS pacientes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        cpf TEXT NOT NULL UNIQUE,
                        telefone TEXT,
                        data_nascimento TEXT NOT NULL
                    )
                    """);
            ensureConsultationsTable(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao inicializar o banco de dados.", exception);
        }
    }

    private static void ensureConsultationsTable(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultas")) {
            createConsultationsTable(connection);
            return;
        }

        if (!tableHasColumn(connection, "consultas", "paciente_id")) {
            migrateLegacyConsultations(connection);
        }
    }

    private static void migrateLegacyConsultations(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE consultas RENAME TO consultas_legacy");
            createConsultationsTable(connection);
            statement.executeUpdate("""
                    INSERT INTO consultas (id, paciente_id, data_consulta, horario, profissional, status)
                    SELECT legacy.id,
                           paciente.id,
                           legacy.data_consulta,
                           legacy.horario,
                           legacy.profissional,
                           legacy.status
                    FROM consultas_legacy legacy
                    JOIN pacientes paciente
                      ON lower(trim(paciente.nome)) = lower(trim(legacy.nome_paciente))
                    """);
            statement.executeUpdate("DROP TABLE consultas_legacy");
        }
    }

    private static void createConsultationsTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS consultas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        paciente_id INTEGER NOT NULL,
                        data_consulta TEXT NOT NULL,
                        horario TEXT NOT NULL,
                        profissional TEXT NOT NULL,
                        status TEXT NOT NULL,
                        FOREIGN KEY (paciente_id) REFERENCES pacientes (id) ON DELETE RESTRICT
                    )
                    """);
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT name
                FROM sqlite_master
                WHERE type = 'table' AND name = ?
                """)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean tableHasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
