## Why

O projeto ainda depende do Hibernate `ddl-auto` (`update` no perfil `lcl`) e de schema pré-provisionado manualmente (`validate` em `qa` e `prd`) sem controle de versão das alterações de banco. Isso impede rastrear histórico de mudanças, dificulta a evolução incremental segura entre ambientes e deixa homologação e produção sem mecanismo automatizado de aplicação e auditoria de DDL.

Adotar o Flyway conforme o plano [011-flyway-versionamento-schema.md](../../../docs/plans/011-flyway-versionamento-schema.md) — com os ajustes necessários para as entidades atuais e a estratégia `validate` — transfere o gerenciamento do schema PostgreSQL para migrações SQL versionadas, mantendo o Hibernate apenas como validador de mapeamento nos perfis de aplicação.

## What Changes

- Adicionar `org.flywaydb:flyway-core` e `org.flywaydb:flyway-database-postgresql` ao `pom.xml` (versões gerenciadas pelo BOM do Spring Boot 3.5.16), viabilizando o suporte do Flyway ao PostgreSQL.
- Configurar o Flyway em `src/main/resources/application.properties` (`spring.flyway.enabled=true`, `spring.flyway.locations=classpath:db/migration`, `spring.flyway.baseline-on-migrate=true`, `spring.flyway.baseline-version=3`) sem definir `ddl-auto` na configuração comum.
- Alterar `src/main/resources/application-lcl.properties` de `spring.jpa.hibernate.ddl-auto=update` para `spring.jpa.hibernate.ddl-auto=validate`, alinhando `lcl`, `qa` e `prd` para que o Flyway gerencie o schema e o Hibernate apenas valide o mapeamento das entidades após as migrações.
- Desabilitar o Flyway no perfil de testes (`spring.flyway.enabled=false` em `src/test/resources/application-test.properties`), mantendo `spring.jpa.hibernate.ddl-auto=create-drop` sobre H2 em memória e adicionando testes específicos para validar a execução e o mapeamento das migrações Flyway.
- Criar o diretório `src/main/resources/db/migration/` com três scripts iniciais separados por tabela, em ordem de dependência: `V1__create_users_table.sql` (incluindo `password_encoding`), `V2__create_wallets_table.sql` e `V3__create_transactions_table.sql` (com `transferring_id` e `receiving_id`), refletindo o estado atual das entidades JPA e sem incluir uma migração `V4__` definitiva no repositório.

## Capabilities

### New Capabilities

- `persistence/schema-migrations`: Versionamento e execução automatizada de migrações SQL do schema via Flyway nos ambientes da aplicação, incluindo baseline em bancos existentes, rastreamento em `flyway_schema_history`, convenção de nomenclatura e desabilitação no perfil `test`.

### Modified Capabilities

- `persistence/schema-management`: Alterar a política do perfil `lcl` de `ddl-auto=update` para `ddl-auto=validate`, unificando `lcl`, `qa` e `prd` como validadores de schema após a execução do Flyway, e explicitar a desabilitação do Flyway no perfil `test` com `create-drop`.

## Impact

- **Dependências (`pom.xml`):** inclusão de `flyway-core` e `flyway-database-postgresql` (suporte obrigatório para PostgreSQL no Flyway 10+/Spring Boot 3.5).
- **Configuração por perfil:** `application.properties`, `application-lcl.properties` (mudança de `update` para `validate`), `application-test.properties` (`spring.flyway.enabled=false`); `application-qa.properties` e `application-prd.properties` permanecem com `validate`.
- **Migrações SQL:** novos arquivos `src/main/resources/db/migration/V1__create_users_table.sql`, `V2__create_wallets_table.sql` e `V3__create_transactions_table.sql` alinhados a `UserEntity`, `WalletEntity` e `TransactionEntity`.
- **Testes:** `SchemaManagementProfileTest` precisará ser atualizado para refletir `lcl` com `validate` em vez de `update`, além de novos testes validando a configuração do Flyway e a compatibilidade dos scripts `V1`–`V3` com o modelo JPA.
- **Operação em ambientes existentes:** com `baseline-on-migrate=true` e `baseline-version=3`, bancos pré-existentes (`lcl`/`qa`/`prd`) receberão baseline na versão `3` sem reexecutar `V1`–`V3`, pressupondo que já possuam as tabelas e colunas atuais (incluindo `password_encoding`).
