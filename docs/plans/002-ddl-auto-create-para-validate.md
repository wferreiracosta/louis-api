# 002 — Alterar `ddl-auto=create` para `validate`

**Sprint:** 🔴 1 — Bugs Críticos  
**Categoria:** Banco de Dados  
**Impacto:** 🔴 Crítico  
**Esforço:** ⏱️ Baixo

---

## Problema

Em [`application.properties`](../../src/main/resources/application.properties):

```properties
spring.jpa.hibernate.ddl-auto=create
```

A estratégia `create` instrui o Hibernate a **remover e recriar todas as tabelas** a cada reinicialização da aplicação. Em um ambiente compartilhado (staging, produção) isso resulta em **perda total dos dados** ao fazer deploy ou reiniciar o serviço.

---

## Solução

Alterar a propriedade para `validate`, que verifica se o schema do banco está consistente com as entidades JPA sem fazer nenhuma modificação.

> **Nota:** A estratégia `validate` é o ponto de partida seguro. Após a adição do Flyway (plano [011](./011-flyway-versionamento-schema.md)), a propriedade pode ser definida como `none`, delegando totalmente o controle de schema ao Flyway.

### Arquivo: `application.properties`

```properties
# Antes
spring.jpa.hibernate.ddl-auto=create

# Depois
spring.jpa.hibernate.ddl-auto=validate
```

---

## Ambientes

| Ambiente | Valor recomendado |
|----------|-------------------|
| Local (dev) | `create-drop` ou `update` |
| CI / Teste | `create-drop` (via profile `test`) |
| Staging / Produção | `validate` ou `none` (após Flyway) |

Considerar externalizar via Spring Profiles (`application-prod.properties`, `application-test.properties`) para não alterar o comportamento local de desenvolvimento.

---

## Verificação

- [ ] Reiniciar a aplicação e confirmar que os dados persistem entre reinicializações
- [ ] Garantir que o profile `test` ainda usa `create-drop` para não quebrar os testes de integração
