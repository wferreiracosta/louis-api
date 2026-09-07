## 1. Correção de WalletEntity

- [x] 1.1 Adicionar `@Builder.Default` no campo `transferring` de `WalletEntity` e verificar que o campo é marcado com a anotação no arquivo `WalletEntity.java`
- [x] 1.2 Adicionar `@Builder.Default` no campo `receiving` de `WalletEntity` e verificar que o campo é marcado com a anotação no arquivo `WalletEntity.java`

## 2. Verificação

- [x] 2.1 Executar `./mvnw test` e verificar que todos os testes passam sem `NullPointerException`
- [x] 2.2 Realizar uma transferência via Swagger UI (ou teste de integração) e confirmar que `payerWallet.getTransferring().add(...)` e `payeeWallet.getReceiving().add(...)` completam sem erro
- [x] 2.3 Confirmar que instâncias de `WalletEntity` criadas via `WalletEntity.builder().build()` retornam listas não-nulas para `getTransferring()` e `getReceiving()`
