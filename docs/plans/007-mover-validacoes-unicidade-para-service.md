# 007 — Mover Validações de Unicidade para `UserServiceImpl`

**Sprint:** 🟠 2 — Limpeza Técnica  
**Categoria:** Arquitetura / Qualidade de Código  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

---

## Problema

[`UserSaveValidator.java`](../../src/main/java/br/com/wferreiracosta/louis/annotations/validators/UserSaveValidator.java) realiza consultas diretas ao banco dentro de um `ConstraintValidator` Bean Validation:

```java
if (repository.findByDocument(parameter.document()).isPresent()) {
    list.add(new FieldMessage("document", "There is already a user registered with this document"));
}

if (repository.findByEmail(parameter.email()).isPresent()) {
    list.add(new FieldMessage("email", "There is already a user registered with this email"));
}
```

### Problemas desta abordagem

- **Fora do controle transacional**: as queries ocorrem antes da transação aberta pelo `@Transactional` do serviço, gerando um contexto de persistência separado
- **Dificulta testes unitários**: para testar o `UserParameter`, é necessário um `ApplicationContext` completo com banco (torna testes de unidade em testes de integração)
- **Violação de responsabilidade**: validadores Bean Validation devem validar apenas **formato e estrutura** dos dados, não regras de negócio que exigem I/O
- **Race condition**: a checagem de unicidade e a inserção ocorrem em transações diferentes — dois cadastros simultâneos com o mesmo documento podem passar pela validação antes de qualquer um ser persistido

---

## Solução

### 1. Simplificar `UserSaveValidator`

Manter apenas validações estruturais (sem I/O):

```java
@Override
public boolean isValid(final UserParameter parameter, final ConstraintValidatorContext context) {
    // Apenas validações de formato, sem acesso ao banco
    return true; // ou remover o validator por completo se não restar validações
}
```

### 2. Mover as checagens para `UserServiceImpl`

```java
@Override
@Transactional
public UserEntity save(final UserParameter parameter, final UserType type) {
    if (repository.findByDocument(parameter.document()).isPresent()) {
        throw new BusinessValidationException("document",
            "There is already a user registered with this document");
    }

    if (repository.findByEmail(parameter.email()).isPresent()) {
        throw new BusinessValidationException("email",
            "There is already a user registered with this email");
    }

    // ... criação e persistência do usuário
}
```

> **Nota:** A unicidade de `document` e `email` também está garantida por `@Column(unique = true)` no banco, servindo como segunda linha de defesa.

---

## Verificação

- [ ] Tentar cadastrar dois usuários com o mesmo documento e verificar HTTP 422
- [ ] Tentar cadastrar dois usuários com o mesmo e-mail e verificar HTTP 422
- [ ] Confirmar que testes unitários do serviço podem ser escritos sem contexto Spring completo
- [ ] Executar todos os testes existentes (`./mvnw test`)
