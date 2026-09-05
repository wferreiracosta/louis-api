# 001 — Adicionar `@Builder.Default` nas listas de `WalletEntity`

**Sprint:** 🔴 1 — Bugs Críticos  
**Categoria:** Correção de Bug  
**Impacto:** 🔴 Crítico  
**Esforço:** ⏱️ Baixo

---

## Problema

Em [`WalletEntity.java`](../../src/main/java/br/com/wferreiracosta/louis/models/entities/WalletEntity.java) as listas `transferring` e `receiving` são declaradas com inicialização direta:

```java
@OneToMany(mappedBy = "transferring")
private List<TransactionEntity> transferring = new ArrayList<>();

@OneToMany(mappedBy = "receiving")
private List<TransactionEntity> receiving = new ArrayList<>();
```

O Lombok `@Builder` **ignora a inicialização inline** (`= new ArrayList<>()`) ao construir via builder. As listas ficam `null` em vez de uma lista vazia.

Isso causa `NullPointerException` em [`TransactionServiceImpl.java`](../../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java) nas linhas:

```java
payerWallet.getTransferring().add(transactionSaved); // NPE
payeeWallet.getReceiving().add(transactionSaved);    // NPE
```

Toda transferência que passe pelo builder da entidade falha silenciosamente em produção.

---

## Solução

Adicionar a anotação `@Builder.Default` em ambas as listas para que o Lombok respeite o valor padrão durante a construção via builder.

### Arquivo: `WalletEntity.java`

```java
@Builder.Default
@JsonManagedReference
@OneToMany(mappedBy = "transferring")
private List<TransactionEntity> transferring = new ArrayList<>();

@Builder.Default
@JsonManagedReference
@OneToMany(mappedBy = "receiving")
private List<TransactionEntity> receiving = new ArrayList<>();
```

---

## Verificação

- [ ] Executar os testes existentes (`./mvnw test`)
- [ ] Realizar uma transferência via Swagger UI e verificar que nenhum `NullPointerException` é lançado
- [ ] Confirmar que os registros de `TransactionEntity` são corretamente associados às wallets
