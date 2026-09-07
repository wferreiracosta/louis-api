# Environment Profiles Specification

## Purpose

Define a diferenciação e resolução de perfis de ambiente (lcl, qa, prd) no Maven com o perfil lcl ativo por padrão e integração com as propriedades de execução do Spring Boot.

## Requirements

### Requirement: Perfis Maven de ambiente
O arquivo `pom.xml` SHALL disponibilizar perfis Maven com os identificadores `lcl`, `qa` e `prd`. O perfil `lcl` SHALL ser configurado como ativo por padrão no Maven (`activeByDefault=true`). Cada perfil SHALL definir a propriedade `activatedProperties` com o identificador correspondente ao ambiente (`lcl`, `qa` ou `prd`).

#### Scenario: Build padrão sem seleção explícita de perfil
- **WHEN** o build Maven for executado sem parâmetros de seleção de perfil
- **THEN** o Maven SHALL ativar por padrão o perfil `lcl`
- **THEN** o valor da propriedade `activatedProperties` SHALL ser resolvido como `lcl`

#### Scenario: Build com perfil qa selecionado
- **WHEN** o build Maven for executado com o perfil `-Pqa` ativo
- **THEN** o Maven SHALL ativar o perfil `qa`
- **THEN** o valor da propriedade `activatedProperties` SHALL ser resolvido como `qa`

#### Scenario: Build com perfil prd selecionado
- **WHEN** o build Maven for executado com o perfil `-Pprd` ativo
- **THEN** o Maven SHALL ativar o perfil `prd`
- **THEN** o valor da propriedade `activatedProperties` SHALL ser resolvido como `prd`

### Requirement: Propagação do perfil ativo para o Spring Boot
A configuração da aplicação em `src/main/resources/application.properties` SHALL definir a propriedade `spring.profiles.default` referenciando a propriedade Maven `@activatedProperties@`. O mecanismo de filtragem de recursos do Maven SHALL substituir o placeholder pelo valor do perfil ativo no arquivo de configuração final.

#### Scenario: Ativação do perfil local em tempo de execução
- **WHEN** a aplicação for empacotada ou executada com o perfil padrão Maven `lcl`
- **THEN** o arquivo de propriedades gerado SHALL conter `spring.profiles.default=lcl`
- **THEN** o Spring Boot SHALL ativar o perfil `lcl` na inicialização quando nenhum outro perfil estiver ativo

#### Scenario: Ativação do perfil qa em tempo de execução
- **WHEN** a aplicação for empacotada ou executada com o perfil Maven `-Pqa`
- **THEN** o arquivo de propriedades gerado SHALL conter `spring.profiles.default=qa`
- **THEN** o Spring Boot SHALL ativar o perfil `qa` na inicialização quando nenhum outro perfil estiver ativo

#### Scenario: Ativação do perfil prd em tempo de execução
- **WHEN** a aplicação for empacotada ou executada com o perfil Maven `-Pprd`
- **THEN** o arquivo de propriedades gerado SHALL conter `spring.profiles.default=prd`
- **THEN** o Spring Boot SHALL ativar o perfil `prd` na inicialização quando nenhum outro perfil estiver ativo
