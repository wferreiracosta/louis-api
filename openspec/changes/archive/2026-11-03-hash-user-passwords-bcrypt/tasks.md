## 1. Dependências e configuração

- [x] 1.1 Atualizar Spring Boot para 3.5.16 e springdoc para 2.8.17, verificar geração OpenAPI e adicionar Spring Security com módulos alinhados em versão corrigida compatível, registrar a versão resolvida e configurar BCrypt `$2a` custo 10; verificar `./mvnw dependency:tree` (ou `mvn.cmd` no Windows), inicialização do contexto e teste real de encode/matches, incluindo senha incorreta.
- [x] 1.2 Configurar a cadeia temporária com acesso anônimo e sem CSRF, form login ou HTTP Basic; verificar os testes HTTP de usuários, carteiras e transações com filtros ativos, sem credenciais nem token CSRF.

## 2. Schema, validação e cadastro

- [x] 2.1 Mapear `password_encoding` nullable e oculto na serialização, preparar a coluna diretamente no teste PostgreSQL; testar duas execuções da preparação e inicialização com `ddl-auto=validate`, preservando registros e adaptando fixtures manuais de schema.
- [x] 2.2 Implementar regra compartilhada de até 72 bytes UTF-8 no parâmetro HTTP e no serviço antes da unicidade; verificar 72/73 bytes, caracteres multibyte, nulo/em branco, HTTP 400 e mensagem especificada, sem consultas ou gravações nas entradas rejeitadas pelo limite.
- [x] 2.3 Injetar encoder no serviço, remover o construtor `UserEntity(UserParameter)` e montar usuário com hash/marcador após unicidade; verificar ambos os tipos, carteira com saldo zero, associação bidirecional, ausência de persistência quando encoder falha e manutenção dos erros 422.
- [x] 2.4 Adaptar testes que instanciam `UserServiceImpl` e que esperam texto puro, incluindo testes de usuário, carteira, transferência e concorrência; verificar que compilam e que testes de encoding usam a entidade efetivamente enviada ao repositório, sem mascarar o comportamento com retorno pré-fabricado.
- [x] 2.5 Adicionar testes de integração HTTP com leitura posterior do banco para ambos os tipos, salt individual e entrada com aparência de hash; verificar matches da senha completa, rejeição de senha diferente e ausência de senha/hash/marcador nas respostas de cadastro e consulta.

## 3. Migração operacional

- [x] 3.1 Criar entrada Java isolada, acionada explicitamente, com datasource existente, modo somente verificação e códigos de saída; verificar o comando empacotado em ambiente descartável, ausência de servidor HTTP e ausência de conversão na inicialização normal da aplicação.
- [x] 3.2 Implementar trava consultiva de sessão PostgreSQL com conexão dedicada e liberação garantida; testar em PostgreSQL descartável que a segunda execução não escreve, que encerramento libera a trava e que uma execução posterior consegue iniciá-la.
- [x] 3.3 Implementar pré-validação completa com paginação por ID e lote configurável positivo; testar nulo, mais de 72 bytes, marcador desconhecido e hash marcado inválido no último lote, comprovando zero alterações em toda a base antes da falha e saída sem credenciais.
- [x] 3.4 Implementar conversão por lote com atualização atômica de hash/marcador, rollback do lote em falha e verificação final; testar base mista de legado/convertidos, texto com aparência de hash, senhas legadas vazias, multibyte, base vazia, reexecução e interrupção após um lote confirmado.
- [x] 3.5 Verificar em PostgreSQL descartável a preservação de IDs, demais campos de usuários, carteiras, saldos e transações; confirmar que retomada não altera hashes confirmados e que o comando só reporta conclusão quando não há pendências nem inconsistências.

## 4. Proteção de informações e entrega

- [x] 4.1 Substituir detalhes de parsing HTTP por `Malformed JSON request`, preservar estrutura/status 400 e impedir logs sensíveis nos fluxos novos; verificar com senhas sentinela em JSON inválido, tipo incompatível, erros de validação e falhas de migração que respostas e logs não contêm entrada nem hashes, incluindo configuração de bind logging.
- [x] 4.2 Remover os scripts SQL e o guia operacional conforme solicitação posterior do usuário; verificar a ausência da pasta e de dependências dos testes nesses arquivos.
- [x] 4.3 Executar a suíte Maven completa (`./mvnw test` ou `mvn.cmd test`) e o ensaio PostgreSQL que cobre preparação do schema, trava, rollback e retomada; registrar resultados, dependências resolvidas e duração observada para dimensionar a janela de manutenção.
