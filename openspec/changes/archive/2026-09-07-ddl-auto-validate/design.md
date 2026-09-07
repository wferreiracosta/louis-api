## Context

A aplicação Spring Boot usa Hibernate para o acesso JPA. A configuração comum fica em `application.properties`, e cada ambiente usará seu próprio arquivo Spring Profile: `application-lcl.properties`, `application-qa.properties` e `application-prd.properties`. Os testes carregam adicionalmente `application-test.properties` por meio de `@ActiveProfiles("test")` e `@TestPropertySource`. O ambiente de teste usa H2 em memória, enquanto os ambientes da aplicação usam a configuração PostgreSQL comum.

O valor comum atual `ddl-auto=create` faz com que a inicialização da aplicação seja responsável por reconstruir o schema. Isso é inadequado onde o banco contém dados persistidos. O ambiente local precisa de uma estratégia mais conveniente, sem destruição de dados, enquanto homologação e produção devem falhar ao detectar schema incompatível. Os testes precisam de um schema efêmero para serem independentes.

## Goals / Non-Goals

### Goals

- Separar a política Hibernate para os perfis `lcl`, `qa` e `prd`.
- Impedir que homologação e produção modifiquem o schema.
- Fazer a inicialização de homologação e produção falhar quando o schema existente não corresponder às entidades JPA.
- Permitir atualização incremental do schema exclusivamente no ambiente local.
- Manter o isolamento dos testes com H2 e seu schema descartável.

### Non-Goals

- Adicionar Flyway, liquibase ou scripts de migração.
- Corrigir incompatibilidades de schema já existentes.

## Decisions

### Remover `ddl-auto` da configuração comum

`src/main/resources/application.properties` deixará de declarar `spring.jpa.hibernate.ddl-auto`. Dessa forma, o valor não poderá vazar da configuração comum para um ambiente que exija uma política diferente. A ativação do ambiente permanecerá a cargo do mecanismo padrão de Spring Profiles (por exemplo, `SPRING_PROFILES_ACTIVE`).

### Definir perfis explícitos para local, homologação e produção

`src/main/resources/application-lcl.properties` definirá `spring.jpa.hibernate.ddl-auto=update`. Essa política pode criar ou ajustar o schema local para acompanhar as entidades, mas não o recria integralmente.

`src/main/resources/application-qa.properties` e `src/main/resources/application-prd.properties` definirão `spring.jpa.hibernate.ddl-auto=validate`. Esse valor não cria, altera ou remove tabelas; ele interrompe a inicialização quando detectar incompatibilidade entre o mapeamento JPA e o schema. Esses perfis exigem schema previamente provisionado e compatível.

### Usar `create-drop` exclusivamente no perfil de testes

`src/test/resources/application-test.properties` definirá `spring.jpa.hibernate.ddl-auto=create-drop`. O arquivo de teste tem precedência sobre a configuração comum quando o perfil `test` é ativado, permitindo o setup e cleanup automatizados do banco H2 em memória sem abrir essa exceção para a aplicação normal.

## Risks / Trade-offs

- QA e produção não inicializarão em um banco vazio ou desatualizado; isso é intencional e expõe a necessidade de provisionamento/migrações do schema.
- `update` pode produzir alterações implícitas no schema local; por isso, ele ficará restrito ao perfil `lcl` e não deve ser ativado em ambientes compartilhados.
- Uma seleção incorreta de perfil pode aplicar uma política inadequada; deploys precisam definir explicitamente o perfil `qa` ou `prd`. Como o repositório atual não contém arquivos de orquestração de deploy da aplicação (e.g. Kubernetes, ECS, scripts de execução do container), a injeção de `SPRING_PROFILES_ACTIVE=qa` e `SPRING_PROFILES_ACTIVE=prd` é uma dependência operacional externa que deve ser configurada no ambiente/pipeline de implantação correspondente.
- Até a introdução de Flyway, alterações de schema continuam exigindo coordenação operacional manual.

## Migration Plan

1. Remover `ddl-auto` de `application.properties` e criar os arquivos de perfil `lcl`, `qa` e `prd` com suas políticas definidas.
2. Atualizar a configuração de teste para `create-drop`.
3. Executar a suíte com o perfil `test` para confirmar a criação e descarte do schema H2.
4. Iniciar a aplicação com `lcl` e confirmar a atualização não destrutiva do schema local.
5. Iniciar a aplicação com `qa` e `prd` contra schemas compatíveis e confirmar que não há DDL de criação, alteração ou remoção, nem perda de dados após reinicialização.

## Open Questions

_(nenhuma)_
