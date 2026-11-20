# 015 — Incluir Serviço da Aplicação no Docker Compose

**Sprint:** 🟡 3 — Preparação para Produção  
**Categoria:** DevOps / Containerização  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

**Commit:** `build: add louis-api service to docker-compose`  
**Branch:** `build/15`

> **Pré-requisito:** Este plano depende do plano [014](./014-dockerfile-multi-stage.md) (Dockerfile) estar implementado.

---

## Problema

O [`docker-compose.yaml`](../../docker-compose.yaml) atual sobe apenas o `postgres` e o `ministack` (LocalStack), mas **não inclui o serviço da aplicação `louis-api`**. Para subir o ambiente completo, o desenvolvedor precisa:

1. Rodar `docker compose up` para subir o banco e o LocalStack
2. Inicializar manualmente o secret no ministack (`sh init-aws.sh`)
3. Iniciar a aplicação separadamente via IDE ou `mvn spring-boot:run`

Isso viola o critério de "subir tudo com um único comando `docker compose up`" e aumenta a fricção no onboarding de novos desenvolvedores.

---

## Solução

Adicionar o serviço `louis-api` ao `docker-compose.yaml`, com dependências corretas e variáveis de ambiente mapeadas.

### Arquivo: `docker-compose.yaml`

```yaml
version: '3'

volumes:
  data:

services:

  postgres:
    container_name: louis-api-postgres
    image: postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
      POSTGRES_DB: louisdb
    ports:
      - 5432:5432
    volumes:
      - data:/var/lib/postgresql
    networks:
      - global-default
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  ministack:
    container_name: louis-api-ministack
    image: ministackorg/ministack
    networks:
      - global-default
    ports:
      - '9090:4566'
    volumes:
      - ./aws/init-aws.sh:/etc/ministack/init/ready.d/init-aws.sh
    environment:
      - SERVICES=s3,sqs,secretsmanager
      - DOCKER_HOST=unix:///var/run/docker.sock
      - AWS_ACCESS_KEY_ID=access_key_id
      - AWS_SECRET_ACCESS_KEY=secret_access_key
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:4566/_localstack/health"]
      interval: 10s
      timeout: 5s
      retries: 10

  louis-api:
    container_name: louis-api
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - '8080:8080'
    environment:
      - SPRING_PROFILES_ACTIVE=lcl
      - AWS_REGION=us-east-1
      - AWS_URL=http://ministack:4566
      - AWS_ACCESS_KEY_ID=access_key_id
      - AWS_SECRET_ACCESS_KEY=secret_access_key
      - AWS_SECRETS_MANAGER_NAME=secret-rds-postgres-louis-use1-lcl
    depends_on:
      postgres:
        condition: service_healthy
      ministack:
        condition: service_healthy
    networks:
      - global-default

networks:
  global-default:
    external: true
```

### Pontos-chave

| Decisão | Justificativa |
|---|---|
| `depends_on` com `condition: service_healthy` | Garante que a aplicação só sobe após postgres e ministack estarem prontos |
| `healthcheck` no postgres | Evita race condition onde a aplicação tenta conectar antes do banco aceitar conexões |
| `AWS_URL=http://ministack:4566` | Dentro da rede Docker, o hostname do serviço substitui `localhost` |
| `SPRING_PROFILES_ACTIVE=lcl` | Ativa o perfil local com `ddl-auto=update` |

---

## Verificação

- [ ] Executar `docker compose up --build` e confirmar que os três serviços sobem sem erros
- [ ] Confirmar que `http://localhost:8080/swagger-ui/index.html` está acessível após o compose subir
- [ ] Realizar uma transferência via Swagger ou `curl` e confirmar persistência no banco
- [ ] Executar `docker compose down` e confirmar que todos os containers são removidos corretamente
- [ ] Executar `mvn clean test` para garantir que nenhuma regressão foi introduzida
