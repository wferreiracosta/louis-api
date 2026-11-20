## RENAMED Requirements

### Requirement: O perfil local deve atualizar o schema sem recriá-lo
FROM: `### Requirement: O perfil local deve atualizar o schema sem recriá-lo`
TO: `### Requirement: O perfil local deve somente validar o schema`

## MODIFIED Requirements

### Requirement: O perfil local deve somente validar o schema

Quando o perfil `lcl` estiver ativo, `application-lcl.properties` SHALL definir `spring.jpa.hibernate.ddl-auto=validate`. O Hibernate SHALL validar o schema após a execução das migrações versionadas e SHALL NOT criar, alterar, remover ou recriar objetos do schema automaticamente.

#### Scenario: Inicialização local

- **WHEN** a aplicação inicia com o perfil `lcl` ativo após o gerenciamento do schema pelas migrações versionadas
- **THEN** o Hibernate SHALL usar a estratégia `validate`
- **THEN** a inicialização SHALL ser concluída sem o Hibernate executar DDL de criação, alteração ou remoção

#### Scenario: Inicialização local com schema incompatível

- **WHEN** a aplicação inicia com o perfil `lcl` ativo contra um schema ausente ou divergente das entidades JPA após a fase de migração
- **THEN** a inicialização SHALL falhar na validação do Hibernate
- **THEN** o Hibernate SHALL NOT alterar automaticamente o schema local

### Requirement: O perfil de testes deve usar schema descartável

Quando o perfil `test` estiver ativo, a configuração de testes SHALL sobrescrever a política comum com `spring.jpa.hibernate.ddl-auto=create-drop` e desabilitar a execução automática do Flyway (`spring.flyway.enabled=false`). O schema do datasource H2 em memória SHALL ser criado pelo Hibernate para a execução dos testes e removido ao encerrar o contexto Hibernate.

#### Scenario: Execução de teste de integração com o perfil test

- **WHEN** um teste de integração é executado com o perfil `test` ativo
- **THEN** as migrações Flyway SHALL permanecer desabilitadas e o Hibernate SHALL criar o schema necessário no datasource H2 em memória antes do uso dos repositórios
- **THEN** os testes SHALL poder persistir entidades sem depender de um schema pré-existente ou de migrações PostgreSQL

#### Scenario: Encerramento do contexto de teste

- **WHEN** o contexto Hibernate do perfil `test` é encerrado
- **THEN** o Hibernate SHALL remover o schema temporário conforme a estratégia `create-drop`
- **THEN** essa política SHALL NOT ser aplicada quando a aplicação inicia com `lcl`, `qa` ou `prd`
