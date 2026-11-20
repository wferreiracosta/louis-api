## Context

Consulte `proposal.md` para a motivação da mudança.
Atualmente, `DataSourceConfig` (`@Profile("!test")`) declara um `@Bean DataSource dataSource()` que instancia diretamente `new HikariDataSource()` e popula apenas `driverClassName`, `jdbcUrl`, `username` e `password` a partir de `SecretsManagerProperties`. Como o `DataSource` é criado programaticamente por um `@Bean` explícito (e não pela autoconfiguração padrão `DataSourceAutoConfiguration`), o Spring Boot não aplica automaticamente propriedades `spring.datasource.hikari.*` de `application.properties` a menos que haja binding explícito via `@ConfigurationProperties`.

## Goals / Non-Goals

**Goals:**
- Externalizar os parâmetros padrão do pool HikariCP em `src/main/resources/application.properties` (`maximum-pool-size=10`, `minimum-idle=5`, `connection-timeout=20000`, `idle-timeout=300000`, `max-lifetime=1200000`, `pool-name=LouisHikariPool`).
- Garantir o binding das propriedades `spring.datasource.hikari.*` no `HikariDataSource` criado em `DataSourceConfig` (`@Profile("!test")`).
- Permitir ajuste fino por ambiente (`lcl`, `qa`, `prd`) via arquivos de perfil ou variáveis de ambiente (`SPRING_DATASOURCE_HIKARI_*`) sem recompilação.
- Validar por testes automatizados que o `HikariDataSource` recebe os parâmetros padrão e sobrescritas de `spring.datasource.hikari.*`, mantendo o comportamento do perfil `test`.

**Non-Goals:**
- Remover a integração com AWS Secrets Manager ou substituir `DataSourceConfig` pela autoconfiguração padrão de `spring.datasource.url/username/password`.
- Alterar valores de configuração específicos nos arquivos `application-lcl.properties`, `application-qa.properties` ou `application-prd.properties` neste momento (apenas os padrões em `application.properties`).
- Habilitar `logging.level.com.zaxxer.hikari=DEBUG` permanentemente em produção (uso restrito à verificação local/diagnóstico, conforme checklist do plano 012).
- Executar testes de carga em ambiente de produção ou alterar `max_connections` do PostgreSQL RDS.

## Decisions

### Decisão 1: Binding via `@ConfigurationProperties(prefix = "spring.datasource.hikari")` no `DataSourceConfig`

- **Opção escolhida:** Manter a construção do `HikariDataSource` em `DataSourceConfig.dataSource()` usando `SecretsManagerProperties` para conexão (`driverClassName`, `jdbcUrl`, `username`, `password`) e aplicar `@ConfigurationProperties(prefix = "spring.datasource.hikari")` para vincular as propriedades do pool ao `HikariDataSource`.
- **Alternativas consideradas:**
  - *Apenas `application.properties` sem alterar `DataSourceConfig.java` (Opção A pura do plano):* Não surtiria efeito nos perfis `!test`, pois o `@Bean` manual em `DataSourceConfig` impede o binding automático da autoconfiguração do Spring Boot.
  - *Valores hardcoded em `DataSourceConfig.java` (Opção B do plano):* Funciona imediatamente, mas impede ajustar `maximumPoolSize`, `minimumIdle` e timeouts por ambiente (`qa`/`prd`, número de instâncias, `max_connections` do PostgreSQL) sem recompilar e reimplantar a aplicação.
- **Racional:** Combina a flexibilidade operacional de `application.properties` / variáveis de ambiente com a obtenção segura de credenciais via AWS Secrets Manager. O Spring Boot realiza o binding dos setters compatíveis de `HikariDataSource` (que estende `HikariConfig`) após a criação do bean.

### Decisão 2: Centralizar os valores padrão em `application.properties`

- **Opção escolhida:** Declarar os seis parâmetros (`maximum-pool-size=10`, `minimum-idle=5`, `connection-timeout=20000`, `idle-timeout=300000`, `max-lifetime=1200000`, `pool-name=LouisHikariPool`) em `src/main/resources/application.properties`.
- **Alternativas consideradas:** Duplicar as propriedades em `application-lcl.properties`, `application-qa.properties` e `application-prd.properties`.
- **Racional:** Evita duplicação quando os valores base são idênticos e mantém a precedência nativa do Spring Boot para futuros overrides por perfil ou variáveis de ambiente.

### Decisão 3: Isolamento do perfil `test` e verificação automatizada

- **Opção escolhida:** Manter `DataSourceConfig` restrito a `@Profile("!test")` e adicionar testes automatizados de contexto Spring (usando `SpringApplicationBuilder` ou `@SpringBootTest`/context runner com mock de `SecretsManagerProperties` e H2, sem AWS real) para verificar que `HikariDataSource` recebe os valores padrão de `application.properties` e respeita sobrescritas de `spring.datasource.hikari.*`.
- **Racional:** Permite validação determinística em CI/CD via `mvn test` sem depender de LocalStack ou PostgreSQL ativo, além de garantir que a suíte existente com `@ActiveProfiles("test")` continue passando.

## Risks / Trade-offs

- **[Risco] Propriedades `spring.datasource.hikari.*` em `application.properties` também se aplicam ao datasource H2 do perfil `test` caso o Spring Boot autoconfigure `HikariDataSource`.**
  → **Mitigação:** Executar toda a suíte `mvn test` durante a implementação para confirmar que `maximum-pool-size=10`, `minimum-idle=5` e `pool-name=LouisHikariPool` são compatíveis com os testes H2 (inclusive testes de concorrência); se houver conflito de `pool-name` entre múltiplos contextos em paralelo no mesmo JVM, avaliar override em `application-test.properties` ou `register-mbeans=false`.
- **[Risco] Sobrescrita acidental de `jdbc-url`, `username` ou `password` via `spring.datasource.hikari.*` se algum ambiente definir essas chaves.**
  → **Mitigação:** Não definir credenciais nem `jdbc-url` em `application*.properties`; documentar que apenas parâmetros de dimensionamento e timeout do pool pertencem ao bloco `spring.datasource.hikari.*` enquanto credenciais vêm exclusivamente do Secrets Manager.
- **[Risco] `maxLifetime` (20 min) próximo ou superior a timeouts de firewall/RDS em determinados ambientes.**
  → **Mitigação:** O valor padrão de `1200000` ms (20 min) é inferior ao padrão do Hikari (30 min) e ao `wait_timeout`/idle timeouts comuns, podendo ser ajustado por variável de ambiente sem alteração de código.
