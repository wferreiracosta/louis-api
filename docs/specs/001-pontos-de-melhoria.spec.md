# Análise de Pontos de Melhoria - louis-api

Este documento apresenta os pontos de melhoria identificados na arquitetura, concorrência, segurança e qualidade de código do projeto **louis-api**. Realize as alterações necessarias separando os commits por etapa listada no documento.

---

## 1. Controle Transacional (`@Transactional`)
No arquivo [TransactionServiceImpl.java](../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java), o método `transfer` não possui a anotação `@Transactional`:
```java
public TransactionDTO transfer(final TransactionParameter parameter) { ... }
```
* **Problema**: Se houver alguma falha durante a execução (como erro ao atualizar a carteira do beneficiário ou salvar o histórico), a transação não sofrerá rollback, gerando **inconsistência de dados** (ex: o dinheiro sai da conta do pagador, mas não entra na do recebedor).
* **Melhoria**: Adicione a anotação `@Transactional` da biblioteca `org.springframework.transaction.annotation` no método `transfer` ou no nível da classe `TransactionServiceImpl`.

---

## 2. Concorrência e *Race Conditions*
No processo de transferência em [TransactionServiceImpl.java](../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java), o saldo atual é lido, alterado em memória e persistido de volta sem nenhum mecanismo de bloqueio (locking):
```java
payerWallet.setAmount(payerWallet.getAmount().subtract(parameter.amount()));
payeeWallet.setAmount(payeeWallet.getAmount().add(parameter.amount()));
```
* **Problema**: Se um usuário iniciar duas transferências simultâneas de R$ 100,00 tendo apenas R$ 100,00 de saldo, ambas as requisições podem ler o mesmo saldo inicial, aprovar a operação concorrentemente e salvar um saldo final incorreto (negativo ou inconsistente).
* **Melhoria**: 
  * **Pessimistic Lock**: Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` na query de busca do Wallet/User para bloquear os registros até o fim da transação.
  * **Optimistic Lock**: Adicione um atributo `@Version` na entidade [WalletEntity.java](../src/main/java/br/com/wferreiracosta/louis/models/entities/WalletEntity.java) para falhar requisições concorrentes que tentem atualizar a carteira a partir de um estado desatualizado.

---

## 3. Validação de Regras de Negócio na Camada errada
As verificações de saldo suficiente e se o pagador é do tipo `MERCHANT` (Lojista não pode enviar dinheiro) estão contidas no [TransactionValidator.java](../src/main/java/br/com/wferreiracosta/louis/annotations/validators/TransactionValidator.java), acionado via `@Valid` na controller.
* **Problema**: 
  1. **Redundância e Desempenho**: Faz consultas ao banco de dados durante a etapa de validação do JSON, que ocorrem antes e fora da transação de escrita, gerando queries duplicadas.
  2. **Bypass em Testes Unitários**: No teste [TransactionServiceTest.java](../src/test/java/br/com/wferreiracosta/louis/services/TransactionServiceTest.java), a transferência é testada com sucesso definindo o `payer` com o tipo `MERCHANT`. Como o teste unitário chama o `service.transfer()` diretamente, a validação da controller é ignorada e a regra de negócio é violada de forma silenciosa no teste.
* **Melhoria**: Mova a lógica de verificação de saldo e tipo de usuário para dentro do [TransactionServiceImpl.java](../src/main/java/br/com/wferreiracosta/louis/services/impl/TransactionServiceImpl.java) e valide as regras com testes no `TransactionServiceTest`.

---

## 4. Exposição Direta de Entidades JPA nos Controllers
Controllers como [UserCommonControllerImpl.java](../src/main/java/br/com/wferreiracosta/louis/controllers/impl/UserCommonControllerImpl.java) e [WalletControllerImpl.java](../src/main/java/br/com/wferreiracosta/louis/controllers/impl/WalletControllerImpl.java) retornam as entidades do banco (`UserEntity` e `WalletEntity`) diretamente para os clientes.
* **Problema**:
  * Acopla o modelo do banco de dados diretamente ao contrato da API externa.
  * Pode resultar em erros de serialização (`LazyInitializationException`) ao tentar acessar relacionamentos carregados com *Lazy Loading* fora de uma sessão transacional.
  * Expõe metadados desnecessários da infraestrutura.
* **Melhoria**: Crie classes DTO de resposta específicas (ex: `UserResponseDTO`, `WalletResponseDTO`) e mapeie-as utilizando bibliotecas como MapStruct ou ModelMapper.

---

## 5. Uso de `DriverManagerDataSource` em Produção
No arquivo [DataSourceConfig.java](../src/main/java/br/com/wferreiracosta/louis/configs/DataSourceConfig.java), está sendo retornado um `DriverManagerDataSource`:
```java
final var dataSource = new DriverManagerDataSource();
```
* **Problema**: O `DriverManagerDataSource` não realiza pool de conexões. Ele abre e fecha uma nova conexão física com o banco de dados para cada consulta realizada, gerando uma sobrecarga severa de desempenho.
* **Melhoria**: Remova essa configuração manual ou configure um pool real como o **HikariCP** (o padrão autoconfigurado pelo Spring Boot).

---

## 6. Detalhes e Erros de Escrita (Typos)
* **Nome do Arquivo e da Classe**: O repositório [TransactionRespository.java](../src/main/java/br/com/wferreiracosta/louis/repositories/TransactionRespository.java) está escrito com um `s` extra (**Respository** em vez de **Repository**). Recomenda-se renomear a classe e o arquivo para `TransactionRepository`.
* **Open-In-View Habilitado**: Nos logs do Spring, há um alerta sobre a propriedade `spring.jpa.open-in-view` estar ativada por padrão. É recomendado desativá-la no [application.properties](../src/main/resources/application.properties) adicionando:
  ```properties
  spring.jpa.open-in-view=false
  ```
* **Dependência Não Utilizada**: A biblioteca `spring-cloud-starter-openfeign` está listada no [pom.xml](../pom.xml), porém não há nenhuma anotação ou interface utilizando OpenFeign no código. Se não for necessária, remova-a para reduzir o tamanho do build.
