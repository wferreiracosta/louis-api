## Context

O projeto usa Spring Boot `3.5.16` com PostgreSQL nos perfis de aplicação (`lcl`, `qa`, `prd`) e H2 em memória no perfil `test`. Conforme [proposal.md](proposal.md), a criação e a evolução do schema passarão a ser governadas pelo Flyway (`src/main/resources/db/migration`), removendo a dependência de `ddl-auto=update` em `lcl` e de scripts manuais em `qa`/`prd`.

A revisão do código atual identificou três divergências importantes entre o rascunho em [docs/plans/011-flyway-versionamento-schema.md](../../../docs/plans/011-flyway-versionamento-schema.md) e o estado vigente do projeto:
1. O `UserEntity` possui a coluna `password_encoding VARCHAR(16)` (introduzida na mudança `hash-user-passwords-bcrypt`), ausente no SQL rascunhado no plano.
2. O `TransactionEntity` mapeia as chaves estrangeiras como `transferring_id` e `receiving_id` (`@JoinColumn`), não `transferring` e `receiving`.
3. O projeto utiliza Spring Boot `3.5.16` (Flyway 10+/11+), onde o suporte ao dialeto PostgreSQL foi modularizado para o artefato `org.flywaydb:flyway-database-postgresql`.

## Goals / Non-Goals

**Goals:**
- Garantir que o Flyway execute antes do `EntityManagerFactory` do Hibernate nos perfis `lcl`, `qa` e `prd`, com `DataSourceConfig` fornecendo o `DataSource` único da aplicação.
- Alinhar as migrações iniciais separadas por tabela (`V1__create_users_table.sql`, `V2__create_wallets_table.sql` e `V3__create_transactions_table.sql`) exatamente às entidades JPA atuais (`UserEntity`, `WalletEntity`, `TransactionEntity`).
- Manter `spring.jpa.hibernate.ddl-auto=validate` em `lcl`, `qa` e `prd` para detectar imediatamente qualquer divergência entre migrações SQL e mapeamentos JPA.
- Preservar a suíte de testes atual em H2 (`ddl-auto=create-drop`, Flyway desabilitado) enquanto adiciona testes automatizados que comprovem a configuração e a execução real das migrações `V1`–`V3` compatíveis com Hibernate `validate`.

**Non-Goals:**
- Não commitar uma migração `V4__` definitiva de exemplo no repositório (a verificação de migração incremental deve ser feita via teste automatizado temporário/isolado ou validação em execução sem deixar artefato artificial no `src/main/resources/db/migration`).
- Não alterar o funcionamento do utilitário offline `PasswordMigrationMain` nem migrar dados de senhas.
- Não executar ou alterar bancos reais de `qa` ou `prd` como parte desta mudança de código.

## Decisions

### 1. Dependências Flyway no `pom.xml` (`flyway-core` + `flyway-database-postgresql`)
- **Decisão:** Adicionar `org.flywaydb:flyway-core` e `org.flywaydb:flyway-database-postgresql`, ambos sem `<version>` explícita para herdar do BOM do `spring-boot-starter-parent` `3.5.16`.
- **Alternativa considerada:** Adicionar apenas `flyway-core` como escrito no plano 011.
- **Racional:** A partir do Flyway 10 (incluído no Spring Boot 3.3+), o driver de metadados e dialeto PostgreSQL foi extraído para `flyway-database-postgresql`. Sem ele, a inicialização contra PostgreSQL falha com `Unsupported Database: PostgreSQL`.

### 2. Estratégia `ddl-auto=validate` nos perfis `lcl`, `qa` e `prd` (em vez de `none`)
- **Decisão:** Manter `spring.jpa.hibernate.ddl-auto` ausente de `application.properties` (preservando a política da spec `persistence/schema-management`), alterar `application-lcl.properties` de `update` para `validate`, e manter `application-qa.properties` e `application-prd.properties` com `validate`.
- **Alternativa considerada:** Definir `spring.jpa.hibernate.ddl-auto=none` em `application.properties` conforme o plano 011.
- **Racional:** Com `validate`, o Hibernate valida na inicialização se o schema gerado/migrado pelo Flyway corresponde exatamente às entidades JPA, atuando como rede de segurança contra erros em scripts SQL. Além disso, evita sobrescrever ou conflitar com os arquivos por perfil já existentes.

### 3. Scripts iniciais separados por tabela (`V1`, `V2` e `V3`) alinhados às entidades atuais
- **Decisão:** Dividir a definição inicial em três arquivos independentes em sintaxe PostgreSQL (`BIGSERIAL`, `NUMERIC(12, 2)`, `TIMESTAMP`), ordenados pelas dependências de chave estrangeira:
  - `V1__create_users_table.sql`: `users` (`id BIGSERIAL PRIMARY KEY`, `name VARCHAR(255)`, `surname VARCHAR(255)`, `document VARCHAR(255) UNIQUE`, `email VARCHAR(255) UNIQUE`, `password VARCHAR(255)`, `password_encoding VARCHAR(16)`, `type VARCHAR(255)`).
  - `V2__create_wallets_table.sql`: `wallets` (`id BIGSERIAL PRIMARY KEY`, `amount NUMERIC(12, 2)`, `created_date TIMESTAMP`, `update_date TIMESTAMP`, `user_id BIGINT UNIQUE REFERENCES users(id)`).
  - `V3__create_transactions_table.sql`: `transactions` (`id BIGSERIAL PRIMARY KEY`, `amount NUMERIC(12, 2)`, `transferring_id BIGINT REFERENCES wallets(id)`, `receiving_id BIGINT REFERENCES wallets(id)`, `timestamp TIMESTAMP`).
- **Nota sobre nulabilidade (`NOT NULL`) e `DEFAULT`:** As entidades atuais não anotam `nullable = false` na maioria dos campos JPA, embora validações Bean Validation/serviço garantam preenchimento no fluxo da API; definir `NOT NULL` em campos obrigatórios de negócio (`name`, `surname`, `document`, `email`, `password`, `type`, `amount`, `timestamp`) e `DEFAULT 0` em `wallets.amount` é compatível com o Hibernate `validate` (pois o `SchemaValidator` do Hibernate não rejeita colunas `NOT NULL` no banco quando a entidade é permissiva), mantendo integridade no banco. Para `password_encoding`, deve permanecer anulável (`NULL`), pois registros legados ainda não migrados pelo `PasswordMigration` usam `NULL` como marcador de legado.
- **Alternativa considerada:** Consolidar as três tabelas em um único `V1__create_users_wallets_transactions_tables.sql` conforme o plano 011, ou criar `V1` sem `password_encoding` e adicionar `V4` para `password_encoding`.
- **Racional:** Separar em `V1`, `V2` e `V3` mantém cada migração focada em uma única tabela, facilita leitura/revisão e respeita a ordem natural de dependências (`users` → `wallets` → `transactions`). Como o projeto já incorporou `password_encoding` em `UserEntity` antes da adoção do Flyway, `V1` já nasce com a estrutura completa de `users` para novos ambientes.

### 4. Configuração do Flyway em `application.properties` e `application-test.properties`
- **Decisão:**
  - `application.properties`: adicionar `spring.flyway.enabled=true`, `spring.flyway.locations=classpath:db/migration`, `spring.flyway.baseline-on-migrate=true` e `spring.flyway.baseline-version=3`.
  - `application-test.properties`: adicionar `spring.flyway.enabled=false` e manter `spring.jpa.hibernate.ddl-auto=create-drop`.
- **Alternativa considerada:** Manter o padrão `baseline-version=1` ou rodar Flyway nos testes de integração sobre H2 com `;MODE=PostgreSQL`.
- **Racional:** Como o schema inicial completo abrange `V1` até `V3`, configurar `spring.flyway.baseline-version=3` garante que bancos pré-existentes recebam baseline na versão `3` e não tentem executar `V2`/`V3` sobre tabelas já existentes. Desabilitar o Flyway no perfil `test` isola os testes unitários/controller/repository existentes contra acoplamento a SQL específico de PostgreSQL, enquanto testes dedicados de migração validam `V1`–`V3` (via H2 em modo PostgreSQL e/ou PostgreSQL real quando disponível, similar ao `PasswordMigrationPostgresTest`) e a compatibilidade `Flyway + Hibernate validate`.

### 5. Compatibilidade com `DataSourceConfig`
- **Decisão:** Nenhuma alteração necessária em `DataSourceConfig.java`.
- **Racional:** `DataSourceConfig` expõe o bean `DataSource` (`@Profile("!test")`), e o `FlywayAutoConfiguration` do Spring Boot usa automaticamente o `DataSource` principal do contexto antes da inicialização do `EntityManagerFactory`.

### 6. Estratégia de Testes
- **Decisão:**
  1. Atualizar `SchemaManagementProfileTest` para esperar `validate` no perfil `lcl` (em vez de `update`), removendo/ajustando os cenários específicos de `update` em `lcl` e verificando as propriedades `spring.flyway.*` por perfil (`enabled=true` e `baseline-version=3` em `lcl`/`qa`/`prd`, `enabled=false` em `test`).
  2. Adicionar teste dedicado de migração Flyway: executar o `Flyway` programaticamente com `classpath:db/migration` sobre H2 em `MODE=PostgreSQL` (ou PostgreSQL condicional via env var), validar `flyway_schema_history` contendo `V1`, `V2` e `V3`, validar uma migração `V4__` incremental em banco temporário de teste (usando location de teste em `src/test/resources/` ou diretório temporário, sem poluir `src/main/resources/db/migration`), validar `baseline-on-migrate=true` com `baseline-version=3` em banco pré-existente sem executar `V1`–`V3` e executar Hibernate `validate` sobre o schema resultante para comprovar paridade com `UserEntity`, `WalletEntity` e `TransactionEntity`.

## Risks / Trade-offs

- **[Risco] Banco pré-existente em `qa`/`prd` sem a coluna `password_encoding` ao usar `baseline-on-migrate=true` e `baseline-version=3`** → Como o baseline marca a versão `3` sem executar `V1`–`V3`, um banco existente que ainda não tenha recebido `password_encoding` falhará no `ddl-auto=validate` do Hibernate. **Mitigação:** Documentar no rollout/Migration Plan que ambientes pré-existentes precisam estar com o schema atual (incluindo `password_encoding`, já exigido pela mudança `hash-user-passwords-bcrypt`) antes ou no momento do baseline.
- **[Risco] `BIGSERIAL` e sintaxe PostgreSQL nas migrações `V1`–`V3` não rodarem nativamente em H2 padrão** → **Mitigação:** `spring.flyway.enabled=false` em `application-test.properties` mantém todos os testes existentes em H2 rodando via `create-drop`, e o teste específico de Flyway usa `MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE` no H2 (ou PostgreSQL real opcional).
- **[Risco] `SchemaManagementProfileTest` atual afirma `lcl` = `update`** → **Mitigação:** Atualizar `SchemaManagementProfileTest` na mesma mudança para refletir o novo contrato `lcl` = `validate`.

## Migration Plan

1. **Ambientes limpos (ex.: novo PostgreSQL `lcl`):** Subir a aplicação com perfil `lcl`. O Flyway cria `flyway_schema_history`, executa `V1__create_users_table.sql`, `V2__create_wallets_table.sql` e `V3__create_transactions_table.sql` em ordem e o Hibernate valida o schema.
2. **Ambientes pré-existentes (`lcl`/`qa`/`prd` com tabelas já criadas):** Garantir que a tabela `users` já possua `password_encoding` (conforme requisito da mudança anterior de BCrypt) e que `transactions` use `transferring_id`/`receiving_id`. Ao subir a aplicação, o Flyway cria `flyway_schema_history` com baseline `3` e o Hibernate valida o schema sem alterações destrutivas.
3. **Rollback:** Caso seja necessário reverter o código antes da entrada de uma futura `V4__`, o schema já existente permanece intacto; a tabela `flyway_schema_history` é inofensiva caso o Flyway seja removido, podendo ser descartada manualmente se desejado.
