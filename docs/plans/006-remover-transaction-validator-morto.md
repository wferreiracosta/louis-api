# 006 — Remover `TransactionValidator` (Código Morto)

**Sprint:** 🟠 2 — Limpeza Técnica  
**Categoria:** Qualidade de Código  
**Impacto:** 🟡 Médio  
**Esforço:** ⏱️ Baixo

---

## Problema

[`TransactionValidator.java`](../../src/main/java/br/com/wferreiracosta/louis/annotations/validators/TransactionValidator.java) é um `ConstraintValidator` que **sempre retorna `true`**, sem realizar nenhuma validação real:

```java
@Override
public boolean isValid(final TransactionParameter parameter, final ConstraintValidatorContext context) {
    return true;
}
```

Apesar de não fazer nada, a classe injeta `UserRepository` (que nunca é usado) e existe uma anotação `@Transaction` associada aplicada sobre `TransactionParameter`. Isso:

- Engana quem lê o código, sugerindo que há validação onde não há
- Aumenta o tempo de bootstrap do Spring (registra um `ConstraintValidator` inútil)
- Gera dead import e dependência desnecessária do repositório na camada de validação

---

## Solução

### Opção A — Remover (recomendada)

1. Deletar [`TransactionValidator.java`](../../src/main/java/br/com/wferreiracosta/louis/annotations/validators/TransactionValidator.java)
2. Deletar a anotação [`@Transaction`](../../src/main/java/br/com/wferreiracosta/louis/annotations/Transaction.java)
3. Remover o uso da anotação em [`TransactionParameter`](../../src/main/java/br/com/wferreiracosta/louis/models/parameters/TransactionParameter.java)

### Opção B — Implementar validações reais

Mover para dentro do validator as validações declarativas que fazem sentido na camada de entrada, como:
- Verificar se `amount` é positivo (complementa as anotações JSR-303)
- Verificar se `payer != payee` (embora isso já seja coberto pelo plano [004](./004-bloquear-auto-transferencia.md) na camada de serviço)

---

## Verificação

- [ ] Confirmar que todos os testes passam após a remoção
- [ ] Verificar que `POST /transaction` ainda retorna erros de validação corretos para campos nulos/inválidos
- [ ] Garantir que nenhuma outra classe referencia `@Transaction` ou `TransactionValidator`
