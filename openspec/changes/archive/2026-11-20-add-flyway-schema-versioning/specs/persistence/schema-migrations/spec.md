## Purpose

Gerenciar a criação e evolução do schema do banco de dados PostgreSQL por meio de scripts SQL versionados executados automaticamente na inicialização da aplicação, mantendo histórico auditável de migrações sem alterações implícitas via ORM.

## ADDED Requirements

### Requirement: Migração versionada na inicialização da aplicação

Quando os perfis de aplicação (`lcl`, `qa` ou `prd`) estiverem ativos, o sistema SHALL executar migrações SQL versionadas a partir de `classpath:db/migration` antes da validação do schema pelo ORM e SHALL registrar cada versão aplicada na tabela `flyway_schema_history`.

#### Scenario: Inicialização contra banco de dados vazio

- **WHEN** a aplicação inicia com perfil `lcl`, `qa` ou `prd` ativo contra um banco PostgreSQL sem tabelas da aplicação
- **THEN** o sistema SHALL aplicar as migrações iniciais versionadas (`V1`, `V2` e `V3`) em ordem criando as tabelas `users`, `wallets` e `transactions` com suas restrições e chaves estrangeiras
- **THEN** o sistema SHALL registrar a aplicação das versões `1`, `2` e `3` na tabela `flyway_schema_history`

#### Scenario: Reinicialização após migração aplicada

- **WHEN** a aplicação reinicia contra o mesmo banco sem novas versões de migração
- **THEN** o sistema SHALL verificar `flyway_schema_history` e não SHALL reaplicar migrações já concluídas
- **THEN** os dados existentes nas tabelas SHALL permanecer preservados

### Requirement: Baseline automático em bancos pré-existentes

O sistema SHALL habilitar `baseline-on-migrate` com `baseline-version=3` na configuração padrão da aplicação para permitir a adoção incremental em bancos que já possuam tabelas de negócio sem histórico prévio de migrações.

#### Scenario: Inicialização contra banco já populado sem histórico de migrações

- **WHEN** a aplicação inicia contra um banco não vazio que ainda não possui a tabela `flyway_schema_history`
- **THEN** o sistema SHALL registrar o baseline na versão `3` sem executar novamente os scripts `V1`, `V2` e `V3`
- **THEN** migrações futuras (`V4` ou superiores) SHALL ser aplicadas incrementalmente a partir desse baseline

### Requirement: Migração inicial alinhada ao modelo persistido

As migrações iniciais separadas por tabela (`V1__create_users_table.sql`, `V2__create_wallets_table.sql` e `V3__create_transactions_table.sql`) SHALL definir integralmente a estrutura atual das tabelas `users`, `wallets` e `transactions` em sintaxe compatível com PostgreSQL e em ordem de dependência de chave estrangeira, incluindo `password_encoding` em `users` e `transferring_id` e `receiving_id` em `transactions`.

#### Scenario: Compatibilidade entre scripts iniciais e validação do modelo

- **WHEN** as migrações iniciais `V1`, `V2` e `V3` são aplicadas sobre um banco PostgreSQL limpo
- **THEN** o schema resultante SHALL conter todas as tabelas, colunas, tipos, nulabilidades, chaves únicas e chaves estrangeiras exigidas pelo mapeamento JPA vigente
- **THEN** a validação de schema na inicialização SHALL ser concluída sem erros

### Requirement: Aplicação incremental de novas versões e nomenclatura

O sistema SHALL reconhecer scripts no diretório `db/migration` seguindo a convenção de prefixo versionado `V{n}__<descricao>.sql` e repetível `R__<descricao>.sql`, aplicando apenas versões pendentes em ordem numérica.

#### Scenario: Adição de uma nova migração versionada após a base inicial

- **WHEN** uma migração `V4__<descricao>.sql` é adicionada após as versões `1`, `2` e `3` já estarem registradas em `flyway_schema_history`
- **THEN** o sistema SHALL aplicar somente a migração `V4` sem recriar as tabelas existentes
- **THEN** a tabela `flyway_schema_history` SHALL conter registros distintos para `1`, `2`, `3` e `4`

#### Scenario: Falha em migração pendente

- **WHEN** uma migração pendente contém SQL inválido ou incompatível com o banco
- **THEN** a inicialização da aplicação SHALL falhar imediatamente antes de aceitar requisições
- **THEN** o registro de histórico SHALL refletir que a migração não foi concluída com sucesso

### Requirement: Desabilitação das migrações no perfil de testes

Quando o perfil `test` estiver ativo, o sistema SHALL desabilitar a execução automática de migrações versionadas para que a suíte de testes utilize schema descartável em memória sem exigir compatibilidade de scripts PostgreSQL no H2.

#### Scenario: Execução de testes com perfil test ativo

- **WHEN** o contexto de testes inicia com o perfil `test` ativo
- **THEN** o mecanismo de migração versionada SHALL permanecer desabilitado (`spring.flyway.enabled=false`)
- **THEN** o contexto de testes não SHALL tentar executar os scripts SQL de `classpath:db/migration` contra o banco H2 em memória
