## 1. Correção da URL de clone

- [x] 1.1 Substituir somente `https://github.com/wferreiracosta/jonah-api.git` por `https://github.com/wferreiracosta/louis-api.git` no comando de clone de Getting Started em `README.md`; verificar com `git diff -- README.md` que o bloco Bash continua íntegro e que apenas a URL mudou.
- [x] 1.2 Confirmar acesso ao destino com `git ls-remote https://github.com/wferreiracosta/louis-api.git HEAD`; verificar retorno bem-sucedido e uma referência HEAD, registrando o resultado. Se houver bloqueio de rede ou autenticação, registrar a limitação e manter esta verificação pendente, sem interpretar a falha como prova de URL incorreta.

## 2. Revisão documental

- [x] 2.1 Revisar todo o README procurando referências residuais de outros projetos, incluindo nomes de serviços, portas e nomes de secrets; comparar as referências existentes com os arquivos de configuração locais e registrar os achados ou sua ausência no relatório de conclusão. Confirmar com `rg -n 'jonah-api' README.md` que não há ocorrências (código de saída 1 esperado); encaminhar inconsistências adicionais para acompanhamento separado.

Specs dispensadas por `skip_specs: true`. A abordagem aplicada está registrada em `design.md`, criado por solicitação do usuário antes do arquivamento.
