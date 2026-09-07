## 1. Separação de configuração por ambiente

- [x] 1.1 Remover `spring.jpa.hibernate.ddl-auto` de `src/main/resources/application.properties`, preservando somente configurações comuns.
- [x] 1.2 Criar `src/main/resources/application-lcl.properties` com `spring.jpa.hibernate.ddl-auto=update`.
- [x] 1.3 Criar `src/main/resources/application-qa.properties` com `spring.jpa.hibernate.ddl-auto=validate`.
- [x] 1.4 Criar `src/main/resources/application-prd.properties` com `spring.jpa.hibernate.ddl-auto=validate`.
- [x] 1.5 Alterar `spring.jpa.hibernate.ddl-auto` em `src/test/resources/application-test.properties` de `create` para `create-drop`.

## 2. Verificação

- [x] 2.1 Executar a suíte Maven e confirmar que os testes que ativam o perfil `test` inicializam e usam o schema H2 descartável com sucesso.
- [x] 2.2 Iniciar a aplicação com o perfil `lcl` e confirmar que o Hibernate usa `update` sem recriar o schema integralmente.
- [x] 2.3 Iniciar a aplicação com os perfis `qa` e `prd`, em execuções separadas, contra schemas compatíveis e confirmar que ela não emite DDL de criação, alteração ou remoção.
- [x] 2.4 Reiniciar a aplicação com os perfis `qa` e `prd` no mesmo banco e confirmar que os dados previamente persistidos continuam presentes.
- [x] 2.5 Iniciar a aplicação com os perfis `qa` e `prd` contra um schema ausente ou incompatível e confirmar que a validação falha sem criar ou modificar tabelas.
