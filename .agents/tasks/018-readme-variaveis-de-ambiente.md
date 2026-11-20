# 018 — Documentar Variáveis de Ambiente no README

**Sprint:** 🟠 2 — Limpeza Técnica  
**Categoria:** Documentação  
**Impacto:** 🟢 Baixo  
**Esforço:** ⏱️ Baixo

**Commit:** `docs: add environment variables reference to README`  
**Branch:** `docs/18`

---

## Problema

O [`README.md`](../../README.md) não possui nenhuma seção descrevendo as variáveis de ambiente necessárias para rodar o projeto. Ao seguir o guia de onboarding atual, o desenvolvedor não sabe:

- Quais variáveis de ambiente a aplicação consome
- Quais valores são obrigatórios vs. opcionais
- Como configurar o ambiente para rodar localmente com Docker vs. em cloud (AWS real)

Esse gap força o desenvolvedor a abrir o `application.properties` e inferir as configurações necessárias, aumentando o risco de erro de configuração.

---

## Solução

Adicionar uma seção **Environment Variables** ao `README.md` com uma tabela de referência completa.

### Arquivo: `README.md`

```markdown
## Environment Variables

### Variáveis AWS (Secrets Manager)

Estas variáveis controlam a integração com o AWS Secrets Manager, de onde a aplicação busca as credenciais do banco de dados no startup.

| Variável | Obrigatória | Valor padrão (lcl) | Descrição |
|---|---|---|---|
| `AWS_REGION` | Sim | `us-east-1` | Região AWS onde o secret está armazenado |
| `AWS_SECRETS_MANAGER_NAME` | Sim | `secret-rds-postgres-louis-use1-lcl` | Nome do secret no Secrets Manager |
| `AWS_URL` | Não (cloud) | `http://localhost:9090` | Endpoint customizado — usar para apontar para o ministack local. Deixar vazio em produção para usar o endpoint AWS real |
| `AWS_ACCESS_KEY_ID` | Não (cloud) | `access_key_id` | Access Key para autenticação no ministack. Deixar vazio em produção para usar `DefaultCredentialsProvider` (IAM Role) |
| `AWS_SECRET_ACCESS_KEY` | Não (cloud) | `secret_access_key` | Secret Key para autenticação no ministack. Deixar vazio em produção |

### Comportamento por ambiente

| Variável `AWS_URL` | Comportamento |
|---|---|
| Preenchida (ex.: `http://localhost:9090`) | Usa endpoint customizado com credenciais estáticas (ministack / LocalStack) |
| Vazia | Usa `DefaultCredentialsProvider` — resolve credenciais via IAM Role, variáveis de ambiente padrão da AWS ou `~/.aws/credentials` |

### Exemplo de `.env` para desenvolvimento local

```env
AWS_REGION=us-east-1
AWS_SECRETS_MANAGER_NAME=secret-rds-postgres-louis-use1-lcl
AWS_URL=http://localhost:9090
AWS_ACCESS_KEY_ID=access_key_id
AWS_SECRET_ACCESS_KEY=secret_access_key
SPRING_PROFILES_ACTIVE=lcl
```

> **Atenção:** Nunca commite arquivos `.env` com credenciais reais. O `.gitignore` já inclui `.env` por padrão.
```

---

## Verificação

- [ ] Confirmar que a tabela de variáveis está renderizando corretamente no GitHub
- [ ] Validar que todas as variáveis listadas correspondem às usadas em [`application.properties`](../../src/main/resources/application.properties) e [`AwsProperties.java`](../../src/main/java/br/com/wferreiracosta/louis/configs/properties/AwsProperties.java)
- [ ] Confirmar que `.env` está listado no `.gitignore`
- [ ] Executar `mvn clean test` para garantir que nenhuma regressão foi introduzida
