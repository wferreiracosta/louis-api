# 017 — Documentar Stack Completa no README

**Sprint:** 🟠 2 — Limpeza Técnica  
**Categoria:** Documentação  
**Impacto:** 🟢 Baixo  
**Esforço:** ⏱️ Baixo

**Commit:** `docs: add full tech stack section to README`  
**Branch:** `docs/17`

---

## Problema

O [`README.md`](../../README.md) menciona Java, Spring Boot, Docker e PostgreSQL na seção "About", mas **omite o AWS Secrets Manager**, que é um componente central da arquitetura de segurança da aplicação.

Qualquer desenvolvedor que leia o README para entender a stack tecnológica do projeto não saberá que:

- As credenciais do banco são gerenciadas externamente via AWS Secrets Manager
- O ambiente local usa o ministack (LocalStack) como substituto do AWS real
- É necessário inicializar o secret no ministack antes de rodar a aplicação

---

## Solução

Adicionar uma seção **Tech Stack** explícita ao `README.md`, listando todas as tecnologias utilizadas.

### Arquivo: `README.md`

```markdown
## Tech Stack

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.1.5 | Framework de aplicação |
| Spring Data JPA | (gerenciado) | Persistência e mapeamento ORM |
| Spring Validation | (gerenciado) | Validação de entrada |
| PostgreSQL | latest | Banco de dados relacional |
| Flyway | (gerenciado) | Versionamento de schema |
| AWS Secrets Manager | SDK v2.20.x | Gerenciamento de credenciais em cloud |
| Ministack (LocalStack) | latest | Emulação local do AWS Secrets Manager |
| Docker | — | Containerização da aplicação e dependências |
| Docker Compose | — | Orquestração local dos serviços |
| SpringDoc OpenAPI | 2.0.3 | Documentação automática da API (Swagger UI) |
| Lombok | (gerenciado) | Redução de boilerplate |
```

---

## Verificação

- [ ] Confirmar que a tabela de stack está renderizando corretamente no GitHub
- [ ] Revisar se todas as versões principais estão corretas conforme o `pom.xml`
- [ ] Garantir que AWS Secrets Manager e Ministack estão explicitamente listados
- [ ] Executar `mvn clean test` para garantir que nenhuma regressão foi introduzida
