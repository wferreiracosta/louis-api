## Why

As consultas de unicidade de usuários estão em um ConstraintValidator, acoplando Bean Validation ao banco e deixando chamadas diretas ao serviço sem essa regra de negócio. O plano 007 propõe concentrar as verificações no serviço; as decisões confirmadas exigem HTTP 422 e a apresentação conjunta dos conflitos de documento e e-mail.

## What Changes

- Mover ambas as consultas para `UserServiceImpl.save`, em uma transação que também abrange a persistência.
- Consultar documento e e-mail antes de decidir pela rejeição, reunindo todos os conflitos e impedindo a gravação quando houver qualquer um deles.
- **BREAKING**: conflitos de unicidade nos cadastros comum e lojista passam de HTTP 400 para HTTP 422, mantendo o formato `ValidationError`, os campos e as mensagens atuais.
- Remover `UserSaveValidator`, `UserValidator` e sua aplicação em `UserParameter`, pois não há validação estrutural nesse validator; preservar as constraints estruturais existentes.
- Manter HTTP 400 para validações estruturais e demais erros de negócio existentes.
- Preservar as constraints únicas do banco como garantia final contra gravações concorrentes.

## Capabilities

### New Capabilities

- `user/registration-uniqueness`: unicidade de documento e e-mail no serviço, rejeição agregada e contrato HTTP 422 nos cadastros.

### Modified Capabilities

Nenhuma especificação existente cobre essa capacidade.

## Impact

Afeta `UserServiceImpl`, as classes de validação de usuário, `UserParameter`, uma exceção agregada de unicidade e seu tratamento em `GlobalExceptionHandler`. Exige testes unitários sem Spring e atualização dos testes dos endpoints `/users/common` e `/users/merchants` conforme as rotas reais do projeto. Não adiciona dependências nem altera o esquema do banco. A mudança de status exige que consumidores aceitem 422; a transação, por si só, não elimina a corrida entre consulta e inserção.
