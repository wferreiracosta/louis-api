## Motivo

Os timestamps de modificação de carteiras não são mantidos quando os saldos mudam, impedindo que trilhas de auditoria financeira indiquem quando uma carteira foi atualizada pela última vez. Os timestamps de criação e última atualização devem ser gerados de forma confiável por eventos do ciclo de vida da persistência, para que as transferências possam ser rastreadas.

## Alterações

- Preencher automaticamente o timestamp de criação de uma carteira quando ela for persistida pela primeira vez.
- Atualizar automaticamente o timestamp de atualização de uma carteira sempre que a carteira persistida for modificada, incluindo alterações de saldo causadas por transferências.
- Manter os timestamps como metadados de auditoria gerenciados pela persistência, em vez de depender de que cada chamador os atribua.

## Capacidades

### Novas capacidades
- `wallet-audit-timestamps`: Fornece timestamps de criação e última atualização gerenciados automaticamente para carteiras persistidas.

### Capacidades modificadas

- Nenhuma.

## Impacto

- Código afetado: mapeamentos de timestamp de `WalletEntity` e os caminhos de persistência que criam ou atualizam carteiras.
- APIs: não são esperadas alterações nos contratos de solicitação ou resposta.
- Dependências: utiliza as anotações de ciclo de vida de timestamp do Hibernate já disponíveis pela dependência ORM do projeto.
