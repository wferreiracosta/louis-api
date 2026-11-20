# Connection Pool Specification

## Purpose

Define a configuração externalizada e previsível do pool de conexões com o banco de dados para ambientes de execução da aplicação, garantindo limites de tamanho, timeouts e identificação explícitos sem recompilação.

## Requirements

### Requirement: Parâmetros padrão do pool de conexões externalizados
A configuração comum da aplicação (`src/main/resources/application.properties`) SHALL declarar explicitamente os parâmetros padrão do pool de conexões sob o prefixo `spring.datasource.hikari`:
- `spring.datasource.hikari.maximum-pool-size=10`
- `spring.datasource.hikari.minimum-idle=5`
- `spring.datasource.hikari.connection-timeout=20000`
- `spring.datasource.hikari.idle-timeout=300000`
- `spring.datasource.hikari.max-lifetime=1200000`
- `spring.datasource.hikari.pool-name=LouisHikariPool`

#### Scenario: Inicialização com valores padrão de pool
- **WHEN** a aplicação é inicializada em um perfil não-teste sem sobrescritas específicas do pool
- **THEN** o pool de conexões SHALL ser criado com `maximumPoolSize=10`, `minimumIdle=5`, `connectionTimeout=20000`, `idleTimeout=300000`, `maxLifetime=1200000` e `poolName=LouisHikariPool`

### Requirement: Aplicação efetiva das propriedades externalizadas ao DataSource com Secrets Manager
Quando o perfil ativo for diferente de `test`, a criação do `DataSource` da aplicação SHALL aplicar automaticamente as propriedades configuradas em `spring.datasource.hikari.*` ao pool instanciado, mantendo o driver, a URL JDBC, o usuário e a senha obtidos do provedor de segredos da aplicação.

#### Scenario: Sobrescrita de parâmetros do pool por ambiente
- **WHEN** propriedades `spring.datasource.hikari.*` (como `maximum-pool-size`, `minimum-idle` ou `connection-timeout`) são sobrescritas via arquivo de perfil ou variáveis de ambiente
- **THEN** o `DataSource` inicializado SHALL refletir os valores sobrescritos em seu pool de conexões
- **THEN** o driver, a URL JDBC, o usuário e a senha continuam sendo provenientes do provedor de segredos configurado

#### Scenario: Preservação de logs sem exposição de credenciais
- **WHEN** a aplicação inicializa o pool de conexões com as propriedades configuradas
- **THEN** credenciais de conexão (usuário e senha) SHALL NOT ser expostas nos parâmetros externalizados de `application.properties`

### Requirement: Compatibilidade e isolamento do perfil de testes
Quando o perfil `test` estiver ativo, a inicialização de testes automatizados SHALL permanecer funcional usando o banco em memória configurado para testes, sem exigir acesso ao provedor de segredos externo.

#### Scenario: Execução da suíte de testes com perfil test
- **WHEN** os testes automatizados são executados com o perfil `test` ativo
- **THEN** o contexto da aplicação SHALL inicializar normalmente sem consultar o AWS Secrets Manager
- **THEN** as propriedades definidas em `application.properties` não impedem a criação nem o fechamento do `DataSource` de testes
