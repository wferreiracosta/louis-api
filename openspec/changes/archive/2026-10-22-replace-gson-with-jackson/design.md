## Context

Ver `proposal.md` para a motivação. O projeto usa Java 17 e Spring Boot 3.1.5; `spring-boot-starter-web` já fornece Jackson. `SecretsManagerProperties` é um record com oito componentes: username, password, engine, host, port (Integer), dbInstanceIdentifier, driverClassName e database.

`SecretsManagerConfig`, ativo fora do perfil `test`, consulta o AWS Secrets Manager e chama um método privado de mapeamento. O método público envolve aquisição e parsing em um `catch (Exception)`, converte erros em `ConfigurationException` e publica a mensagem genérica com o nome do segredo. Portanto, a mensagem específica de parsing sugerida no plano não chega diretamente ao consumidor do bean.

Há usos adicionais de Gson em cinco classes de teste. O design é necessário para decidir compatibilidade de serialização, tratamento de erros e cobertura do caminho que o perfil `test` desativa.

## Goals / Non-Goals

**Goals:**
- Reutilizar o mapper configurado pelo Spring, sem configurar outro bean global.
- Preservar o mapeamento dos campos e o funcionamento do DataSource, bem como os cenários HTTP exercitados pelos testes.
- Tornar falhas de parsing identificáveis internamente sem registrar dados do segredo.

**Non-Goals:**
- Atualizar Spring Boot, Jackson ou SDK AWS; alterar políticas de credenciais, região, endpoint, perfis ou ciclo de vida do cliente AWS.
- Adicionar validação de obrigatoriedade aos campos do segredo, suporte a SecretBinary ou novos contratos de erro público.
- Reestruturar controllers ou modificar o esquema do banco.

## Decisions

### 1. Injeção por construtor do mapper existente

Adicionar `ObjectMapper` como campo final de `SecretsManagerConfig`, utilizando o construtor Lombok existente, e substituir `fromJson` por `readValue`. Não criar um mapper manual nem adicionar versão/dependência explícita de Jackson: isso duplicaria configuração já gerenciada pelo Spring Boot.

Preservar nomes de propriedades e aceitar atributos adicionais do segredo conforme o mapper Spring. Verificar campos ausentes/nulos e `port` numérica ou representada como texto numérico, sem introduzir regras de validação novas. Não alterar opções globais do mapper para acomodar esse fluxo; eventuais ajustes de compatibilidade devem ser locais e motivados pelos testes.

### 2. Erros de parsing permanecem erros de configuração

Manter a verificação de ausência de `SecretString`. Capturar `JsonProcessingException` no mapeamento e lançar `ConfigurationException("Failed to parse Secrets Manager response")`. Preservar o encapsulamento externo existente: no fluxo público, a mensagem continua sendo `An error occurred when obtaining information from the secrets manager <nome>`.

Registrar apenas mensagem fixa de parsing, sem o JSON nem a mensagem/stack trace do parser, pois estes podem conter trechos do segredo. A alternativa do exemplo de origem, que registra a exceção completa, não será reproduzida. Não ampliar o construtor de `ConfigurationException` ou reformular o tratamento geral de falhas nesta mudança.

### 3. Remoção coordenada nos testes

Migrar `UserCommonControllerTest`, `UserMerchantsControllerTest`, `TransactionControllerTest` e `UserUniquenessIntegrationTest` para `ObjectMapper` injetado e `writeValueAsString`. Remover a injeção/import de Gson sem uso em `WalletControllerTest`. Ajustar assinaturas para exceções verificadas onde necessário.

Preservar os cenários e assertions existentes. Jackson pode emitir propriedades nulas que Gson omitia; verificar especialmente os testes de validação, garantindo que os casos originais continuem exercitados. Se omissão for parte de um cenário, construir o payload desse cenário explicitamente sem mudar a configuração global. Manter Gson apenas em escopo de teste foi descartado porque perpetuaria dois mecanismos JSON e contrariaria a remoção completa pretendida.

### 4. Verificação focada no comportamento

Cobrir o mapeamento de todos os campos, propriedades adicionais, valores ausentes/nulos e JSON malformado com dados sintéticos. Exercitar ausência de `SecretString` e o encapsulamento público de erros. Como `@Profile("!test")` desativa a configuração nos testes comuns, usar testes focados com resposta AWS controlada e mapper equivalente ao gerenciado pelo Spring; evitar chamadas reais à AWS e refatorações amplas apenas para testar.

Executar a suíte e `mvnw.cmd package` no Windows (`./mvnw package` em ambientes Unix), inspecionar a árvore de dependências e confirmar a ausência de Gson no JAR. Complementar com inicialização local usando o Ministack e PostgreSQL configurados no compose, verificando carga do segredo e conexão ao banco. Respeitar os pré-requisitos de rede externa e esquema existente; não alterar DDL para contornar falhas do ambiente.

## Risks / Trade-offs

- Diferenças de coerção, campos nulos e atributos desconhecidos entre parsers → testes com fixtures representativas e assertions do record completo; entradas malformadas devem continuar impedindo a inicialização.
- Perfil `test` mascara problemas na criação do bean → cobertura focada e smoke test fora desse perfil.
- Logs do Jackson podem revelar valores sensíveis → diagnóstico fixo e teste com valor sentinela para assegurar que o conteúdo não aparece nos logs.
- Dependência transitiva pode manter Gson no artefato mesmo após remoção direta → inspecionar `dependency:tree` e o JAR; investigar a origem antes de considerar a remoção concluída.
- Infraestrutura local indisponível → registrar explicitamente a validação pendente, sem afirmar sucesso de integração com base apenas na suíte H2.

## Migration Plan

1. Migrar produção e testes e remover Gson na mesma entrega.
2. Executar testes focados, empacotamento, inspeção de dependências e smoke test local.
3. Publicar pelo fluxo normal do projeto, sem migração de segredo ou banco.
4. Em caso de regressão, reverter conjuntamente a troca do parser e a remoção da dependência ou restaurar o artefato anterior.
