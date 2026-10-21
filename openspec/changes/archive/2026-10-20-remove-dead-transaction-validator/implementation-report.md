# Relatório de implementação

## Alteração

Exclusão de `annotations/Transaction.java` e `annotations/validators/TransactionValidator.java`: 49 linhas removidas. A busca inicial encontrou quatro referências, todas entre esses dois arquivos; após a exclusão, a mesma busca em `src` não encontrou resultados. `TransactionParameter` já não usava a anotação. Controller, serviço, handler, dependências Maven e outros validators permaneceram intactos.

## Ambiente e baseline

- Java 17.0.11 e Maven 3.9.7; perfil Maven `lcl`, perfil Spring de teste `test`, banco H2 em memória.
- A primeira execução no sandbox deixou de avançar ao iniciar o controller e foi interrompida. A repetição fora do sandbox concluiu normalmente.
- `mvn -Plcl test`: BUILD SUCCESS, 77 testes, zero falhas/erros/ignorados (68 existentes e nove sondagens temporárias).
- Os erros de schema ausente no log fazem parte dos testes de validação do schema; os testes correspondentes passaram.

## Sondagens de compatibilidade

Foi usada uma classe temporária `TransactionRemovalProbeTest`, com MockMvc e o contexto completo de `ControllerTestAnnotations`. Cada caso criou dois usuários COMMON, `payer` e `payee`, sobrenome `Probe`, emails `<nome>@probe.test`, documentos iguais aos nomes e carteiras com saldo 100. Cada invocação usou transação com rollback e recriação do contexto, isolando os dados. Os IDs observados foram 1 e 2.

O payload inicial foi `{"amount":5,"payer":1,"payee":2}`. A matriz abaixo altera um campo por vez. Os payloads e resultados completos normalizados estão em `probe-baseline.txt`. Timestamps de erro e a data de sucesso são excluídos da comparação; os demais campos da resposta, o status e a classe da causa raiz são comparados.

| Caso | Resultado antes da remoção |
| --- | --- |
| amount nulo ou ausente | NullPointerException propagada pelo MockMvc |
| payer nulo ou ausente | NullPointerException propagada pelo MockMvc |
| payee nulo ou ausente | IllegalArgumentException propagada pelo MockMvc |
| amount = 0 | HTTP 201, amount = 0 |
| amount = -5 | HTTP 201, amount = -5 |
| amount = "invalid" | HTTP 400, erro de desserialização de BigDecimal |

Essas lacunas já existiam antes da remoção e não foram corrigidas. A divergência entre a spec de auto-transferência (422) e o handler/teste (400) também permanece fora do escopo.

## Verificação após remoção

- `mvn -Plcl clean test`: BUILD SUCCESS, 77 testes, zero falhas/erros/ignorados, incluindo os seis testes de controller e os testes de serviço/concorrrência.
- As nove linhas normalizadas de `probe-after.txt` são idênticas às de `probe-baseline.txt` (`Compare-Object` sem diferenças).
- A classe temporária de sondagem foi removida após a comparação; nenhum teste permanente foi alterado.
- `git diff --check` e `openspec validate remove-dead-transaction-validator --strict` passaram.
- O diff final de produção contém somente a exclusão dos dois arquivos. A busca por referências aos tipos removidos em `src` não retornou resultados. Os demais arquivos novos são artefatos desta change.
