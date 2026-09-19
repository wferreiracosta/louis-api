## Why

Atualmente, em `TransactionServiceImpl`, os locks pessimistas sobre as carteiras dos usuários são adquiridos na ordem em que os IDs chegam via parâmetro (`parameter.payer()` e depois `parameter.payee()`). Quando duas transações concorrentes ocorrem entre os mesmos usuários em direções opostas (por exemplo, usuário A transferindo para B e usuário B transferindo para A simultaneamente), a primeira transação bloqueia a carteira A e aguarda pela carteira B, enquanto a segunda transação bloqueia a carteira B e aguarda pela carteira A, resultando em deadlock no banco de dados.

Adquirir os locks pessimistas em ordem determinística (ordem crescente de ID) elimina a espera circular entre transações concorrentes e previne a ocorrência de deadlocks nesse cenário.

## What Changes

- Modificar `TransactionServiceImpl.transfer` para determinar a ordem de aquisição de locks com base no identificador de usuário (`Math.min` e `Math.max` entre `payer` e `payee`).
- Adquirir os locks pessimistas via `walletService.findByUserIdWithLock(...)` estritamente na ordem crescente de ID.
- Associar corretamente as carteiras bloqueadas aos papéis de pagador (`payerWallet`) e recebedor (`payeeWallet`) antes da validação de saldo e movimentação financeira.
- Preservar todas as regras de negócio existentes (validação de existência de usuários, restrição para lojistas e saldo suficiente).
- Adicionar testes unitários e de concorrência cobrindo o fluxo de transferência, incluindo cenários em que `payer < payee` e `payer > payee`.

## Capabilities

### New Capabilities

- `transaction/deadlock-prevention`: Define a ordenação determinística de locks pessimistas na transferência entre carteiras para prevenir deadlocks em operações concorrentes bidirecionais.

### Modified Capabilities

None.

## Impact

- **Código afetado:** `TransactionServiceImpl.java` e testes em `TransactionServiceImplTest.java`.
- **APIs e Contratos:** Inalterados; nenhuma mudança de interface externa ou DTOs.
- **Concorrência e Banco de Dados:** Eliminação de exceções de deadlock decorrentes de transferências bidirecionais simultâneas.
