# user/registration-uniqueness Specification

## Purpose

Garantir a unicidade de documento e e-mail no cadastro de usuários comuns e lojistas, informando todos os conflitos detectados em uma única resposta.

## Requirements

### Requirement: Unicidade global no cadastro
O sistema MUST verificar documento e e-mail contra todos os usuários existentes, independentemente do tipo de usuário, inclusive quando o cadastro for invocado diretamente pelo serviço. Se qualquer campo estiver em uso, o sistema MUST rejeitar o cadastro sem persistir usuário ou carteira.

#### Scenario: Documento já cadastrado
- **WHEN** um cadastro possui documento existente e e-mail disponível
- **THEN** o cadastro é rejeitado com erro `document` e mensagem `There is already a user registered with this document`
- **AND** nenhum usuário ou carteira é criado

#### Scenario: E-mail já cadastrado
- **WHEN** um cadastro possui documento disponível e e-mail existente
- **THEN** o cadastro é rejeitado com erro `email` e mensagem `There is already a user registered with this email`
- **AND** nenhum usuário ou carteira é criado

#### Scenario: Ambos os campos já cadastrados
- **WHEN** documento e e-mail estão em uso, pelo mesmo usuário ou por usuários diferentes
- **THEN** o cadastro é rejeitado com os dois erros, na ordem `document`, `email`, e suas mensagens de duplicidade
- **AND** nenhum usuário ou carteira é criado

#### Scenario: Conflito entre tipos de usuário
- **WHEN** um cadastro comum ou lojista usa documento ou e-mail de um usuário do outro tipo
- **THEN** o cadastro é rejeitado com todos os conflitos identificados

#### Scenario: Chamada direta ao serviço
- **WHEN** o serviço de cadastro recebe diretamente dados estruturalmente válidos com documento e e-mail existentes
- **THEN** a operação é rejeitada com os dois erros, mesmo sem passar pela validação HTTP

### Requirement: Contrato HTTP de conflitos de unicidade
Para dados estruturalmente válidos, os endpoints POST `/users/common` e POST `/users/merchants` MUST retornar HTTP 422 ao detectar conflitos nas verificações de unicidade, com `status` 422, `message` igual a `Errors`, `timestamp` e uma lista `errors` contendo `fieldName` e `message` para cada campo em conflito. A resposta MUST conter todos os conflitos detectados, sem duplicar erros por campo.

#### Scenario: Resposta para um ou dois conflitos
- **WHEN** qualquer endpoint de cadastro recebe dados estruturalmente válidos com um ou ambos os campos duplicados
- **THEN** retorna HTTP 422 e o corpo especificado, com exatamente um erro por campo duplicado

### Requirement: Preservação dos demais contratos
O sistema MUST manter as validações estruturais atuais e seu HTTP 400, preservar o HTTP 400 dos demais erros de negócio existentes e permitir cadastros válidos sem conflitos com HTTP 201 e a representação atual de usuário e carteira.

#### Scenario: Campos obrigatórios ausentes
- **WHEN** um endpoint de cadastro recebe campos obrigatórios em branco
- **THEN** retorna HTTP 400 com as mensagens estruturais atuais
- **AND** não inicia o cadastro no serviço nem consulta unicidade pela validação estrutural

#### Scenario: Cadastro sem conflitos
- **WHEN** dados válidos contêm documento e e-mail disponíveis
- **THEN** o cadastro retorna HTTP 201 com usuário do tipo solicitado e carteira associada com saldo inicial zero

#### Scenario: Outro erro de negócio
- **WHEN** uma transferência é rejeitada por uma regra de negócio já existente
- **THEN** seu status HTTP 400 e formato de erro permanecem inalterados

### Requirement: Integridade de unicidade na persistência
O sistema MUST preservar a garantia persistente de unicidade de documento e e-mail mesmo quando cadastros concorrentes passam pelas verificações prévias. O contrato HTTP 422 acima cobre conflitos detectados pelas verificações prévias; esta mudança não define novo mapeamento HTTP para violações detectadas somente na gravação concorrente.

#### Scenario: Inserções concorrentes conflitantes
- **WHEN** duas operações concorrentes tentam persistir o mesmo documento ou e-mail
- **THEN** no máximo uma delas persiste o valor conflitante
