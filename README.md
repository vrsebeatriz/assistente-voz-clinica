# Assistente de Voz para Gestao Clinica

Aplicacao desktop em JavaFX para demonstracao de rotinas de clinica com cadastro de pacientes, consulta de agenda e interpretacao de comandos por voz.

## Stack

- Java 21+
- JavaFX 21
- Maven 3.9+
- SQLite
- Vosk

## Funcionalidades

- painel principal com navegacao lateral, status do modulo de voz e area de resposta do sistema
- cadastro de pacientes com nome, CPF, telefone e data de nascimento
- busca de pacientes por nome
- exclusao de paciente selecionado com confirmacao
- cadastro de consultas vinculadas a pacientes, com data, horario, profissional e status
- listagem de consultas por data
- atalho para consultas do dia
- exclusao de consulta selecionada com confirmacao
- interpretador de comandos por voz e por texto manual
- reconhecimento de voz offline com Vosk
- inicializacao automatica da estrutura do banco SQLite

## Comandos suportados

- `abrir pacientes`
- `abrir consultas`
- `cadastrar paciente`
- `buscar paciente [nome]`
- `mostrar consultas de hoje`
- `limpar formulario`
- `fechar sistema`

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

## Como executar

1. Garanta Java 21+ e Maven 3.9+ instalados.
2. Se quiser reconhecimento por voz real, baixe um modelo em portugues do Vosk.
3. Extraia o modelo em `models/vosk-model-small-pt-0.3`.
4. Compile o projeto:

```bash
mvn compile
```

5. Execute a aplicacao:

```bash
mvn javafx:run
```

Se o modelo do Vosk nao estiver configurado, a aplicacao ainda abre normalmente e o fluxo pode ser testado pelo campo `Executar texto`.

## Banco de dados

O banco local e criado automaticamente em:

```text
data/clinic-voice.db
```

As tabelas sao criadas automaticamente na primeira execucao, mas o banco agora inicia sem dados de exemplo.

Estrutura atual do modelo:

- `pacientes`: cadastro principal da base
- `consultas`: vinculadas por `paciente_id` com chave estrangeira

Se a base tiver sido criada em uma versao antiga do projeto, a migracao da tabela de consultas e feita automaticamente na inicializacao.

## Observacoes importantes

- O projeto ja foi validado com `mvn compile`.
- As consultas agora sao armazenadas com vinculo real para pacientes no banco.
- Um paciente com consultas vinculadas nao pode ser excluido enquanto essas consultas existirem.
- A exclusao de paciente e consulta depende de selecionar uma linha na tabela correspondente.
- O reconhecimento por voz de nomes depende da transcricao do Vosk; o sistema tenta casar o texto reconhecido com todos os pacientes cadastrados.

## Fluxo sugerido de demonstracao

1. Abrir a tela de pacientes.
2. Cadastrar um novo paciente.
3. Buscar um paciente cadastrado.
4. Excluir um paciente selecionado.
5. Abrir a tela de consultas.
6. Cadastrar uma consulta vinculada ao paciente.
7. Filtrar consultas por data.
8. Excluir uma consulta selecionada.
9. Testar um comando manual no painel de voz.

## Melhorias futuras

- preencher formulario por voz
- adicionar testes automatizados para controllers, services e DAOs
- criar edicao de pacientes e consultas
- empacotar a aplicacao com `jpackage`
