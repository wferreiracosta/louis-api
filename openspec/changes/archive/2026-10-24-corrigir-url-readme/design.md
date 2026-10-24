## Context

Ver `proposal.md` para a motivação. Este documento registra a abordagem já aplicada e foi criado por solicitação do usuário antes do arquivamento. O destino HTTPS foi confirmado pelo remoto `origin` e pela consulta ao GitHub registrada em `verification.md`.

## Goals / Non-Goals

**Goals:** Preservar a estrutura Markdown e o comando de onboarding, com uma alteração de uma linha facilmente verificável no diff.

**Non-Goals:** Alterar a execução da aplicação, configurar Docker ou corrigir outras lacunas do onboarding nesta change.

## Decisions

- Usar `https://github.com/wferreiracosta/louis-api.git`, mantendo o protocolo HTTPS existente. A alternativa SSH exigiria configuração de chaves sem necessidade para esta correção.
- Substituir somente a URL no bloco Bash. Uma reescrita do onboarding ampliaria o escopo; os achados sobre Compose e rede externa ficam registrados em `verification.md` para acompanhamento separado.
- Validar com revisão do diff, busca pela referência antiga e `git ls-remote` ao destino. Clonar todo o repositório seria desnecessário para confirmar sua acessibilidade. A suíte da aplicação não exercita este texto documental.
- Manter `skip_specs: true`, pois nenhum requisito de comportamento da aplicação foi modificado.

## Risks / Trade-offs

- Falha de rede pode impedir a verificação remota → registrar a limitação e não confundi-la com URL incorreta. A consulta realizada retornou HEAD com sucesso.
- O comando de clone correto não garante o funcionamento integral do onboarding → registrar as lacunas encontradas separadamente e limitar a conclusão à verificação realizada.

## Migration Plan

Não há migração nem implantação de serviços. A correção acompanha a publicação do README no repositório. Uma eventual reversão consiste em reverter a edição da URL, embora isso restaure o destino incorreto anterior.
