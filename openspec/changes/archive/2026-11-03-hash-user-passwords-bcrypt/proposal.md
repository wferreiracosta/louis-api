## Why

O cadastro armazena senhas em texto puro, expondo credenciais em caso de acesso indevido ao banco. O plano `docs/plans/010-hash-senhas-bcrypt.md` será aplicado aos novos cadastros e, conforme escopo confirmado pelo usuário, aos registros existentes.

## What Changes

- Gerar hash BCrypt antes da persistência de usuários comuns e lojistas, preservando carteira, unicidade e acesso anônimo aos endpoints atuais.
- Remover o mapeamento direto de senha recebida para a entidade e manter credenciais fora de respostas e logs, inclusive erros de parsing.
- Entregar migração operacional explícita, retomável e verificável das senhas existentes, com marcador de formato persistido atomicamente com o hash. Não inferir formato pelo conteúdo da senha.
- **BREAKING**: limitar novas senhas a 72 bytes UTF-8, retornando HTTP 400 para valores maiores. O usuário aprovou essa restrição e o bloqueio da migração, antes de qualquer alteração de dados, se houver senha antiga nula ou acima do limite.
- Entregar pré-validação e ferramenta de execução em manutenção. Scripts SQL e guia operacional foram removidos a pedido do usuário. Não executar a migração de ambientes compartilhados como parte da implementação.

## Capabilities

### New Capabilities

- `user/password-storage`: proteção das senhas no cadastro, migração do legado, validação de tamanho e não exposição de credenciais.

### Modified Capabilities

- Nenhuma. Os contratos de `user/registration-uniqueness` e `persistence/schema-management` permanecem; a nova restrição de senha complementa as validações existentes.

## Impact

Afeta `pom.xml`, configuração de segurança, serviço e entidade de usuário, validação de entrada, tratamento de erros e testes. Acrescenta metadado de formato à tabela `users`, ferramenta de migração. QA/produção continuam com `ddl-auto=validate`, exigindo preparação explícita do schema. Introduz Spring Security conforme o plano, com configuração temporária de acesso aberto; Inclui atualização para Spring Boot 3.5.16 e springdoc 2.8.17, autorizada pelo usuário para obter Spring Security corrigido com módulos alinhados. JWT, login e reset de senha ficam fora do escopo. Hash é irreversível: implantação deve impedir retorno de escritores que persistam texto puro.
