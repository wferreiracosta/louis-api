# 005 — Adicionar `@UpdateTimestamp` em `WalletEntity.updateDate`

**Sprint:** 🔴 1 — Bugs Críticos  
**Categoria:** Concorrência / Auditoria  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

---

## Problema

Em [`WalletEntity.java`](../../src/main/java/br/com/wferreiracosta/louis/models/entities/WalletEntity.java), o campo `updateDate` é declarado como um simples atributo sem nenhuma anotação de ciclo de vida do Hibernate:

```java
private LocalDateTime createdDate;
private LocalDateTime updateDate;
```

Ao realizar uma transferência, [`TransactionServiceImpl.java`](../../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java) atualiza o saldo via `setAmount()` e persiste com `walletService.update()`, mas **`updateDate` nunca é modificado**. O campo fica `null` ou com o valor da criação da carteira para sempre.

Isso compromete:
- Rastreabilidade de quando a carteira foi modificada pela última vez
- Auditorias e relatórios financeiros
- Debugging de inconsistências de saldo

---

## Solução

Adicionar `@UpdateTimestamp` no campo `updateDate` e `@CreationTimestamp` no campo `createdDate`. O Hibernate passa a gerenciar esses valores automaticamente.

### Arquivo: `WalletEntity.java`

```java
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// ...

@CreationTimestamp
private LocalDateTime createdDate;

@UpdateTimestamp
private LocalDateTime updateDate;
```

> **Alternativa moderna (Hibernate 6+):** Usar `@CurrentTimestamp(event = INSERT)` e `@CurrentTimestamp(event = UPDATE)` que são mais explícitas quanto ao ciclo de vida.

---

## Verificação

- [ ] Criar uma carteira e verificar que `createdDate` é preenchido automaticamente na inserção
- [ ] Realizar uma transferência e verificar que `updateDate` das carteiras do pagador e recebedor é atualizado
- [ ] Confirmar via banco que `updateDate` difere de `createdDate` após a primeira transferência
