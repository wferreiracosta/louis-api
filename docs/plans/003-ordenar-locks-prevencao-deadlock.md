# 003 — Ordenar Locks Pessimistas para Prevenir Deadlock

**Sprint:** 🔴 1 — Bugs Críticos  
**Categoria:** Concorrência  
**Impacto:** 🔴 Crítico  
**Esforço:** ⏱️ Baixo

**Commit:** `fix: order pessimistic locks to prevent deadlock`  
**Branch:** `fix/3`

---

## Problema

Em [`TransactionServiceImpl.java`](../../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java), os locks pessimistas são adquiridos na ordem em que os IDs chegam via parâmetro:

```java
final var payerWallet = walletService.findByUserIdWithLock(parameter.payer());
final var payeeWallet = walletService.findByUserIdWithLock(parameter.payee());
```

### Cenário de Deadlock

| Momento | Thread 1 (A → B) | Thread 2 (B → A) |
|---------|-----------------|-----------------|
| t1 | 🔒 Bloqueia carteira A | 🔒 Bloqueia carteira B |
| t2 | ⏳ Aguarda carteira B | ⏳ Aguarda carteira A |
| t3 | 💀 **DEADLOCK** | 💀 **DEADLOCK** |

O banco de dados (PostgreSQL) detecta o deadlock e cancela uma das transações com erro, degradando a experiência do usuário e potencialmente deixando dados inconsistentes dependendo do ponto de rollback.

---

## Solução

Adquirir sempre os locks em **ordem crescente de ID**, garantindo que duas threads concorrentes nunca aguardem uma pela outra.

### Arquivo: `TransactionServiceImpl.java`

```java
// Garantir ordem determinística para evitar deadlock
final Long firstId  = Math.min(parameter.payer(), parameter.payee());
final Long secondId = Math.max(parameter.payer(), parameter.payee());

// Adquire locks na ordem crescente de ID
final var firstWallet  = walletService.findByUserIdWithLock(firstId);
final var secondWallet = walletService.findByUserIdWithLock(secondId);

// Recupera as entidades corretas por papel (payer/payee)
final var payerWallet = firstId.equals(parameter.payer()) ? firstWallet : secondWallet;
final var payeeWallet = firstId.equals(parameter.payee()) ? firstWallet : secondWallet;
```

---

## Verificação

- [ ] Executar teste de carga com transferências simultâneas A→B e B→A
- [ ] Confirmar ausência de erros de deadlock nos logs do PostgreSQL
- [ ] Garantir que o saldo final das carteiras está correto após execuções paralelas
- [ ] Adicionar teste unitário cobrindo o cenário de transferência bidirecional
