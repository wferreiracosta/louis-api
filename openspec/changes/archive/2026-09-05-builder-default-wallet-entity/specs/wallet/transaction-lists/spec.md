## Purpose

Define o contrato de inicialização das listas de transações (`transferring` e `receiving`) de `WalletEntity`, garantindo que esses campos sejam sempre não-nulos independentemente da forma de construção da entidade.

## ADDED Requirements

### Requirement: Listas de transações de WalletEntity são sempre inicializadas

`WalletEntity` SHALL garantir que os campos `transferring` e `receiving` sejam sempre inicializados como listas vazias (nunca `null`), independentemente de a instância ter sido criada via construtor padrão, `@AllArgsConstructor` ou pelo builder gerado por Lombok (`@Builder`).

#### Scenario: Construção via builder sem informar as listas

- **WHEN** uma instância de `WalletEntity` é criada via `WalletEntity.builder().build()` sem fornecer valores para `transferring` ou `receiving`
- **THEN** `wallet.getTransferring()` SHALL retornar uma lista vazia (não `null`)
- **THEN** `wallet.getReceiving()` SHALL retornar uma lista vazia (não `null`)

#### Scenario: Adição de transação em wallet construída via builder

- **WHEN** `WalletEntity` é construída via builder
- **AND** o sistema tenta invocar `wallet.getTransferring().add(transaction)`
- **THEN** a operação SHALL completar sem lançar `NullPointerException`

#### Scenario: Construção via construtor padrão mantém comportamento anterior

- **WHEN** uma instância de `WalletEntity` é criada via `new WalletEntity()`
- **THEN** `wallet.getTransferring()` SHALL retornar uma lista vazia (comportamento preservado)
- **THEN** `wallet.getReceiving()` SHALL retornar uma lista vazia (comportamento preservado)
