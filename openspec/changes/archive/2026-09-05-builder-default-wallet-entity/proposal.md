## Why

As listas `transferring` e `receiving` em `WalletEntity` são declaradas com inicialização inline (`= new ArrayList<>()`), mas o Lombok `@Builder` ignora esse valor ao construir instâncias via builder — resultando em campos `null`. Isso provoca `NullPointerException` em `TransactionServiceImpl` toda vez que uma transferência é processada pela entidade construída via builder, causando falha crítica em produção.

## What Changes

- Adição de `@Builder.Default` nos campos `transferring` e `receiving` de `WalletEntity` para que o Lombok respeite a inicialização padrão durante a construção via builder.
- Nenhuma alteração de API, contrato de dados ou comportamento externamente observável além da eliminação do NPE.

## Capabilities

### New Capabilities

_(nenhuma — esta mudança não introduz capacidades novas)_

### Modified Capabilities

- `wallet/transaction-lists`: O comportamento de inicialização das listas `transferring` e `receiving` em `WalletEntity` muda de `null` (quando construída via builder) para `[]` (lista vazia), tornando o contrato do objeto consistente independente da forma de construção.

## Impact

- **Arquivo afetado:** `src/main/java/br/com/wferreiracosta/louis/models/entities/WalletEntity.java`
- **Serviço impactado:** `TransactionServiceImpl` — as chamadas `.add()` sobre as listas deixarão de lançar NPE.
- **Sem impacto em API pública**, endpoints REST ou schema de banco de dados.
- **Sem impacto em serialização JSON** — `@JsonManagedReference` é mantido; uma lista vazia serializa da mesma forma que antes em entidades criadas sem builder.
