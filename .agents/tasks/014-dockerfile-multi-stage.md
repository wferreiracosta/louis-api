# 014 — Adicionar Dockerfile Multi-Stage

**Sprint:** 🟡 3 — Preparação para Produção  
**Categoria:** DevOps / Containerização  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

**Commit:** `build: add multi-stage Dockerfile`  
**Branch:** `build/14`

---

## Problema

O projeto não possui `Dockerfile`. A única forma de gerar uma imagem é via `mvn spring-boot:build-image` (Paketo Buildpacks), que é configurado no `pom.xml` mas depende de daemon Docker externo e não oferece controle sobre o processo de build, camadas da imagem ou usuário de execução.

Sem um `Dockerfile` explícito:

- Não é possível customizar a imagem (ex.: definir usuário não-root)
- O build é acoplado ao Maven Wrapper e ao ambiente local
- Pipelines de CI/CD não têm uma imagem padronizada para referenciar
- A separação entre camada de build e camada de runtime não está garantida

---

## Solução

Criar um `Dockerfile` multi-stage na raiz do projeto, separando a fase de compilação da fase de runtime.

### Arquivo: `Dockerfile`

```dockerfile
# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Usuário não-root para execução segura
RUN addgroup --system louis && adduser --system --ingroup louis louis
USER louis

COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Pontos-chave

| Decisão | Justificativa |
|---|---|
| `eclipse-temurin:17-jdk-jammy` no build | JDK completo necessário para o Maven compilar |
| `eclipse-temurin:17-jre-jammy` no runtime | JRE é menor (~200 MB a menos) e suficiente para rodar o JAR |
| `USER louis` não-root | Boas práticas de segurança em containers — nunca rodar como root |
| `dependency:go-offline` em layer separada | Cache de dependências Maven entre builds, evitando download a cada mudança de código |
| `-DskipTests` | Testes são executados em CI separado; o Dockerfile é apenas para gerar o artefato executável |

---

## Verificação

- [ ] Executar `docker build -t louis-api:local .` na raiz do projeto sem erros
- [ ] Executar `docker run --rm louis-api:local` e confirmar que a aplicação sobe (mesmo que falhe na conexão com o banco — o objetivo é validar a imagem)
- [ ] Confirmar com `docker inspect louis-api:local` que o usuário de execução não é `root`
- [ ] Confirmar que a imagem gerada é menor do que a imagem de build (stage 1 vs stage 2)
- [ ] Executar `mvn clean test` para garantir que nenhuma regressão foi introduzida
