# Assistente de Voz para Gestao Clinica

Aplicacao desktop em JavaFX para cadastro de pacientes, cadastro de consultas vinculadas e execucao de comandos por voz em ambiente local.

## Problema que o projeto resolve

Em rotinas de clinica e recepcao, tarefas simples como localizar pacientes, consultar agenda e registrar atendimentos costumam depender de navegacao manual repetitiva.

Este projeto foi desenvolvido para centralizar essas operacoes em uma interface desktop unica, com suporte a:

- cadastro de pacientes
- busca rapida por nome
- cadastro de consultas ligadas ao paciente
- filtro de agenda por data
- comandos por voz e por texto

## Solucao proposta

O sistema combina JavaFX, SQLite e Vosk para entregar uma aplicacao local, sem dependencia de servicos online, capaz de:

- armazenar pacientes em banco SQLite
- vincular consultas a pacientes por chave estrangeira
- listar e filtrar consultas do dia ou de uma data especifica
- interpretar comandos falados como `abrir pacientes` e `buscar paciente [nome]`
- confirmar exclusoes para evitar remocoes acidentais

## Tecnologias utilizadas

- Java 21+
- JavaFX 21
- Maven 3.9+
- SQLite
- JDBC (`sqlite-jdbc`)
- Vosk
- CSS JavaFX

## Funcionalidades implementadas

- painel principal com navegacao lateral e modulo de voz
- cadastro de pacientes com nome, CPF, telefone e data de nascimento
- busca de pacientes por nome
- exclusao de paciente selecionado com confirmacao
- cadastro de consultas vinculadas a pacientes
- filtro de consultas por data
- atalho para consultas do dia
- exclusao de consulta selecionada com confirmacao
- reconhecimento de comandos por voz offline
- execucao manual de comandos por texto
- criacao automatica da estrutura do banco
- migracao de consultas antigas para o modelo com `paciente_id`

## Comandos suportados

- `abrir pacientes`
- `abrir consultas`
- `cadastrar paciente`
- `buscar paciente [nome]`
- `mostrar consultas de hoje`
- `limpar formulario`
- `fechar sistema`

## Como executar

1. Garanta Java 21+ e Maven 3.9+ instalados.
2. Se quiser usar voz real, baixe um modelo em portugues do Vosk.
3. Extraia o modelo em `models/vosk-model-small-pt-0.3`.
4. Compile o projeto:

```bash
mvn compile
```

5. Execute a aplicacao:

```bash
mvn javafx:run
```

Se o modelo do Vosk nao estiver configurado, o sistema ainda abre normalmente e pode ser testado pelo campo `Executar texto`.

## Banco de dados

Arquivo local:

```text
data/clinic-voice.db
```

Estrutura atual:

- `pacientes`: cadastro principal
- `consultas`: vinculadas por `paciente_id`

Observacoes importantes:

- o banco inicia sem dados de exemplo
- pacientes com consultas vinculadas nao podem ser excluidos
- consultas antigas em formato legado sao migradas automaticamente na inicializacao

## Estrutura do projeto

```text
src/
`-- main/
    |-- java/
    |   |-- app/
    |   |-- controller/
    |   |-- dao/
    |   |-- model/
    |   |-- service/
    |   |-- util/
    |   `-- view/
    `-- resources/
        `-- styles/
```

## Diferenciais da implementacao

- reconhecimento de voz offline
- consultas com vinculo real a pacientes
- interface desktop com fluxo unico para pacientes, agenda e voz
- confirmacao de exclusao
- busca de paciente tolerante a variacoes de nome reconhecido

## Limitacoes atuais

- o cadastro por voz ainda nao preenche formularios automaticamente
- ainda nao existe edicao de pacientes
- ainda nao existe edicao de consultas
- a qualidade do reconhecimento de nomes depende da transcricao do Vosk

## Proximos passos

- preencher formulario por voz
- adicionar edicao de pacientes
- adicionar edicao de consultas
- criar testes automatizados
- empacotar a aplicacao com `jpackage`
