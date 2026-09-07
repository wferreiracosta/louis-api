## 1. Configuração de Perfis no Maven (pom.xml)

- [x] 1.1 Declarar a seção `<profiles>` no `pom.xml` com os perfis `lcl`, `qa` e `prd`, configurando `lcl` como `<activeByDefault>true</activeByDefault>` e a propriedade `<activatedProperties>` em cada perfil (`lcl`, `qa` e `prd`), validando com `mvn help:active-profiles`
- [x] 1.2 Validar a seleção de perfis via linha de comando do Maven executando `mvn help:active-profiles`, `mvn help:active-profiles -Pqa` e `mvn help:active-profiles -Pprd`

## 2. Integração com Propriedades do Spring Boot

- [x] 2.1 Configurar `spring.profiles.active=@activatedProperties@` em `src/main/resources/application.properties` e validar a substituição em `target/classes/application.properties` após executar `mvn process-resources`
- [x] 2.2 Validar que o empacotamento com `-Pqa` e `-Pprd` gera o arquivo `target/classes/application.properties` com `spring.profiles.active=qa` e `spring.profiles.active=prd` respectivamente

## 3. Testes e Validação Integrada

- [x] 3.1 Adicionar testes automatizados para validar a resolução da propriedade `spring.profiles.active` processada pelo Maven e a ativação dos perfis correspondentes
- [x] 3.2 Executar a suíte de testes do projeto via `mvn test` e verificar que todas as asserções passam com sucesso
