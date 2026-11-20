## Why

Atualmente, em `DataSourceConfig.java`, o `HikariDataSource` é instanciado apenas com as credenciais e a URL JDBC obtidas do AWS Secrets Manager, operando com os valores padrão do HikariCP (por exemplo, `connectionTimeout` de 30 s, `idleTimeout` de 10 min, `maxLifetime` de 30 min e `minimumIdle` igual ao `maximumPoolSize`). Para preparar a aplicação para produção, é necessário configurar parâmetros explícitos e adequados para o pool de conexões e externalizá-los em propriedades Spring para permitir ajustes por ambiente sem recompilação.

## What Changes

- Definir as propriedades padrão do pool HikariCP em `src/main/resources/application.properties` sob o prefixo `spring.datasource.hikari.*` (`maximum-pool-size=10`, `minimum-idle=5`, `connection-timeout=20000`, `idle-timeout=300000`, `max-lifetime=1200000`, `pool-name=LouisHikariPool`).
- Configurar o `DataSourceConfig` (`@Profile("!test")`) para vincular (`bind`) as propriedades `spring.datasource.hikari.*` via `@ConfigurationProperties` ao `HikariDataSource` criado programaticamente com as credenciais do `SecretsManagerProperties`.
- Garantir que o perfil `test` permaneça funcional sem depender de AWS Secrets Manager e que os valores configurados possam ser sobrescritos por arquivos de perfil ou variáveis de ambiente Spring.

## Capabilities

### New Capabilities
- `persistence/connection-pool`: Define a configuração externalizada e o binding dos parâmetros do pool de conexões HikariCP para o `DataSource` da aplicação, preservando a obtenção de credenciais via Secrets Manager e permitindo sobrescrita por ambiente.

### Modified Capabilities
<!-- Nenhuma capability existente tem seus requisitos alterados. -->

## Impact

- **Configuração de DataSource**: `src/main/java/br/com/wferreiracosta/louis/configs/DataSourceConfig.java` passa a aplicar propriedades `spring.datasource.hikari.*` ao `HikariDataSource`.
- **Propriedades da Aplicação**: `src/main/resources/application.properties` passa a declarar os valores padrão do pool HikariCP (herdáveis e sobrescrevíveis por `application-{profile}.properties` e variáveis de ambiente).
- **Testes**: Novos testes unitários/de contexto para validar o binding das propriedades `spring.datasource.hikari.*` no `DataSourceConfig` e a preservação dos parâmetros provenientes do `SecretsManagerProperties`, sem regressão na suíte existente com perfil `test`.
