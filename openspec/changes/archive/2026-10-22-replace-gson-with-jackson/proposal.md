## Why

O projeto mantém Gson exclusivamente para operações JSON que podem usar o Jackson já fornecido pelo Spring Boot. O plano `docs/plans/008-substituir-gson-por-jackson.md` motiva remover essa dependência redundante e padronizar o processamento JSON, preservando os contratos existentes.

## What Changes

- Remover a dependência explícita `com.google.code.gson:gson` do Maven.
- Injetar o `ObjectMapper` gerenciado pelo Spring em `SecretsManagerConfig` para desserializar `SecretString` em `SecretsManagerProperties`.
- Tratar falhas de parsing como `ConfigurationException`, preservando a falha de inicialização e a mensagem externa do fluxo existente, sem expor o conteúdo do segredo nos logs.
- Migrar os usos de Gson nos testes de controllers e de unicidade de usuários; remover injeções e imports sem uso.
- Verificar compatibilidade do mapeamento, executar o build e validar a inicialização local com Secrets Manager e banco.

## Capabilities

### New Capabilities

Nenhuma. Trata-se de refatoração de dependência, sem nova capacidade funcional.

### Modified Capabilities

Nenhuma. Contratos HTTP, perfis, formato dos segredos e configuração do banco permanecem os mesmos. A change declara `skip_specs: true`, pois não altera requisitos das specs existentes.

## Impact

- Produção: `pom.xml` e `src/main/java/br/com/wferreiracosta/louis/configs/SecretsManagerConfig.java`.
- Testes: `UserCommonControllerTest`, `UserMerchantsControllerTest`, `TransactionControllerTest`, `WalletControllerTest` e `UserUniquenessIntegrationTest`, além da cobertura focada de configuração.
- Jackson permanece na versão gerenciada pelo Spring Boot; nenhuma nova biblioteca ou atualização de versão é necessária.
- Integração local: o compose atual usa Ministack, embora o documento de origem mencione Localstack. A verificação usará o emulador configurado no projeto.
- Não há migração de dados nem mudança de API pública. Branch e commit sugeridos no documento são referências, não ações deste planejamento.
