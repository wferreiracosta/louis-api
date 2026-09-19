## 1. Implementação da Ordenação de Locks

- [x] 1.1 Atualizar `TransactionServiceImpl.transfer` para determinar `firstId` e `secondId` via `Math.min` e `Math.max` e adquirir os locks com `walletService.findByUserIdWithLock` em ordem crescente de ID, reatribuindo para `payerWallet` e `payeeWallet`, verificando compilação bem-sucedida com `./mvnw test-compile`.

## 2. Testes e Validação

- [x] 2.1 Adicionar teste unitário em `TransactionServiceTest` cobrindo o cenário onde `payer.id < payee.id`, verificando que o valor é debitado da carteira do pagador e creditado na do recebedor com os saldos finais corretos via `./mvnw test -Dtest=TransactionServiceTest`.
- [x] 2.2 Adicionar teste unitário em `TransactionServiceTest` cobrindo o cenário onde `payer.id > payee.id`, verificando que o valor é debitado da carteira do pagador e creditado na do recebedor com os saldos finais corretos via `./mvnw test -Dtest=TransactionServiceTest`.
- [x] 2.3 Adicionar teste de concorrência em `TransactionConcurrencyTest` usando `ExecutorService` e `CountDownLatch` com banco H2 em memória (via `@SpringBootTest`), simulando múltiplas transferências simultâneas bidirecionais (A → B e B → A), verificando ausência de exceções e consistência dos saldos finais via `./mvnw test -Dtest=TransactionConcurrencyTest`.
- [x] 2.4 Executar a suíte de testes completa do projeto para certificar que todas as funcionalidades e testes existentes permanecem íntegros via `./mvnw test`.
