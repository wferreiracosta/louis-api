## Purpose

Garante a integridade e a prevenção de impasses (deadlocks) durante transferências financeiras concorrentes entre carteiras, por meio da aquisição determinística e ordenada de bloqueios pessimistas sobre as contas envolvidas.

## ADDED Requirements

### Requirement: Aquisição ordenada de bloqueios em transferências concorrentes

O sistema SHALL adquirir os bloqueios pessimistas sobre as carteiras dos usuários envolvidos em ordem determinística crescente com base em seus identificadores numéricos, independentemente da direção da transferência (quem é pagador e quem é recebedor).

#### Scenario: Transferências simultâneas em direções opostas entre os mesmos usuários
- **WHEN** duas ou mais transações tentam realizar transferências mútuas simultâneas entre os mesmos usuários (usuário A para usuário B e usuário B para usuário A)
- **THEN** ambas as transações SHALL requisitar os bloqueios das carteiras na mesma ordem determinística (menor identificador primeiro, seguido pelo maior identificador)
- **THEN** as transações concorrentes SHALL ser executadas sem interrupção por exceções de deadlock no banco de dados

#### Scenario: Transferência com pagador possuindo identificador menor que o recebedor
- **WHEN** uma solicitação de transferência é iniciada onde o identificador do usuário pagador é numericamente menor que o do recebedor
- **THEN** o sistema SHALL bloquear primeiro a carteira do pagador e em seguida a carteira do recebedor
- **THEN** o sistema SHALL debitar o valor da carteira do pagador e creditar na carteira do recebedor com sucesso
- **THEN** o saldo final da carteira do pagador SHALL ser igual ao saldo anterior subtraído do valor transferido
- **THEN** o saldo final da carteira do recebedor SHALL ser igual ao saldo anterior acrescido do valor transferido

#### Scenario: Transferência com pagador possuindo identificador maior que o recebedor
- **WHEN** uma solicitação de transferência é iniciada onde o identificador do usuário pagador é numericamente maior que o do recebedor
- **THEN** o sistema SHALL bloquear primeiro a carteira do recebedor e em seguida a carteira do pagador
- **THEN** o sistema SHALL debitar o valor da carteira do pagador e creditar na carteira do recebedor com sucesso
- **THEN** o saldo final da carteira do pagador SHALL ser igual ao saldo anterior subtraído do valor transferido
- **THEN** o saldo final da carteira do recebedor SHALL ser igual ao saldo anterior acrescido do valor transferido

#### Scenario: Saldo insuficiente com pagador possuindo identificador maior que o recebedor
- **WHEN** uma solicitação de transferência é iniciada onde o identificador do pagador é numericamente maior que o do recebedor
- **AND** o saldo da carteira do pagador é inferior ao valor solicitado
- **THEN** o sistema SHALL lançar uma exceção de validação de negócio indicando saldo insuficiente
- **THEN** nenhum débito ou crédito SHALL ser realizado em qualquer das carteiras envolvidas
