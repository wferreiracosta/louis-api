# 016 — Adicionar Healthcheck com Spring Actuator

**Sprint:** 🟡 3 — Preparação para Produção  
**Categoria:** Observabilidade / DevOps  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

**Commit:** `feat: add actuator healthcheck endpoint`  
**Branch:** `feat/16`

---

## Problema

A aplicação não expõe nenhum endpoint de saúde (`/health` ou `/actuator/health`). Isso impede:

- **Orquestradores** (Docker, Kubernetes) de saber se o container está pronto para receber tráfego
- **Load balancers** de remover instâncias não-saudáveis do pool
- **Pipelines de CI/CD** de validar automaticamente se um deploy subiu com sucesso
- **Monitoração** de detectar degradação de conectividade com o banco de dados

Atualmente, o `docker-compose.yaml` não consegue definir um `healthcheck` para o serviço `louis-api` porque não há endpoint de saúde disponível.

---

## Solução

### 1. Adicionar dependência do Actuator ao `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

> O Spring Boot gerencia a versão do Actuator automaticamente via BOM do `spring-boot-starter-parent`.

### 2. Configurar exposição do endpoint no `application.properties`

```properties
# Actuator
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always
management.endpoint.health.probes.enabled=true
```

- `exposure.include=health` — expõe apenas o endpoint de saúde (sem expor `env`, `beans`, `metrics` desnecessariamente)
- `show-details=always` — retorna detalhes do estado do banco de dados e outros indicadores
- `probes.enabled=true` — habilita `/actuator/health/liveness` e `/actuator/health/readiness` para uso em Kubernetes

### 3. Exemplo de resposta esperada

```json
GET /actuator/health

{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### 4. Atualizar `docker-compose.yaml` com healthcheck da aplicação

Após o Actuator estar disponível, o serviço `louis-api` no Compose pode se auto-verificar:

```yaml
louis-api:
  # ...
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 15s
    timeout: 5s
    retries: 5
    start_period: 30s
```

---

## Verificação

- [ ] Acessar `http://localhost:8080/actuator/health` e confirmar resposta `{"status":"UP"}`
- [ ] Confirmar que o componente `db` aparece na resposta com `status: UP`
- [ ] Derrubar o container do PostgreSQL e confirmar que o health retorna `{"status":"DOWN"}`
- [ ] Confirmar que nenhum outro endpoint do Actuator está exposto (`/actuator/beans`, `/actuator/env`, etc.)
- [ ] Executar `mvn clean test` para garantir que nenhuma regressão foi introduzida
