# Convenção de Commits

> Referência: [Conventional Commits Pattern](https://medium.com/linkapi-solutions/conventional-commits-pattern-3778d1a1e657)

---

## Formato

```
<tipo>: <mensagem>
```

**Exemplo:**

```
test: add test for create product automation
```

---

## Tipos

| Tipo | Quando usar | Exemplo |
|---|---|---|
| `test` | Criação ou alteração de códigos de teste | `test: add unit tests for TransactionService` |
| `feat` | Desenvolvimento de uma nova feature (serviço, funcionalidade, endpoint) | `feat: add actuator healthcheck endpoint` |
| `refactor` | Refatoração de código sem impacto em regras de negócio | `refactor: move uniqueness validations to UserServiceImpl` |
| `style` | Mudanças de formatação e estilo que não alteram comportamento (indentação, espaços, comentários) | `style: remove trailing whitespace from entity classes` |
| `fix` | Correção de erros que geram bugs no sistema | `fix: prevent deadlock by ordering pessimistic locks` |
| `chore` | Mudanças no projeto que não afetam o sistema nem testes (configs de lint, .gitignore, etc.) | `chore: add .env to .gitignore` |
| `docs` | Mudanças na documentação (README, Swagger, comentários de API) | `docs: add environment variables reference to README` |
| `build` | Mudanças no processo de build ou dependências externas (Dockerfile, pom.xml, Compose) | `build: add multi-stage Dockerfile` |
| `perf` | Alterações que melhoram a performance do sistema | `perf: configure HikariCP connection pool settings` |
| `ci` | Mudanças em arquivos de configuração de CI/CD (GitHub Actions, Travis, etc.) | `ci: add workflow to run tests on pull request` |
| `revert` | Reversão de um commit anterior | `revert: revert "feat: add wallet endpoint"` |

---

## Regras

- A mensagem deve ser escrita em **inglês**, no **infinitivo** (imperativo), com letra minúscula
- Não termine a mensagem com ponto final
- Seja objetivo e descritivo — máximo de 72 caracteres na mensagem
- O tipo deve refletir a **natureza real da mudança**, não o arquivo alterado
