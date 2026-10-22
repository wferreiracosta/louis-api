## 1. Migrar o processamento JSON

- [x] 1.1 Injetar o `ObjectMapper` gerenciado pelo Spring em `SecretsManagerConfig` e substituir Gson por `readValue`; verificar com teste focado que os oito componentes do record são preenchidos com os valores do segredo sintético.
- [x] 1.2 Tratar `JsonProcessingException` como `ConfigurationException` com diagnóstico fixo, preservando a ausência de `SecretString` e o encapsulamento externo existente; verificar JSON inválido, segredo ausente, mensagem pública e ausência de um valor sentinela do segredo nos logs.
- [x] 1.3 Cobrir compatibilidade de campos adicionais, campos ausentes/nulos e porta numérica ou textual numérica com o mapper configurado como no Spring; executar os testes focados sem acesso à AWS real e confirmar os valores esperados.

## 2. Remover Gson da suíte e do build

- [x] 2.1 Migrar `UserCommonControllerTest`, `UserMerchantsControllerTest`, `TransactionControllerTest` e `UserUniquenessIntegrationTest` para `ObjectMapper` injetado e `writeValueAsString`; executar essas classes e confirmar que os cenários e assertions HTTP existentes permanecem válidos, incluindo casos de validação com campos ausentes/nulos.
- [x] 2.2 Remover import e injeção sem uso de Gson em `WalletControllerTest`; executar a classe e confirmar a criação do contexto e aprovação dos testes.
- [x] 2.3 Remover a dependência Gson do `pom.xml` e conferir `rg -n 'com\.google\.gson|\bGson\b|\bgson\b' src pom.xml` sem ocorrências; executar `./mvnw.cmd dependency:tree "-Dincludes=com.google.code.gson:gson"` e verificar ausência da biblioteca, investigando eventual origem transitiva.

## 3. Validar a integração

- [x] 3.1 Executar `./mvnw.cmd package` com a suíte completa habilitada, confirmar sucesso e inspecionar o JAR gerado para assegurar ausência de `gson-*.jar` em `BOOT-INF/lib`; registrar comandos e resultados. Em Unix, usar `./mvnw`.
- [ ] 3.2 Preparar os pré-requisitos locais do compose e iniciar a aplicação no perfil `lcl` com Ministack e PostgreSQL; confirmar criação do bean de segredo, configuração do DataSource e uma conexão bem-sucedida ao banco sem exibir credenciais. Se a infraestrutura impedir a execução, registrar o impedimento e manter esta tarefa pendente.
