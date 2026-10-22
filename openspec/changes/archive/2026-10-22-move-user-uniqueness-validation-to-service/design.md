## Context

Ver `proposal.md` para motivação. Atualmente `UserSaveValidator` consulta documento e e-mail e agrega `FieldMessage`; não contém regras estruturais. `UserParameter` já declara `@NotBlank` nos campos obrigatórios. `UserServiceImpl.save` não tem `@Transactional`. `BusinessValidationException` carrega um único campo e seu handler responde 400, inclusive para regras de transferência. Documento e e-mail possuem `@Column(unique = true)`.

## Goals / Non-Goals

**Goals:**
- Aplicar a regra também a chamadas diretas ao serviço e tornar seus testes independentes de contexto Spring.
- Separar a rejeição agregada de unicidade dos demais erros de negócio.
- Cobrir consultas e gravação na transação do serviço, preservando a criação e associação de carteira.

**Non-Goals:**
- Alterar normalização, comparação ou formato de documento e e-mail; acrescentar novas validações de formato.
- Mudar status de erros estruturais ou regras de transferência.
- Introduzir locks, isolamento serializável, migrações de banco ou tradução de violações concorrentes de constraints para 422.

## Decisions

1. **Agregar antes de rejeitar.** No início de `save`, executar `findByDocument` e `findByEmail`, sem curto-circuito. Montar uma lista em ordem documento/e-mail com as mensagens existentes e lançar uma única exceção se não estiver vazia, antes de construir ou salvar usuário/carteira. O fail-fast do exemplo do plano foi descartado porque não atende à decisão do usuário. As consultas existentes preservam a semântica de comparação e abrangem ambos os tipos de usuário.

2. **Criar `UserUniquenessValidationException`.** Usar uma exceção derivada de `RuntimeException` com cópia imutável de uma lista não vazia de `FieldMessage`. Criar handler específico que retorna `ValidationError` com `status=422`, `message="Errors"`, timestamp e todos os erros. Preservar `BusinessValidationException` e seu handler. Generalizar a exceção atual exigiria adaptar consumidores sem necessidade e aumentaria o risco de mudar status de transferências.

3. **Abrir transação no método público `save`.** Adicionar `org.springframework.transaction.annotation.Transactional`, abrangendo as duas leituras e a gravação. A exceção de runtime permite rollback. Testes unitários com instanciação direta não exercitam o proxy; uma verificação de integração deve confirmar que leituras e gravação executam na transação do serviço, sem depender de transação criada pelo próprio teste.

4. **Remover a constraint customizada sem regra remanescente.** Excluir `UserSaveValidator`, `UserValidator` e a anotação/import em `UserParameter`, mantendo `@NotBlank`. Manter um validator que sempre retorna true deixaria uma abstração vazia. Um teste com Validator padrão, sem Spring ou banco, comprovará que as validações estruturais continuam funcionando.

5. **Preservar garantia do banco.** A transação não impede que duas requisições leiam ausência simultaneamente. Manter as constraints únicas como garantia final; não capturar genericamente `DataIntegrityViolationException`, pois isso poderia classificar outras falhas como duplicidade e não recuperaria necessariamente os dois conflitos.

## Risks / Trade-offs

- [Clientes esperam 400] → Registrar a mudança incompatível para 422 nos dois endpoints e verificar status HTTP e campo `status` do corpo nos testes.
- [Dois conflitos ocultados por curto-circuito] → Testar conflito simultâneo no mesmo registro e em registros distintos, garantindo ambas as consultas e ausência de `save`.
- [Transação aparente somente no teste] → Exercitar o bean Spring sem transação externa do teste e observar transação ativa nas operações do repositório.
- [Corrida entre leitura e gravação] → Preservar constraints únicas; o tratamento HTTP de falhas detectadas apenas no commit continua fora do escopo.
- [Falhas estruturais misturadas com duplicidade] → Preservar a precedência de Bean Validation: dados estruturalmente inválidos retornam 400 antes de chegar ao serviço; a agregação de unicidade se aplica aos dados estruturalmente válidos.

## Migration Plan

Implementar exceção e handler, mover as consultas para o serviço transacional e remover a constraint customizada na mesma entrega. Atualizar testes e eventual documentação de respostas dos endpoints, executar a suíte e comunicar a mudança 400→422 aos consumidores. Não há migração de dados ou esquema. Um rollback do código restaura a validação anterior e o status 400, preservando as constraints existentes.
