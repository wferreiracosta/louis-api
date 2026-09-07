## Why

A aplicação possui arquivos de propriedades segregados para os perfis `lcl`, `qa` e `prd`, porém o arquivo `pom.xml` não define perfis Maven correspondentes. Para selecionar o perfil de ambiente durante o ciclo de build e empacotamento, é necessário ter perfis declarados no Maven com o ambiente local (`lcl`) ativo por padrão para facilitar o desenvolvimento diário.

## What Changes

- Adicionar seção `<profiles>` no `pom.xml` com os perfis `lcl`, `qa` e `prd`.
- Configurar o perfil Maven `lcl` com `<activeByDefault>true</activeByDefault>`.
- Definir a propriedade de perfil ativo no Maven (`<activatedProperties>`) em cada perfil (`lcl`, `qa`, `prd`).
- Configurar `spring.profiles.active=@activatedProperties@` em `src/main/resources/application.properties` para resolução via filtragem de recursos do Maven.
- Permitir a comutação de perfis via linha de comando do Maven (`mvn clean package -Pqa` ou `mvn clean package -Pprd`).

## Capabilities

### New Capabilities

- `configuration/environment-profiles`: Define a diferenciação e resolução de perfis de ambiente (`lcl`, `qa`, `prd`) no Maven (`pom.xml`) com o perfil `lcl` ativo por padrão e integração com as propriedades do Spring Boot.

### Modified Capabilities

<!-- Nenhuma capability existente tem seus requisitos alterados. -->

## Impact

- **Build Maven**: `pom.xml` passa a suportar os perfis `lcl`, `qa` e `prd`. Por padrão, execuções sem `-P` assumirão `lcl`.
- **Configuração da Aplicação**: `src/main/resources/application.properties` conterá a diretiva `spring.profiles.active=@activatedProperties@`.
- **Retrocompatibilidade e Testes**: O perfil `test` continuará sobrescrevendo as configurações conforme necessário nos testes de integração.
