## 1. Confirmar baseline

- [x] 1.1 Revisar referências com `rg -n 'TransactionValidator|br\.com\.wferreiracosta\.louis\.annotations\.Transaction\b|@Transaction\b' src` e confirmar que `TransactionParameter` não utiliza a anotação; registrar os resultados antes da exclusão.
- [x] 1.2 Executar `mvn -Plcl test` com JDK 17 antes da alteração e registrar o resultado da suíte, distinguindo falhas preexistentes.
- [x] 1.3 Registrar uma matriz de sondagens isoladas de `POST /transaction`, conforme o design: cada campo nulo/ausente, amount zero/negativo e tipo JSON incompatível; guardar payload, fixture e status/campos estáveis ou exceção para comparação após a limpeza, sem introduzir constraints.

## 2. Remover código morto

- [x] 2.1 Excluir `src/main/java/br/com/wferreiracosta/louis/annotations/validators/TransactionValidator.java` e `src/main/java/br/com/wferreiracosta/louis/annotations/Transaction.java`; verificar no diff que ambos foram removidos integralmente.
- [x] 2.2 Remover somente eventuais usos/imports residuais dos tipos excluídos e repetir a busca da tarefa 1.1, esperando zero resultados em `src`; verificar no diff que `@Valid`, `@Transactional`, regras de negócio e dependências Maven foram preservados e que o DTO não foi alterado sem necessidade.

## 3. Verificar compatibilidade

- [x] 3.1 Executar `mvn -Plcl clean test` e registrar o resultado; confirmar a compilação sem classes antigas e a aprovação dos testes existentes de sucesso, usuários inexistentes, comerciante, saldo insuficiente e auto-transferência.
- [x] 3.2 Repetir as sondagens da tarefa 1.3 com as mesmas fixtures e comparar respostas estáveis ou exceções; registrar equivalência antes/depois e quaisquer lacunas preexistentes, sem corrigir nulidade/positividade ou a divergência HTTP 422/400 nesta change.
- [x] 3.3 Revisar `git diff --check` e o diff final, confirmando que a alteração se limita à remoção dos tipos/referências mortos e aos artefatos da change; registrar as evidências de verificação no relatório de implementação.
