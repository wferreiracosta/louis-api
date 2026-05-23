# Plano de Implementação - Refatoração louis-api (001-pontos-de-melhoria)

Este plano descreve as etapas para implementar os pontos de melhoria descritos na especificação `001-pontos-de-melhoria.spec.md`, respeitando as diretrizes fornecidas e as exceções aprovadas.

## Regras Globais a Respeitar (Com exceções concedidas pelo usuário)
1. **Alteração do pacote "configs"**: Permitido para implementar a melhoria 5 ("Uso de DriverManagerDataSource em Produção"), alterando o arquivo `DataSourceConfig.java`.
2. **Adição de bibliotecas no `pom.xml`**: Permitido, mas para evitar complexidade na compilação do Maven, usaremos o pool **HikariCP** (que já vem por padrão com o Spring Boot Starter JPA, sem necessidade de adicionar novas linhas ao `pom.xml`), e para o mapeamento dos DTOs usaremos mapeadores manuais rápidos (ou adicionaremos ModelMapper se necessário).
3. **Formato das mensagens de commit**: `[tipo_commit]:[mensagem]` (e.g. `refactor:adiciona controle transacional`).

---

## Proposta de Alterações por Etapa (Commits separados)

### Etapa 1: Controle Transacional
- **Objetivo**: Adicionar `@Transactional` no método `transfer`.
- **Arquivos**:
  - [TransactionServiceImpl.java](../../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java)
- **Mensagem do Commit**: `refactor:adiciona controle transacional em transferencias`

### Etapa 2: Concorrência e Pessimistic Lock
- **Objetivo**: Implementar bloqueio pessimista ao buscar as carteiras envolvidas na transferência.
- **Arquivos**:
  - [WalletRepository.java](../../src/main/java/br/com/wferreiracosta/louis/repositories/WalletRepository.java)
  - [WalletServiceImpl.java](../../src/main/java/br/com/wferreiracosta/louis/services/impl/WalletServiceImpl.java)
- **Mensagem do Commit**: `fix:adiciona lock pessimista nas carteiras durante transferencia`

### Etapa 3: Validação de Regras de Negócio na Camada de Serviço
- **Objetivo**: Mover a lógica de validação (saldo suficiente e tipo de usuário lojista) de `TransactionValidator` para `TransactionServiceImpl`. Ajustar o teste `TransactionServiceTest` para validar adequadamente e garantir que o validador original não execute queries duplicadas ou seja removido de validações de negócio críticas.
- **Arquivos**:
  - [TransactionValidator.java](../../src/main/java/br/com/wferreiracosta/louis/annotations/validators/TransactionValidator.java)
  - [TransactionServiceImpl.java](../../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java)
  - [TransactionServiceTest.java](../../src/test/java/br/com/wferreiracosta/louis/services/TransactionServiceTest.java)
- **Mensagem do Commit**: `refactor:move validacao de regras de negocio de transferencia para servico`

### Etapa 4: Exposição de DTOs nos Controllers
- **Objetivo**: Criar classes de DTO de resposta para `UserEntity` e `WalletEntity` e atualizar os controllers para retorná-los, mapeando-os manualmente.
- **Arquivos**:
  - Novos arquivos de DTO: `UserResponseDTO.java`, `WalletResponseDTO.java`.
  - [UserCommonControllerImpl.java](../../src/main/java/br/com/wferreiracosta/louis/controllers/impl/UserCommonControllerImpl.java)
  - [UserMerchantsControllerImpl.java](../../src/main/java/br/com/wferreiracosta/louis/controllers/impl/UserMerchantsControllerImpl.java)
  - [WalletControllerImpl.java](../../src/main/java/br/com/wferreiracosta/louis/controllers/impl/WalletControllerImpl.java)
- **Mensagem do Commit**: `refactor:retorna DTOs de resposta em vez de entidades nos controllers`

### Etapa 5: Uso de `DriverManagerDataSource` em Produção
- **Objetivo**: Substituir o `DriverManagerDataSource` (que cria conexões sob demanda) pelo pool de conexões do **HikariCP** (utilizando a classe `HikariDataSource` do próprio Spring Boot Starter) no ambiente de não-teste.
- **Arquivos**:
  - [DataSourceConfig.java](../../src/main/java/br/com/wferreiracosta/louis/configs/DataSourceConfig.java)
- **Mensagem do Commit**: `refactor:configura pool de conexao HikariCP em producao`

### Etapa 6: Detalhes e Erros de Escrita (Typos) e Limpeza
- **Objetivo**: 
  1. Renomear `TransactionRespository` para `TransactionRepository` e corrigir todas as referências.
  2. Adicionar `spring.jpa.open-in-view=false` em `application.properties`.
  3. Remover `spring-cloud-starter-openfeign` do `pom.xml`.
- **Arquivos**:
  - [TransactionRespository.java](../../src/main/java/br/com/wferreiracosta/louis/repositories/TransactionRespository.java) (Renomear)
  - Vários arquivos referenciando o repositório.
  - [application.properties](../../src/main/resources/application.properties)
  - [pom.xml](../../pom.xml)
- **Mensagem do Commit**: `refactor:corrige erros de grafia, limpa dependencias e desabilita OSIV`

---

## Plano de Verificação
- Executar `mvn clean test` após cada etapa para garantir que nenhuma regressão foi introduzida.
