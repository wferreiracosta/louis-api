## Why

Atualmente, a configuração comum do Hibernate usa `spring.jpa.hibernate.ddl-auto=create`. Em qualquer inicialização que use essa configuração, o Hibernate recria o schema, o que pode apagar os dados persistidos em ambientes compartilhados, como homologação e produção.

Além disso, o perfil de testes usa `create`. Os testes precisam continuar criando um schema H2 isolado e descartável, mas devem fazê-lo explicitamente com `create-drop`, sem herdar a política segura da aplicação.

## What Changes

- Separar a configuração JPA/Hibernate por perfis Spring `lcl`, `qa` e `prd`, com um arquivo `application-<perfil>.properties` para cada ambiente.
- Definir `ddl-auto=update` no perfil local (`lcl`) para manter o schema de desenvolvimento sincronizado sem recriá-lo.
- Definir `ddl-auto=validate` nos perfis de homologação (`qa`) e produção (`prd`), de modo que a aplicação apenas valide a compatibilidade entre entidades e schema existente.
- Alterar a configuração do perfil `test` para `create-drop`, preservando a criação e remoção do schema H2 em memória para testes de integração.
- Remover a política `ddl-auto` da configuração comum, para que ela não possa substituir a política do ambiente ativo.
- Manter as demais configurações de datasource e dialeto inalteradas nesta mudança.
- Não introduzir Flyway nesta mudança; a futura migração para gerenciamento versionado do schema permanece fora de escopo.

## Capabilities

### New Capabilities

- `persistence/schema-management`: Define políticas explícitas e previsíveis de gerenciamento de schema Hibernate para os perfis `lcl`, `qa`, `prd` e `test`.

### Modified Capabilities

_(nenhuma — não há capability de gerenciamento de schema existente no repositório OpenSpec)_

## Impact

- **Arquivos afetados:** `src/main/resources/application.properties`, `application-lcl.properties`, `application-qa.properties`, `application-prd.properties` e `src/test/resources/application-test.properties`.
- **Ambiente local:** mantém a conveniência de desenvolvimento por meio de `update`, sem recriar tabelas.
- **Homologação e produção:** exigem que o schema já exista e esteja compatível com as entidades JPA.
- **Testes de integração:** continuam usando H2 em memória, com schema criado no início e removido no fim do ciclo do contexto.
- **Sem mudança de API pública** e sem migração ou alteração direta de dados.
