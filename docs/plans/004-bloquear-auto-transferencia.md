# 004 — Bloquear Auto-Transferência (Self-Transfer)

**Sprint:** 🔴 1 — Bugs Críticos  
**Categoria:** Regra de Negócio  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

---

## Problema

Em [`TransactionServiceImpl.java`](../../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java) não há nenhuma validação que impeça `payer` e `payee` de serem o mesmo usuário.

### Consequências

- **Lock duplo no mesmo ID**: com a solução do plano [003](./003-ordenar-locks-prevencao-deadlock.md), `Math.min` e `Math.max` produziriam o mesmo ID, resultando em dois locks no mesmo row — comportamento undefined no PostgreSQL.
- **Semântica inválida**: uma transferência de A para A não tem sentido de negócio e não deveria ser aceita pela API.
- **Histórico poluído**: gera registros de transação que debitam e creditam o mesmo saldo sem efeito líquido.

---

## Solução

Adicionar validação no início do método `transfer`, antes de qualquer consulta ao banco.

### Arquivo: `TransactionServiceImpl.java`

```java
@Override
@Transactional
public TransactionDTO transfer(final TransactionParameter parameter) {
    final var payerField = "payer";

    // Primeira verificação — deve ocorrer antes de qualquer acesso ao banco
    if (parameter.payer().equals(parameter.payee())) {
        throw new BusinessValidationException(payerField, "Self-transfer is not allowed");
    }

    // ... restante do método inalterado
}
```

---

## Verificação

- [ ] Chamar `POST /transaction` com `payer` e `payee` idênticos e verificar retorno HTTP 422 com mensagem `"Self-transfer is not allowed"`
- [ ] Confirmar que nenhum registro é criado no banco para requisições inválidas
- [ ] Adicionar teste unitário para o caso de auto-transferência
