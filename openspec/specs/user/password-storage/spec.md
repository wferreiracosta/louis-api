# user/password-storage Specification

## Purpose

Proteger as credenciais de usuários comuns e lojistas no cadastro e converter o legado de forma retomável, sem exposição de senhas ou perda silenciosa de conteúdo.

## Requirements

### Requirement: Hash de senha antes da persistência
O sistema MUST persistir somente um hash BCrypt com salt individual para cada novo cadastro válido, inclusive chamadas diretas ao serviço de cadastro, e MUST registrar explicitamente o formato da credencial. A senha recebida MUST ser tratada como entrada original mesmo quando seu conteúdo se assemelha a um hash.

#### Scenario: Cadastro de qualquer tipo
- **WHEN** um usuário comum ou lojista é cadastrado com dados válidos e disponíveis
- **THEN** a senha persistida difere da entrada e a verificação BCrypt da entrada contra o valor persistido tem sucesso
- **AND** uma senha diferente não é validada contra esse hash
- **AND** o tipo e a carteira associada com saldo zero são preservados

#### Scenario: Salt individual
- **WHEN** dois usuários distintos são cadastrados com a mesma senha
- **THEN** seus hashes persistidos são diferentes e ambos validam a senha original

#### Scenario: Entrada semelhante a hash
- **WHEN** uma senha válida fornecida pelo usuário possui aparência de hash BCrypt
- **THEN** ela é codificada como qualquer outra senha e não é persistida diretamente

### Requirement: Limite de senha em bytes UTF-8
O sistema MUST rejeitar novas senhas acima de 72 bytes UTF-8, sem truncamento, normalização ou persistência de usuário/carteira. Os endpoints de cadastro MUST responder HTTP 400 no formato de validação existente, com erro de campo `password` e mensagem `Password must be at most 72 UTF-8 bytes`. O limite MUST valer também em chamadas diretas ao serviço. As validações de campos obrigatórios continuam vigentes.

#### Scenario: Limite exato
- **WHEN** um cadastro válido contém senha com exatamente 72 bytes UTF-8
- **THEN** o cadastro é aceito e o hash valida a senha completa

#### Scenario: Limite excedido
- **WHEN** um cadastro contém senha com mais de 72 bytes UTF-8, incluindo caracteres multibyte
- **THEN** a operação é rejeitada pelo limite antes das consultas de unicidade e nenhuma entidade é persistida
- **AND** na API a resposta é HTTP 400 com o erro de campo especificado

### Requirement: Compatibilidade dos endpoints
O sistema MUST manter o acesso anônimo dos endpoints existentes, sem exigir credenciais ou token CSRF. Para entradas dentro do limite, MUST preservar HTTP 201 no cadastro válido, HTTP 400 nas validações estruturais e HTTP 422 nos conflitos prévios de unicidade, com os corpos existentes.

#### Scenario: Requisição anônima
- **WHEN** um cliente invoca os endpoints existentes sem autenticação nem token CSRF
- **THEN** a requisição alcança as regras atuais da aplicação e não é bloqueada nem redirecionada pela configuração de segurança

#### Scenario: Conflitos existentes
- **WHEN** um cadastro com senha válida possui documento e e-mail já utilizados
- **THEN** retorna HTTP 422 com ambos os erros na ordem atual e não grava usuário nem carteira

### Requirement: Migração explícita do legado
O sistema MUST disponibilizar migração acionada explicitamente em manutenção, sem executá-la na inicialização normal. Antes de qualquer atualização de credenciais, MUST validar todo o conjunto e abortar sem alterações se houver senha legada nula ou maior que 72 bytes UTF-8, marcador desconhecido ou hash inválido marcado como convertido. A preparação de schema é uma etapa separada. A identificação do legado MUST usar metadado explícito, nunca somente prefixo ou aparência da senha.

#### Scenario: Base legada válida
- **WHEN** a migração é executada sobre usuários legados comuns e lojistas com senhas suportadas
- **THEN** todas as senhas legadas são convertidas para BCrypt, preservando a verificação da entrada completa
- **AND** IDs, demais campos dos usuários, carteiras, saldos e transações permanecem inalterados

#### Scenario: Pré-validação bloqueia o conjunto inteiro
- **WHEN** qualquer registro apresenta uma das condições impeditivas, inclusive no último lote examinado
- **THEN** a migração termina com falha antes de atualizar qualquer credencial
- **AND** informa contagens e identificadores dos registros problemáticos sem revelar senhas ou hashes

#### Scenario: Senha legada com aparência de hash
- **WHEN** um registro marcado como legado contém um texto que se parece com um hash BCrypt válido
- **THEN** esse texto é convertido como senha original, sem ser confundido com registro já migrado

#### Scenario: Inicialização normal
- **WHEN** a aplicação inicia sem a opção explícita de migração
- **THEN** não executa conversão de senhas existentes

### Requirement: Retomada e conclusão verificável
A migração MUST atualizar hash e marcador de formato atomicamente, preservar hashes já convertidos em reexecuções e permitir retomada após falha. MUST processar os dados em lotes limitados e MUST reportar sucesso somente quando a verificação final não encontrar registros pendentes ou inconsistentes. A execução MUST impedir duas instâncias migradoras de modificar o mesmo conjunto simultaneamente; escritores normais devem permanecer suspensos durante a operação.

#### Scenario: Interrupção durante a migração
- **WHEN** ocorre falha após alguns lotes confirmados
- **THEN** os lotes confirmados permanecem convertidos e o lote em falha é revertido integralmente
- **AND** a próxima execução converte somente os registros restantes

#### Scenario: Reexecução em base convertida
- **WHEN** a migração é executada novamente após sucesso
- **THEN** nenhum hash é alterado e a conclusão informa zero registros pendentes

#### Scenario: Segunda instância
- **WHEN** outra instância tenta migrar a mesma base enquanto uma execução está ativa
- **THEN** ela termina sem atualizar credenciais e informa que existe uma migração em andamento

### Requirement: Não exposição de credenciais
O sistema MUST omitir senhas, hashes e marcadores internos das respostas de API e MUST evitar sua emissão em logs, mensagens de erro e relatórios da migração. Isso inclui JSON inválido e falhas de desserialização contendo valores de senha.

#### Scenario: Respostas normais
- **WHEN** o cliente cadastra ou consulta usuários e suas carteiras
- **THEN** o corpo não contém senha, hash nem metadado de formato de credencial

#### Scenario: Falha contendo entrada sensível
- **WHEN** uma requisição contém JSON inválido ou um valor incompatível com o tipo esperado no campo de senha
- **THEN** a API mantém HTTP 400 com mensagem genérica e não reproduz a entrada sensível na resposta ou nos logs

#### Scenario: Relatório operacional
- **WHEN** a migração conclui, falha ou encontra dados incompatíveis
- **THEN** o relatório contém somente status, contagens e identificadores necessários ao diagnóstico, sem credenciais
