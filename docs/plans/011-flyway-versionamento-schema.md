# 011 — Adicionar Flyway para Versionamento de Schema

**Sprint:** 🟡 3 — Preparação para Produção  
**Categoria:** DevOps / Banco de Dados  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Médio

**Commit:** `build: add Flyway for versioned schema migration`  
**Branch:** `build/11`

---

## Problema

O projeto depende exclusivamente do Hibernate DDL automático (`ddl-auto`) para criar e manter o schema do banco. Isso significa:

- Não há histórico de mudanças no schema
- Não é possível fazer rollback de uma alteração de banco com segurança
- Colaboradores não conseguem saber quais mudanças de schema foram feitas entre versões
- É impossível aplicar mudanças incrementais em staging/produção sem risco de perda de dados

---

## Solução

### 1. Adicionar dependência do Flyway ao `pom.xml`

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

> O Spring Boot gerencia a versão do Flyway automaticamente via BOM do `spring-boot-starter-parent`.

### 2. Alterar `ddl-auto` para `none`

Após o Flyway estar configurado, o Hibernate não deve mais gerenciar o schema:

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=none
```

### 3. Criar o script de migração inicial

Criar o diretório `src/main/resources/db/migration/` e o primeiro script:

**`V1__create_users_wallets_transactions_tables.sql`**

```sql
CREATE TABLE users (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    surname  VARCHAR(255) NOT NULL,
    document VARCHAR(255) NOT NULL UNIQUE,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    type     VARCHAR(50)  NOT NULL
);

CREATE TABLE wallets (
    id           BIGSERIAL PRIMARY KEY,
    amount       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_date TIMESTAMP,
    update_date  TIMESTAMP,
    user_id      BIGINT UNIQUE REFERENCES users(id)
);

CREATE TABLE transactions (
    id             BIGSERIAL PRIMARY KEY,
    amount         NUMERIC(12, 2) NOT NULL,
    timestamp      TIMESTAMP NOT NULL,
    transferring   BIGINT REFERENCES wallets(id),
    receiving      BIGINT REFERENCES wallets(id)
);
```

### 4. Configurar Flyway no `application.properties`

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

### 5. Ajustar profile de testes

Para os testes de integração, usar `create-drop` do Hibernate ou o suporte a `@Sql` do Spring Test para não depender do Flyway no ambiente de teste.

---

## Convenção de Nomenclatura

| Tipo | Prefixo | Exemplo |
|------|---------|---------|
| Versão | `V{n}__` | `V1__create_tables.sql` |
| Repetível | `R__` | `R__update_views.sql` |
| Undo (Pro) | `U{n}__` | `U1__undo_create_tables.sql` |

---

## Verificação

- [ ] Executar a aplicação e verificar que as tabelas são criadas pelo Flyway (não pelo Hibernate)
- [ ] Confirmar que a tabela `flyway_schema_history` existe no banco com o registro da migração V1
- [ ] Criar uma segunda migração `V2__` e verificar que é aplicada incrementalmente sem recriar tabelas
- [ ] Confirmar que os testes de integração ainda funcionam corretamente
