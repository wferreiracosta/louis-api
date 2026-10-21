## Context

Ver `proposal.md` para motivação e escopo. O projeto usa Java 17, Spring Boot 3.1.5 e Jakarta Validation. `TransactionValidator.isValid` retorna sempre `true`; sua única ligação encontrada é a anotação `annotations.Transaction`. `TransactionParameter` é um record sem essa anotação ou constraints de campo, enquanto o controller mantém `@Valid @RequestBody`.

Este design registra as decisões necessárias para adaptar o plano 006 ao checkout real, especialmente a verificação de entradas inválidas sem inventar um contrato que hoje não existe.

## Goals / Non-Goals

**Goals:**
- Eliminar os tipos sem consumidores e confirmar que o build limpo não depende de classes compiladas antigas.
- Demonstrar que a remoção preserva os caminhos existentes de transferência e erro.

**Non-Goals:**
- Introduzir `@NotNull`, `@Positive`, validator substituto ou regras novas.
- Alterar o contrato HTTP, corrigir falhas preexistentes de entradas nulas ou reconciliar a spec de auto-transferência nesta change.
- Remover a dependência Jakarta Validation, outros validators ou usos legítimos de `UserRepository` e `@Transactional`.

## Decisions

1. **Excluir ambos os tipos integralmente.** O validator não oferece comportamento e a anotação não possui consumidor no DTO atual. Implementar a opção B do plano mudaria a aceitação de requisições e exigiria outra change. Não editar o DTO apenas para produzir um diff; remover uso/import somente se existir na revisão de implementação.
2. **Preservar as camadas atuais.** As regras de existência de usuários, pagador comerciante, saldo e auto-transferência permanecem no serviço; `@Valid` e o handler permanecem intactos. A remoção da injeção não utilizada ocorre pela exclusão da classe, sem afetar o repositório.
3. **Verificar comportamento por baseline.** Antes da remoção, executar os testes existentes e registrar sondagens controladas do endpoint: campos `amount`, `payer` e `payee` nulos/ausentes, valor zero/negativo e tipo JSON incompatível. Usar fixtures isoladas e rollback, registrando status/campos estáveis da resposta ou exceção propagada pelo MockMvc. Repetir após a remoção; não comparar timestamps nem exigir HTTP 400 para todos esses casos, pois faltam constraints no record. Registrar falhas preexistentes sem corrigi-las neste escopo. Não é necessário manter testes permanentes que cristalizem comportamento defeituoso.
4. **Usar a suíte existente como regressão funcional.** Executar `mvn -Plcl clean test` com JDK 17 e registrar o resultado. Os testes de controller já cobrem sucesso, usuários inexistentes, comerciante, saldo insuficiente e auto-transferência. A comparação de referência deve preservar HTTP 400 para os erros de negócio atuais, sem modificar a spec que menciona 422.
5. **Dispensar delta funcional.** `skip_specs: true` segue a instrução do schema para refatoração pura. A documentação histórica pode continuar mencionando os tipos removidos; a busca de referências deve se concentrar em código e configuração executáveis.

## Risks / Trade-offs

- [Busca textual confundir `@Transaction` com `@Transactional`] → Usar nomes qualificados e limites de palavra; preservar a anotação transacional do Spring.
- [Build incremental esconder referências quebradas] → Executar build limpo após excluir os arquivos.
- [Plano presumir validações inexistentes] → Comparar baseline antes/depois e registrar lacunas separadamente, sem ampliar o escopo.
- [Divergência HTTP 422/400 preexistente] → Preservar o comportamento observado e não editar a spec de auto-transferência nesta limpeza.
- [Ausência de evidência de ganho de bootstrap] → Não prometer melhoria de desempenho nem estabelecer benchmark como aceite.

## Migration Plan

Não há migração de dados ou configuração. Após as verificações, entregar pelo fluxo normal de build/deploy do projeto. Se necessário, reverter a exclusão dos dois arquivos e eventuais referências removidas; nenhuma restauração de dados é necessária.
