# Self-Transfer Validation Specification

## Purpose

Garante que uma transferência financeira não possa ser iniciada quando o pagador e o recebedor são o mesmo usuário, prevenindo registros de transação sem efeito líquido e colisões de bloqueio pessimista no banco de dados.

## Requirements

### Requirement: Rejeição de auto-transferência antes do acesso ao banco de dados

O sistema SHALL rejeitar qualquer solicitação de transferência em que o identificador do pagador (`payer`) seja igual ao identificador do recebedor (`payee`), lançando uma exceção de validação de negócio antes de qualquer consulta ou bloqueio sobre o banco de dados.

#### Scenario: Requisição de transferência com payer e payee idênticos

- **WHEN** uma requisição `POST /transaction` é recebida com `payer` igual a `payee`
- **THEN** o sistema SHALL retornar HTTP 422 com o campo `"payer"` e a mensagem `"Self-transfer is not allowed"`
- **THEN** nenhum registro de transação SHALL ser criado no banco de dados
- **THEN** nenhum bloqueio pessimista SHALL ser adquirido sobre qualquer carteira

#### Scenario: Requisição de transferência com payer e payee distintos

- **WHEN** uma requisição `POST /transaction` é recebida com `payer` diferente de `payee`
- **THEN** o sistema SHALL prosseguir com a validação e execução normais da transferência
- **THEN** a validação de auto-transferência SHALL ser transparente e sem efeito colateral
