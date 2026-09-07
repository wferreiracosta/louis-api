## Context

Consulte `proposal.md` para a motivação da mudança.
Atualmente, o projeto conta com arquivos de configuração de ambiente segregados (`application-lcl.properties`, `application-qa.properties`, `application-prd.properties`) e utiliza `spring-boot-starter-parent` 3.1.5, que já possui filtragem de recursos habilitada por padrão via delimitador `@...@`. No entanto, o `pom.xml` não declara perfis de build Maven, exigindo passagem manual de perfis em tempo de execução.

## Goals / Non-Goals

**Goals:**
- Declarar perfis Maven (`profiles`) para `lcl`, `qa` e `prd` no `pom.xml`.
- Configurar `lcl` como o perfil ativo padrão através de `<activeByDefault>true</activeByDefault>`.
- Definir a propriedade `<activatedProperties>` em cada perfil com seu respectivo identificador.
- Definir `spring.profiles.active=@activatedProperties@` em `application.properties`.
- Garantir que a ativação explícita de `-Pqa` ou `-Pprd` comute a propriedade de ambiente sem conflito.

**Non-Goals:**
- Alterar as propriedades internas de datasource ou segredos da AWS existentes nos arquivos de propriedades.
- Adicionar plugins Maven adicionais além dos já fornecidos pelo Spring Boot parent.
- Alterar o comportamento do perfil `test`, que permanece isolado para testes unitários e de integração.

## Decisions

### Decisão 1: Utilizar a propriedade Maven `activatedProperties` para propagação

- **Opção escolhida:** Definir `<activatedProperties>lcl</activatedProperties>` (e respectivamente `qa` e `prd`) dentro de `<properties>` em cada `<profile>`.
- **Alternativas consideradas:**
  - Definir `<spring.profiles.active>lcl</spring.profiles.active>` como propriedade Maven: embora funcione, pode gerar ambiguidade com propriedades de sistema ou variáveis de ambiente de mesmo nome durante o boot. O padrão `activatedProperties` com substituição `@activatedProperties@` é idiomático e desacoplado.
- **Racional:** Permite que o `maven-resources-plugin` filtre `application.properties` substituindo `spring.profiles.active=@activatedProperties@` pelo valor do perfil ativo.

### Decisão 2: Ativação padrão de `lcl` via `activeByDefault`

- **Opção escolhida:** Adicionar `<activation><activeByDefault>true</activeByDefault></activation>` ao perfil `lcl`.
- **Racional:** Garante que qualquer build local (`mvn clean compile`, `mvn spring-boot:run` ou compilação pela IDE) assuma `lcl` sem exigir parâmetros adicionais. Caso `-Pqa` ou `-Pprd` seja especificado, o Maven desativa automaticamente o perfil com `activeByDefault`.

### Decisão 3: Integração com testes existentes

- **Opção escolhida:** Manter testes de contexto configurados para usar o perfil apropriado (`test` ou profiles explícitos). Atualizar `SchemaManagementProfileTest` se necessário para contemplar a resolução do perfil padrão filtrado.

## Risks / Trade-offs

- **[Risco]** Executar a aplicação via classe principal na IDE sem compilação prévia pelo Maven pode deixar `@activatedProperties@` sem substituição caso a IDE não processe recursos Maven.
  → **Mitigação:** Documentar a necessidade de delegar o build ao Maven ou selecionar o perfil Maven na IDE.
- **[Risco]** Sobrescrita acidental ao especificar múltiplos perfis Maven simultaneamente.
  → **Mitigação:** Os perfis são mutuamente exclusivos por finalidade de ambiente (`lcl`, `qa`, `prd`).
