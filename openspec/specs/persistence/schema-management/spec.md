# Schema Management Specification

## Purpose

Define políticas previsíveis de gerenciamento do schema Hibernate por ambiente, protegendo bancos compartilhados contra alterações automáticas e preservando o isolamento dos testes.

## Requirements

### Requirement: Os ambientes da aplicação devem usar perfis explícitos de schema

A configuração comum em `application.properties` SHALL NOT definir `spring.jpa.hibernate.ddl-auto`. A aplicação SHALL disponibilizar os arquivos `application-lcl.properties`, `application-qa.properties` e `application-prd.properties`, e cada um SHALL definir explicitamente a estratégia `ddl-auto` aplicável ao respectivo perfil Spring.

#### Scenario: Inicialização sem perfil de ambiente

- **WHEN** a aplicação inicia sem que `lcl`, `qa` ou `prd` esteja ativo
- **THEN** a configuração comum SHALL NOT impor uma estratégia `ddl-auto` de ambiente
- **THEN** a seleção de perfil SHALL permanecer sob controle da configuração de execução

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

### Requirement: Homologação e produção devem somente validar o schema

Quando os perfis `qa` ou `prd` estiverem ativos, seus respectivos arquivos de perfil SHALL definir `spring.jpa.hibernate.ddl-auto=validate`. O Hibernate SHALL validar que o schema existente é compatível com as entidades JPA e SHALL NOT criar, alterar, remover ou recriar objetos do schema.

#### Scenario: Inicialização em homologação ou produção com schema compatível

- **WHEN** a aplicação inicia com o perfil `qa` ou `prd` ativo contra um schema compatível com as entidades JPA
- **THEN** a inicialização SHALL ser concluída sem o Hibernate executar DDL de criação, alteração ou remoção
- **THEN** os dados existentes no banco SHALL permanecer disponíveis após uma reinicialização da aplicação

#### Scenario: Inicialização em homologação ou produção com schema incompatível

- **WHEN** a aplicação inicia com o perfil `qa` ou `prd` ativo contra um schema ausente ou incompatível com as entidades JPA
- **THEN** a inicialização SHALL falhar na validação do Hibernate
- **THEN** o Hibernate SHALL NOT tentar corrigir ou recriar o schema automaticamente

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
