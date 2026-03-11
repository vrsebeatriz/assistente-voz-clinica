package model;

import java.time.LocalDate;

public class Patient {

    private final long id;
    private final String nome;
    private final String cpf;
    private final String telefone;
    private final LocalDate dataNascimento;

    public Patient(long id, String nome, String cpf, String telefone, LocalDate dataNascimento) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
}
