## Why

O plano `docs/plans/006-remover-transaction-validator-morto.md` identifica um validator que sempre retorna `true` e injeta `UserRepository` sem utilizá-lo. Remover essa estrutura sem consumidores reduz código enganoso e dependências internas desnecessárias, sem alterar o comportamento da API.

## What Changes

- Adotar a opção A do plano: excluir `annotations/validators/TransactionValidator.java` e `annotations/Transaction.java`.
- Confirmar a ausência de usos e imports desses tipos no código de produção e testes; remover eventuais referências residuais.
- Preservar `TransactionParameter`, que já não possui `@Transaction` nem import associado no checkout atual.
- Preservar `@Valid`, as validações de negócio no serviço e o tratamento de erros existente. Não adicionar constraints de nulidade, positividade ou outras regras.
- Verificar compilação limpa, testes e compatibilidade do endpoint `POST /transaction`.

## Capabilities

### New Capabilities

Nenhuma. Trata-se de remoção de código morto, sem capacidade funcional nova.

### Modified Capabilities

Nenhuma. Nenhum requisito funcional será alterado; a mudança declara `skip_specs: true`, conforme a orientação do schema para refatorações puras.

## Impact

- Produção: exclusão dos dois tipos internos em `src/main/java/br/com/wferreiracosta/louis/annotations/`.
- API, banco, dependências Maven e regras de negócio: sem mudanças previstas.
- Verificação: suíte existente de controller/serviço e comparação de entradas nulas/inválidas antes e depois, sem presumir validações declarativas inexistentes.
- A spec `transaction/self-transfer-validation` menciona HTTP 422, mas o handler e os testes atuais usam HTTP 400. Essa divergência preexistente fica registrada e fora do escopo desta limpeza.
- A alegação de melhoria de bootstrap do plano não foi medida; não constitui critério de aceite.
