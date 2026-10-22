## 1. Erros agregados e contrato HTTP

- [x] 1.1 Criar `UserUniquenessValidationException` com lista imutável não vazia de `FieldMessage` e handler específico em `GlobalExceptionHandler`; verificar com testes que um e dois conflitos geram HTTP 422, `status=422`, `message=Errors`, timestamp e todos os erros em ordem documento/e-mail.
- [x] 1.2 Preservar `BusinessValidationException` e o tratamento estrutural; verificar em testes de regressão que erros de transferência e campos obrigatórios continuam retornando HTTP 400.

## 2. Validação no serviço e remoção da constraint

- [x] 2.1 Mover as duas consultas de unicidade para o início de `UserServiceImpl.save`, agregar os conflitos e rejeitar antes da persistência; verificar com testes JUnit/Mockito sem Spring os casos documento, e-mail, ambos no mesmo registro e ambos em registros distintos, garantindo as duas consultas e nenhuma chamada a `save` em caso de erro.
- [x] 2.2 Adicionar `@Transactional` ao método público `save` e preservar a criação de usuário e carteira; verificar sucesso para COMMON e MERCHANT em teste unitário e transação ativa nas consultas e gravação em teste do bean Spring sem transação externa do teste.
- [x] 2.3 Remover `UserSaveValidator`, `UserValidator` e as referências em `UserParameter`, preservando `@NotBlank`; verificar ausência de referências com busca no código e validar dados válidos e campos obrigatórios em branco usando Validator padrão sem Spring nem banco.

## 3. Integração e compatibilidade

- [x] 3.1 Atualizar testes de duplicidade em `UserCommonControllerTest` e `UserMerchantsControllerTest` para 422 e acrescentar ambos os campos duplicados, inclusive em usuários distintos e entre tipos; verificar lista completa, mensagens, ordem e ausência de novos usuários/carteiras após rejeição.
- [x] 3.2 Verificar nos dois endpoints que dados estruturalmente inválidos continuam produzindo 400 antes das consultas de unicidade, incluindo uma entrada que também colida com um usuário existente; manter os testes de criação válida com HTTP 201 e carteira associada com saldo zero.
- [x] 3.3 Preservar as constraints únicas de documento e e-mail; verificar com teste de persistência que inserções concorrentes conflitantes em transações independentes deixam no máximo um registro com o valor disputado, sem exigir novo status HTTP para a falha de gravação.
- [x] 3.4 Atualizar documentação de respostas dos endpoints onde existir para registrar o 422 exclusivo de unicidade e a agregação; verificar que não anuncia 422 para regras de negócio não alteradas.
- [x] 3.5 Executar a suíte completa com `./mvnw test` (ou `./mvnw.cmd test` no Windows), revisar o diff e registrar o resultado; confirmar que os testes de cadastro, validação estrutural e transferências passam.

## Verificação executada

Em 2026-10-22, a suíte completa passou: **88 testes, 0 falhas, 0 erros, 0 ignorados**. Como o checkout não contém Maven Wrapper, foi utilizado o Maven instalado. A execução inicial ficou sem progresso na inicialização do Mockito; carregar explicitamente o agente Byte Buddy resolveu a execução neste ambiente, sem alterar a configuração do projeto.

Comando executado:

```powershell
mvn '-DargLine=-javaagent:C:/Users/wferr/.m2/repository/net/bytebuddy/byte-buddy-agent/1.12.23/byte-buddy-agent-1.12.23.jar' test
```

Também passaram `git diff --check` e `openspec validate move-user-uniqueness-validation-to-service --strict`. Os testes de concorrência exercitam constraints em H2; não houve execução contra PostgreSQL. O tratamento HTTP de violações detectadas somente na gravação concorrente permanece fora do escopo definido no design.