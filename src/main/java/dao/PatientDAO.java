package dao;

import model.Patient;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public Patient save(Patient patient) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO pacientes (nome, cpf, telefone, data_nascimento)
                     VALUES (?, ?, ?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, patient.getNome());
            statement.setString(2, patient.getCpf().replaceAll("\\D", ""));
            statement.setString(3, patient.getTelefone());
            statement.setString(4, patient.getDataNascimento().toString());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                long id = generatedKeys.next() ? generatedKeys.getLong(1) : 0L;
                return new Patient(
                        id,
                        patient.getNome(),
                        patient.getCpf().replaceAll("\\D", ""),
                        patient.getTelefone(),
                        patient.getDataNascimento()
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao salvar o paciente.", exception);
        }
    }

    public List<Patient> findAll() {
        return queryPatients("SELECT * FROM pacientes ORDER BY nome", statement -> {
        });
    }

    public List<Patient> findByName(String name) {
        return queryPatients("""
                SELECT * FROM pacientes
                WHERE nome LIKE ?
                ORDER BY nome
                """, statement -> statement.setString(1, "%" + name.trim() + "%"));
    }

    public boolean deleteById(long id) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     DELETE FROM pacientes
                     WHERE id = ?
                     """)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao excluir o paciente.", exception);
        }
    }

    private List<Patient> queryPatients(String sql, SqlConsumer<PreparedStatement> binder) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.accept(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Patient> patients = new ArrayList<>();
                while (resultSet.next()) {
                    patients.add(new Patient(
                            resultSet.getLong("id"),
                            resultSet.getString("nome"),
                            resultSet.getString("cpf"),
                            resultSet.getString("telefone"),
                            LocalDate.parse(resultSet.getString("data_nascimento"))
                    ));
                }
                return patients;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao consultar pacientes.", exception);
        }
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T value) throws SQLException;
    }
}
