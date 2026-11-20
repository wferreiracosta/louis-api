## 1. Externalização das Propriedades do Pool HikariCP

- [x] 1.1 Adicionar as propriedades padrão `spring.datasource.hikari.*` (`maximum-pool-size=10`, `minimum-idle=5`, `connection-timeout=20000`, `idle-timeout=300000`, `max-lifetime=1200000`, `pool-name=LouisHikariPool`) em `src/main/resources/application.properties` e verificar com `mvn process-resources` que `target/classes/application.properties` contém todas as chaves esperadas sem credenciais expostas.

## 2. Binding no DataSourceConfig

- [x] 2.1 Atualizar `src/main/java/br/com/wferreiracosta/louis/configs/DataSourceConfig.java` para aplicar `@ConfigurationProperties(prefix = "spring.datasource.hikari")` ao bean `HikariDataSource` preservando `driverClassName`, `jdbcUrl`, `username` e `password` obtidos de `SecretsManagerProperties`, e verificar a compilação com `mvn test-compile`.

## 3. Testes Automatizados e Validação

- [x] 3.1 Criar teste automatizado em `src/test/java/br/com/wferreiracosta/louis/configs/DataSourceConfigTest.java` validando (com `SecretsManagerProperties` simulado e H2) que o `HikariDataSource` criado em perfil `!test` recebe os seis parâmetros padrão de `application.properties`, aceita sobrescritas de `spring.datasource.hikari.*` e preserva `jdbcUrl`, `driverClassName`, `username` e `password` do Secrets Manager; verificar executando `mvn test -Dtest=DataSourceConfigTest`.
- [x] 3.2 Executar a suíte completa de testes (`mvn test`) para confirmar que todos os testes passam sem regressão ou conflitos com o perfil `test`.
