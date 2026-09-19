## Context

Ver `proposal.md` para a motivação.

No fluxo atual de `TransactionServiceImpl.transfer()`, os locks pessimistas (`PESSIMISTIC_WRITE`) sobre as carteiras são adquiridos chamando sequencialmente `walletService.findByUserIdWithLock(parameter.payer())` e `walletService.findByUserIdWithLock(parameter.payee())`. Como a ordem depende exclusivamente da direção da transferência definida pelo cliente, transferências simultâneas bidirecionais (Thread 1: A → B, Thread 2: B → A) provocam espera circular entre threads, resultando em deadlock detectado e abortado pelo PostgreSQL.

## Goals / Non-Goals

**Goals:**
- Garantir a aquisição determinística de bloqueios pessimistas entre as duas carteiras participantes em ordem crescente de ID do usuário (`Math.min` seguido de `Math.max`).
- Mapear corretamente as carteiras obtidas para os papéis de `payerWallet` e `payeeWallet`, preservando integralmente o fluxo subsequente de débito, crédito, validações e persistência.
- Testar explicitamente cenários onde `payer < payee` e `payer > payee`.
- Validar via testes de concorrência a execução paralela de transferências bidirecionais sem ocorrência de deadlock e com saldos finais consistentes.

**Non-Goals:**
- Validar ou bloquear auto-transferência (`payer == payee`), responsabilidade atribuída ao plano 004 (`004-bloquear-auto-transferencia.md`).
- Alterar o tipo ou a estratégia do bloqueio em `WalletService` (permanece lock pessimista `PESSIMISTIC_WRITE`).
- Alterar contratos públicos da API REST ou assinaturas de métodos.

## Decisions

### 1. Ordenação determinística com `Math.min` e `Math.max`

- **Decisão:** Obter `firstId = Math.min(parameter.payer(), parameter.payee())` e `secondId = Math.max(parameter.payer(), parameter.payee())`, e solicitar os bloqueios sequencialmente para `firstId` e `secondId`.
- **Racional:** Para um par de identificadores `Long`, `Math.min` e `Math.max` são instruções primitivas extremamente leves, sem sobrecarga de alocação de listas ou streams.
- **Alternativas consideradas:**
  - `List.of(parameter.payer(), parameter.payee()).stream().sorted()`: Descartada por gerar objetos e overhead desnecessário para apenas dois elementos.

### 2. Atribuição de papéis após aquisição dos locks

- **Decisão:** Reassociar as carteiras bloqueadas com base nos IDs originais da requisição:
  ```java
  final var payerWallet = firstId.equals(parameter.payer()) ? firstWallet : secondWallet;
  final var payeeWallet = firstId.equals(parameter.payee()) ? firstWallet : secondWallet;
  ```
- **Racional:** Mantém a clareza e a legibilidade do restante do método, sem alterar as etapas posteriores de verificação de saldo, cálculo e salvamento da transação.

### 3. Estratégia de testes e validação concorrente

- **Decisão:**
  - Incluir testes unitários no `TransactionServiceTest` validando transferências tanto com `payer.id < payee.id` quanto com `payer.id > payee.id`, verificando saldos finais corretos em ambos os cenários.
  - Implementar teste concorrente com `ExecutorService` e `CountDownLatch` sobre banco H2 em memória (`@SpringBootTest`) simulando múltiplas transferências simultâneas em direções opostas (A → B e B → A).
- **Racional:** H2 elimina a necessidade de subir container para rodar testes, mantendo o ciclo de feedback local rápido.
- **Alternativas consideradas:**
  - PostgreSQL via Testcontainers: Descartada. Embora reproduza fielmente o comportamento de deadlock do PostgreSQL, exige Docker disponível em tempo de build e aumenta significativamente o tempo de execução dos testes.
- **Trade-off aceito:** H2 usa um mecanismo de lock diferente do PostgreSQL; um deadlock real em produção poderia não ser reproduzido pelo teste. A ordenação determinística de locks é verificada pela lógica em si e pelos testes unitários — o teste concorrente valida principalmente a consistência dos saldos finais em execução paralela.

## Risks / Trade-offs

- **[Cenário de auto-transferência (`payer == payee`)]** → Caso os dois IDs sejam iguais, `firstId` e `secondId` serão idênticos, requisitando o lock duas vezes na mesma transação.
  - *Mitigação:* O bloqueio de auto-transferência é objeto de validação específica do plano 004. A lógica aqui não introduz novos problemas além do comportamento pré-existente.
- **[Desempenho de lock sequencial]** → A aquisição de dois locks pessimistas em ordem determinística introduz uma ligeira serialização entre transações que disputam as mesmas carteiras.
  - *Mitigação:* Esta serialização é exatamente o comportamento desejado para prevenir deadlocks e manter a integridade dos saldos financeiros.
