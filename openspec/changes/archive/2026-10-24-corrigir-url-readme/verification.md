# Verificação — 2026-10-24

- `git diff -- README.md`: apenas a URL do comando de clone foi alterada; o bloco Bash foi preservado.
- `git ls-remote https://github.com/wferreiracosta/louis-api.git HEAD`: sucesso, retornando `29004e29e7e8d99f568d3dc7b810290a775732b1 HEAD`. A primeira tentativa no sandbox falhou por resolução de DNS; a execução com acesso de rede confirmou o destino.
- `rg -n 'jonah-api' README.md`: nenhuma ocorrência, código de saída 1 esperado.
- Revisão integral do README: não foram identificadas outras referências copiadas de outro projeto. O container `louis-api-ministack` e o caminho `/etc/ministack/init/ready.d/init-aws.sh` correspondem ao `docker-compose.yaml`. O README não cita nomes de secrets. A porta 8080 não é sobrescrita nas configurações da aplicação; a dependência Springdoc está presente. Não foi realizado teste de inicialização da API.
- Acompanhamento separado: o Compose contém apenas PostgreSQL e Ministack, embora o onboarding sugira que seu comando inicia a API. A ausência do serviço da aplicação já está documentada em `docs/plans/015-docker-compose-servico-aplicacao.md`. A rede externa `global-default` também pressupõe preparação prévia não explicada no README; incluir esse pré-requisito em uma futura revisão do onboarding.
- Não foi executada a suíte da aplicação, pois a alteração é exclusivamente documental.
