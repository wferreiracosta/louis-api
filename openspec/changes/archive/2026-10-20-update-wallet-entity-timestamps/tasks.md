## 1. Mapeamento de timestamps de persistência

- [x] 1.1 Configurar o modelo de persistência da carteira para preencher seu timestamp de criação na inserção e renovar seu timestamp de atualização na modificação; verificar que uma carteira recém-persistida possui timestamp de criação não nulo e que uma carteira persistida e atualizada possui timestamp de atualização não nulo.

## 2. Verificação de comportamento

- [x] 2.1 Adicionar ou atualizar a cobertura no nível de persistência para criação de carteiras e atualizações de saldo; verificar que o teste automatizado confirma que o timestamp de atualização avança após uma alteração de saldo persistida.
- [x] 2.2 Verificar que uma transferência concluída renova os timestamps de atualização das duas carteiras participantes; verificar, pelo teste de integração relevante ou por registros persistidos no banco de dados, que ambos os timestamps diferem dos respectivos timestamps de criação após a transferência.
