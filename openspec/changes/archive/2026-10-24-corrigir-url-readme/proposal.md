## Why

O comando de clone em `README.md` aponta para `jonah-api`, levando quem segue o onboarding ao projeto errado. O plano `docs/plans/009-corrigir-url-readme.md` identifica a correção, e o remoto `origin` confirma o destino `https://github.com/wferreiracosta/louis-api.git`.

## What Changes

- Substituir a URL do comando `git clone` em Getting Started por `https://github.com/wferreiracosta/louis-api.git`.
- Verificar o acesso ao repositório de destino durante a implementação.
- Revisar o restante do README em busca de referências copiadas de outros projetos, conferindo nomes de serviços, portas e nomes de secrets contra os arquivos locais. Registrar achados adicionais para acompanhamento separado, sem ampliar esta correção.

## Capabilities

### New Capabilities

Nenhuma. Alteração exclusivamente documental, com `skip_specs: true`.

### Modified Capabilities

Nenhuma. Os requisitos e o comportamento da aplicação permanecem inalterados.

## Impact

A implementação afeta apenas a URL de clone em `README.md`. Não altera código, APIs, dependências, configuração ou infraestrutura. A acessibilidade remota ainda será verificada; a correspondência com `origin` foi confirmada localmente. Não é necessário executar a suíte da aplicação para esta edição documental.
