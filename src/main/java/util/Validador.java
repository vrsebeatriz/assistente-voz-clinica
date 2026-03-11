package util;

import java.time.LocalDate;

public final class Validador {

    private Validador() {
    }

    public static void validarPaciente(String nome, String cpf, LocalDate dataNascimento) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do paciente.");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("Informe o CPF do paciente.");
        }
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11) {
            throw new IllegalArgumentException("O CPF precisa ter 11 digitos.");
        }
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Informe a data de nascimento.");
        }
    }

    public static void validarBuscaPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Digite um nome para a busca.");
        }
    }

    public static void validarConsulta(String nomePaciente,
                                       LocalDate dataConsulta,
                                       String horario,
                                       String profissional,
                                       String status) {
        if (nomePaciente == null || nomePaciente.isBlank()) {
            throw new IllegalArgumentException("Selecione um paciente para a consulta.");
        }
        if (dataConsulta == null) {
            throw new IllegalArgumentException("Informe a data da consulta.");
        }
        if (horario == null || horario.isBlank()) {
            throw new IllegalArgumentException("Informe o horario da consulta.");
        }
        if (profissional == null || profissional.isBlank()) {
            throw new IllegalArgumentException("Informe o profissional responsavel.");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Informe o status da consulta.");
        }
    }
}
