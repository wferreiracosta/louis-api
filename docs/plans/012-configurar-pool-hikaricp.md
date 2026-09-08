# 012 — Configurar Pool de Conexões HikariCP

**Sprint:** 🟡 3 — Preparação para Produção  
**Categoria:** Performance / Infraestrutura  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

**Commit:** `perf: configure HikariCP connection pool settings`  
**Branch:** `perf/12`

---

## Problema

Em [`DataSourceConfig.java`](../../src/main/java/br/com/wferreiracosta/louis/configs/DataSourceConfig.java), o `HikariDataSource` é criado apenas com as credenciais de conexão:

```java
final var dataSource = new HikariDataSource();
dataSource.setDriverClassName(properties.driverClassName());
dataSource.setJdbcUrl(url);
dataSource.setUsername(properties.username());
dataSource.setPassword(properties.password());
```

Sem configurar os parâmetros do pool, o Hikari opera com **valores padrão** que podem não ser adequados para a carga esperada em produção:

| Parâmetro | Padrão Hikari | Risco |
|-----------|--------------|-------|
| `maximumPoolSize` | 10 | Pode ser insuficiente ou excessivo |
| `minimumIdle` | igual ao `maximumPoolSize` | Pool sempre cheio, mesmo em baixa demanda |
| `connectionTimeout` | 30.000ms (30s) | Usuário espera 30s antes de receber erro |
| `idleTimeout` | 600.000ms (10min) | Conexões ociosas mantidas por muito tempo |
| `maxLifetime` | 1.800.000ms (30min) | Pode exceder `wait_timeout` do PostgreSQL |

---

## Solução

### Opção A — Via `application.properties` (recomendada)

Externalizar a configuração para o `application.properties`, facilitando ajustes por ambiente sem recompilar:

```properties
# HikariCP
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.pool-name=LouisHikariPool
```

### Opção B — Via `DataSourceConfig.java`

Se o `DataSource` é criado programaticamente (como é o caso atual), configurar os parâmetros diretamente:

```java
@Bean
public DataSource dataSource() {
    final var url = format("jdbc:%s://%s:%s/%s",
            properties.engine(), properties.host(),
            properties.port(), properties.database());

    final var dataSource = new HikariDataSource();
    dataSource.setDriverClassName(properties.driverClassName());
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(properties.username());
    dataSource.setPassword(properties.password());

    // Pool configuration
    dataSource.setMaximumPoolSize(10);
    dataSource.setMinimumIdle(5);
    dataSource.setConnectionTimeout(20_000);
    dataSource.setIdleTimeout(300_000);
    dataSource.setMaxLifetime(1_200_000);
    dataSource.setPoolName("LouisHikariPool");

    return dataSource;
}
```

---

## Referência de Valores

Os valores devem ser ajustados com base em:
- Número de instâncias da aplicação
- `max_connections` configurado no PostgreSQL (`SHOW max_connections;`)
- Fórmula recomendada pelo Hikari: `maximumPoolSize = (núcleos_cpu * 2) + disco_efetivo`

---

## Verificação

- [ ] Confirmar via logs do Hikari (`logging.level.com.zaxxer.hikari=DEBUG`) que o pool está sendo criado com os parâmetros corretos
- [ ] Executar teste de carga e verificar que não ocorrem timeouts de aquisição de conexão
- [ ] Confirmar que o pool não excede `max_connections` do PostgreSQL
