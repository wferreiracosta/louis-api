## Purpose

Garante que cada carteira persistida registre horários confiáveis de criação e última modificação para auditoria financeira e rastreabilidade operacional.

## ADDED Requirements

### Requirement: O horário de criação da carteira é registrado automaticamente
O sistema DEVE (MUST) atribuir um timestamp de criação quando uma carteira for persistida pela primeira vez, sem exigir que o chamador forneça esse timestamp.

#### Scenario: Persistência de uma nova carteira
- **WHEN** uma carteira é criada e persistida
- **THEN** seu timestamp de criação é preenchido com o horário da persistência

### Requirement: O horário de modificação da carteira é registrado automaticamente
O sistema DEVE (MUST) atualizar o timestamp de última modificação de uma carteira sempre que uma carteira persistida for atualizada, sem exigir que o chamador atribua esse timestamp.

#### Scenario: Atualização do saldo de uma carteira
- **WHEN** o saldo de uma carteira persistida é alterado e a carteira é salva
- **THEN** seu timestamp de última modificação é atualizado para o horário da atualização

#### Scenario: Conclusão de uma transferência
- **WHEN** uma transferência atualiza as carteiras do pagador e do recebedor
- **THEN** o timestamp de última modificação de cada carteira atualizada é renovado
