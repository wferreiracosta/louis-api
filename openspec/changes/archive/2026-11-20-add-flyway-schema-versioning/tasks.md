## 1. Dependências e Configuração de Perfis

- [x] 1.1 Adicionar `org.flywaydb:flyway-core` e `org.flywaydb:flyway-database-postgresql` ao `pom.xml` (sem versão explícita, herdando do `spring-boot-starter-parent`) e verificar via `mvn dependency:resolve` ou `mvn test-compile` que as dependências resolvem sem conflitos
- [x] 1.2 Configurar `spring.flyway.enabled=true`, `spring.flyway.locations=classpath:db/migration`, `spring.flyway.baseline-on-migrate=true` e `spring.flyway.baseline-version=3` em `src/main/resources/application.properties` sem definir `spring.jpa.hibernate.ddl-auto` na configuração comum, verificando que `lcl`/`qa`/`prd` herdam as propriedades Flyway
- [x] 1.3 Alterar `src/main/resources/application-lcl.properties` para `spring.jpa.hibernate.ddl-auto=validate` e verificar que `application-qa.properties` e `application-prd.properties` mantêm `validate`
- [x] 1.4 Adicionar `spring.flyway.enabled=false` em `src/test/resources/application-test.properties` preservando `spring.jpa.hibernate.ddl-auto=create-drop`, verificando que os testes existentes continuam subindo com H2 sem executar migrações Flyway

## 2. Migrações Iniciais Versionadas

- [x] 2.1 Criar `src/main/resources/db/migration/V1__create_users_table.sql` definindo a tabela `users` (`id` `BIGSERIAL PRIMARY KEY`, `name`, `surname`, `document` `UNIQUE`, `email` `UNIQUE`, `password`, `password_encoding VARCHAR(16)`, `type`), verificando paridade completa com `UserEntity`
- [x] 2.2 Criar `src/main/resources/db/migration/V2__create_wallets_table.sql` definindo a tabela `wallets` (`id` `BIGSERIAL PRIMARY KEY`, `amount NUMERIC(12, 2)`, `created_date`, `update_date`, `user_id BIGINT UNIQUE REFERENCES users(id)`), verificando paridade completa com `WalletEntity`
- [x] 2.3 Criar `src/main/resources/db/migration/V3__create_transactions_table.sql` definindo a tabela `transactions` (`id` `BIGSERIAL PRIMARY KEY`, `amount NUMERIC(12, 2)`, `transferring_id BIGINT REFERENCES wallets(id)`, `receiving_id BIGINT REFERENCES wallets(id)`, `timestamp`), verificando paridade completa com `TransactionEntity`

## 3. Testes e Verificação

- [x] 3.1 Atualizar `SchemaManagementProfileTest` para validar que o perfil `lcl` resolve `spring.jpa.hibernate.ddl-auto=validate` e que `spring.flyway.enabled` resolve como `true` (com `baseline-version=3`) em `lcl`/`qa`/`prd` e `false` em `test`, executando `mvn test -Dtest=SchemaManagementProfileTest` com sucesso
- [x] 3.2 Criar teste dedicado de migração Flyway validando: (a) execução ordenada de `V1__create_users_table.sql`, `V2__create_wallets_table.sql` e `V3__create_transactions_table.sql` e registro das versões `1`, `2` e `3` em `flyway_schema_history`, (b) compatibilidade do schema gerado com Hibernate `ddl-auto=validate` sobre `UserEntity`, `WalletEntity` e `TransactionEntity`, (c) aplicação incremental de uma `V4__` isolada em fixture temporária de teste sem recriar tabelas e (d) `baseline-on-migrate=true` com `baseline-version=3` sobre schema pré-existente sem histórico, executando o novo teste via `mvn test`
- [x] 3.3 Executar a suíte completa de testes (`mvn test`) e confirmar que todos os testes passam sem regressões
