## Context

`TransactionServiceImpl.transfer()` já possui validações sequenciais (existência de usuários, tipo de pagador, saldo suficiente) antes de executar o débito/crédito. A lógica de prevenção de deadlock introduzida pela mudança `order-locks-prevent-deadlock` usa `Math.min`/`Math.max` sobre os IDs do pagador e recebedor — comportamento que colapsa a dois locks no mesmo row quando ambos os IDs são iguais, tornando o cenário de auto-transferência especialmente perigoso em produção.

## Goals / Non-Goals

**Goals:**
- Adicionar uma única guarda antecipada em `TransactionServiceImpl.transfer()` que rejeite `payer == payee` antes de qualquer acesso ao banco.
- Garantir que a exceção lançada seja consistente com o padrão já estabelecido no serviço (`BusinessValidationException`).

**Non-Goals:**
- Alterar o modelo de dados, contrato de API (path, método HTTP, shape do response já existente) ou qualquer outro serviço.
- Introduzir nova infraestrutura (filtros, interceptors, anotações de validação JSR-380).
- Tratar cenários de transferência entre contas do mesmo usuário com carteiras distintas (o modelo atual é 1 usuário → 1 carteira).

## Decisions

### Localização da guarda: antes de qualquer acesso ao banco

**Decisão**: a verificação `payer == payee` deve ocorrer como *primeira* instrução do método, antes das consultas `userRepository.findById`.

**Rationale**: não há estado de banco necessário para determinar que dois IDs iguais são inválidos. Adiar a verificação exigiria buscar informações desnecessárias e abriria a janela para que locks pessimistas fossem adquiridos antes do erro ser detectado — exatamente o cenário de undefined behavior descrito no plano 004.

**Alternativas consideradas**:
- *Após* buscar usuários: descartada — acessa banco sem necessidade e expõe a lógica de lock ao bug.
- Anotação `@Valid` / constraint customizada no `TransactionParameter`: válida a longo prazo, mas introduz infra adicional para uma validação trivial; fora de escopo desta mudança.

### Tipo da exceção: `BusinessValidationException` com campo `"payer"`

**Decisão**: reutilizar `BusinessValidationException("payer", "Self-transfer is not allowed")`, mantendo o padrão já usado nas demais validações do método.

**Rationale**: consistência com o formato de erro já esperado pelos consumidores da API (campo + mensagem → HTTP 422). Nenhuma nova exceção ou handler é necessário.

## Risks / Trade-offs

- **Risco baixo / nenhuma migração necessária**: a mudança é puramente aditiva na camada de serviço. Não há dados históricos afetados — registros de auto-transferência existentes (se houver) permanecem intactos; a validação bloqueia apenas novas requisições.
- **Cobertura de teste**: a ausência de um teste unitário específico para este caso deixaria o comportamento implícito. A tarefa de implementação deve incluir um teste dedicado.
