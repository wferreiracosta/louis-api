## 1. Implementação da validação

- [x] 1.1 Em `TransactionServiceImpl.transfer()`, adicionar como primeira instrução do método a verificação `if (parameter.payer().equals(parameter.payee()))` e lançar `new BusinessValidationException("payer", "Self-transfer is not allowed")` — verificar que o código compila sem erros (`mvn compile`)

## 2. Testes

- [x] 2.1 Adicionar teste unitário para o caso de auto-transferência: criar um `TransactionParameter` com `payer == payee`, invocar `transfer()` e verificar que `BusinessValidationException` é lançada com o campo `"payer"` e a mensagem `"Self-transfer is not allowed"`, e que nenhuma interação com repositórios ou `walletService` ocorre — verificar que o teste passa (`mvn test -pl . -Dtest=TransactionServiceImplTest`)

- [x] 2.2 Verificar que os testes de transferência válida (payer ≠ payee) continuam passando sem regressão — verificar executando toda a suite de testes (`mvn test`)

## 3. Verificação manual

- [x] 3.1 Chamar `POST /transaction` com `payer` e `payee` idênticos e confirmar resposta HTTP 422 com corpo contendo o campo `"payer"` e mensagem `"Self-transfer is not allowed"` — verificar que nenhum registro é criado na tabela `transactions` no banco de dados
