package dao;

import model.Consultation;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {

    public Consultation save(Consultation consultation) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO consultas (paciente_id, data_consulta, horario, profissional, status)
                     VALUES (?, ?, ?, ?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, consultation.getPatientId());
            statement.setString(2, consultation.getDataConsulta().toString());
            statement.setString(3, consultation.getHorario().toString());
            statement.setString(4, consultation.getProfissional());
            statement.setString(5, consultation.getStatus());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                long id = generatedKeys.next() ? generatedKeys.getLong(1) : 0L;
                return new Consultation(
                        id,
                        consultation.getPatientId(),
                        consultation.getNomePaciente(),
                        consultation.getDataConsulta(),
                        consultation.getHorario(),
                        consultation.getProfissional(),
                        consultation.getStatus()
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao salvar a consulta.", exception);
        }
    }

    public List<Consultation> findByDate(LocalDate date) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT consulta.id,
                            consulta.paciente_id,
                            paciente.nome AS nome_paciente,
                            consulta.data_consulta,
                            consulta.horario,
                            consulta.profissional,
                            consulta.status
                     FROM consultas consulta
                     JOIN pacientes paciente ON paciente.id = consulta.paciente_id
                     WHERE consulta.data_consulta = ?
                     ORDER BY consulta.horario
                     """)) {
            statement.setString(1, date.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Consultation> consultations = new ArrayList<>();
                while (resultSet.next()) {
                    consultations.add(new Consultation(
                            resultSet.getLong("id"),
                            resultSet.getLong("paciente_id"),
                            resultSet.getString("nome_paciente"),
                            LocalDate.parse(resultSet.getString("data_consulta")),
                            LocalTime.parse(resultSet.getString("horario")),
                            resultSet.getString("profissional"),
                            resultSet.getString("status")
                    ));
                }
                return consultations;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao consultar as consultas.", exception);
        }
    }

    public boolean deleteById(long id) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     DELETE FROM consultas
                     WHERE id = ?
                     """)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao excluir a consulta.", exception);
        }
    }
}
