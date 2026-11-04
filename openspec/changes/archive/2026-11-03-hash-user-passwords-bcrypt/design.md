## Context

Ver `proposal.md` para motivação e escopo. O projeto usa Java 17, Spring Boot 3.1.5, JPA, PostgreSQL e H2 nos testes. `UserServiceImpl.save` verifica unicidade, cria carteira e chama `new UserEntity(parameter)`, que copia a senha sem proteção. Não há mecanismo de migração versionada configurado; QA/produção usam `ddl-auto=validate`.

`UserResponseDTO` já omite a senha e `UserEntity.password` possui `@JsonIgnore`. `GlobalExceptionHandler` devolve `getLocalizedMessage()` para erros de leitura HTTP. Há testes que comparam senha salva com texto puro e outros que instanciam `UserServiceImpl` diretamente.

## Goals / Non-Goals

**Goals:** centralizar encoding, tornar a migração independente da inicialização normal e representar explicitamente o estado de cada credencial. Permitir diagnóstico sem exportar credenciais e retomar lotes confirmados.

**Non-Goals:** JWT, autenticação, reset de senha, remoção automática de usuários inválidos, migração de backups, implantação em ambientes compartilhados e migração para Spring Boot 4. Não adicionar um framework completo de migrações para esta operação pontual.

## Decisions

### 1. BCrypt na camada de serviço e configuração HTTP explícita

Adicionar o starter de segurança previsto no plano e um bean `PasswordEncoder` baseado em `BCryptPasswordEncoder`, com versão `$2a` e custo 10 explícitos. Atualizar para Spring Boot 3.5.16 e springdoc 2.8.17, conforme ampliação de escopo autorizada pelo usuário. Usar as versões de Spring Security gerenciadas pelo BOM do Boot, verificando a árvore Maven; não introduzir deliberadamente uma versão com o problema de comprimento descrito na referência abaixo.

O serviço valida o limite, mantém as duas consultas de unicidade e só então codifica a senha uma vez, monta a entidade com hash e marcador `BCRYPT` e preserva a associação bidirecional com a carteira. Remover o construtor que recebe `UserParameter`. Não mover encoding para callbacks JPA ou setters, pois isso tornaria regravações suscetíveis a dupla codificação. Falha no encoding interrompe a operação antes da persistência.

Configurar `SecurityFilterChain` com `permitAll`, CSRF desabilitado e sem form login/HTTP Basic. Testar a cadeia real, sem `addFilters=false`. Alternativa considerada: somente `spring-security-crypto`, que evitaria filtros; manter o starter para seguir a direção do plano fornecido e registrar explicitamente a configuração transitória.

### 2. Limite em bytes e mensagens sem conteúdo sensível

Criar validação de no máximo 72 bytes usando UTF-8, sem trim ou normalização. Usar a mesma regra no parâmetro HTTP e no início do serviço, antes da unicidade. `@Size` sozinho não é adequado porque conta caracteres. A anotação de limite deve deixar valores nulos para `@NotBlank`; o serviço deve rejeitar entradas nulas/em branco também. Preservar mensagens obrigatórias existentes e usar `Password must be at most 72 UTF-8 bytes` para excesso.

A decisão de rejeitar acima do limite e bloquear a migração nesses casos foi confirmada pelo usuário. Não truncar nem introduzir pré-hash, que alteraria a futura verificação de credenciais. Trocar detalhes de exceção de JSON por mensagem estável `Malformed JSON request`, mantendo status e estrutura do erro. Não registrar parâmetros, hashes, exceções contendo entrada nem valores de bind JDBC/Hibernate. Marcador também recebe exclusão de serialização.

Referências: [armazenamento de senhas no Spring](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html), [limite de BCrypt](https://github.com/spring-projects/spring-security/blob/main/crypto/src/main/java/org/springframework/security/crypto/bcrypt/BCrypt.java), [aviso sobre verificação de comprimento](https://spring.io/security/cve-2025-22228/). Confirmar comportamento com a versão efetivamente resolvida no projeto, não presumir equivalência com o ramo principal dessas referências.

### 3. Metadado explícito em vez de reconhecer prefixos

Adicionar `users.password_encoding`, VARCHAR(16), nullable, sem default `BCRYPT`. `NULL` identifica legado e `BCRYPT` identifica credencial convertida. Um novo cadastro grava hash e `BCRYPT` juntos. A preparação do schema fica sob responsabilidade do ambiente; scripts SQL e guia operacional foram removidos a pedido do usuário. O teste prepara a coluna diretamente em seu próprio schema descartável.

A fonte atual só grava texto puro; portanto, registros preexistentes sem marcador são tratados como texto puro, mesmo se parecem hashes completos. Não existe classificação inequívoca baseada em regex. Se houver dados provenientes de outra versão que já codificava senhas, sua proveniência precisa ser reconciliada antes da operação; não adivinhar pelo conteúdo. Metadado desconhecido bloqueia a execução. Hash marcado `BCRYPT` deve ter estrutura, versão e custo válidos e suportados; aparência válida não comprova a senha original.

Alternativa rejeitada: tabela externa de IDs migrados, que acrescentaria outra fonte de estado. A coluna mantém hash e formato na mesma atualização transacional. Não depender de DDL automático em QA/produção.

### 4. Comando operacional isolado, em duas passagens

Entregar entrada Java própria para migração, executada explicitamente, com contexto Spring mínimo para datasource e encoder e sem servidor HTTP, controllers ou cadeia web. Documentar o comando exato compatível com o empacotamento Maven. Reutilizar a resolução existente de credenciais de banco; não passar senhas na linha de comando. Incluir modo somente verificação e códigos de saída zero para sucesso e não zero para falha/bloqueio.

Executar exclusivamente em janela de manutenção, com todas as instâncias escritoras paradas. Obter trava consultiva de sessão PostgreSQL em conexão dedicada, mantida até o encerramento; uma segunda execução deve falhar sem escrever. Essa trava coordena migradores, não substitui a suspensão de escritores. Liberar a trava em `finally`; encerramento da conexão deve liberar a sessão. Testar essa parte em PostgreSQL descartável, sem simular semântica de PostgreSQL apenas no H2.

Primeira passagem: paginação por chave `id`, lotes limitados (default 100, configurável positivo), apenas leitura. Validar todos os registros, inclusive hashes marcados; identificar nulos e textos legados acima de 72 bytes. Em erro, emitir somente IDs/motivos/contagens, sem atualizar nada. Strings legadas vazias ou em branco, se existirem, são codificadas literalmente; a migração preserva credenciais históricas sem aplicar retroativamente a política de cadastro.

Segunda passagem: processar apenas `password_encoding IS NULL`. Cada lote tem transação própria; bloquear/reler as linhas antes de codificar, atualizar somente senha e marcador e confirmar o lote. Nunca usar caminhos de cadastro ou callbacks que alterem carteiras. O hash e marcador são gravados juntos; erro reverte o lote, encerra com falha e mantém lotes anteriores. Sem auto-retry infinito.

Verificação final: nenhuma credencial legada, marcador desconhecido ou hash inválido deve restar. Reportar contagens de convertidos, já convertidos e pendentes. Uma nova execução pula registros marcados e não modifica seus hashes. O modo somente verificação permite inspeção anterior sem converter valores.

## Risks / Trade-offs

- Custo de CPU e duração da manutenção → medir em base descartável representativa; lotes limitam memória e duração de transações, mas BCrypt continua custoso por registro.
- Escritor antigo após migração → suspender todas as instâncias antes da operação e retomar somente a versão que grava hash e marcador; a trava consultiva não protege de aplicações antigas.
- Dados incompatíveis → pré-validação bloqueia o conjunto inteiro; correção exige procedimento separado e decisão operacional, sem descartar contas ou substituir senhas silenciosamente.
- Falha após lotes confirmados → preservar conversões concluídas e retomar; não tentar recuperar texto puro a partir do hash.
- Coluna ausente em QA/produção → executar expansão explícita antes do deploy e validar schema; `ddl-auto=validate` permanece.
- Backups anteriores ainda contêm texto puro → esta mudança converte a base ativa; aplicar controles e retenção existentes aos backups, sem prometer eliminá-los automaticamente.
- Atualização do conjunto Spring Security pode exigir ajustes pontuais → verificar dependências e testes de contexto; a atualização para Boot 3.5.16 está autorizada; verificar também OpenAPI e regressões de persistência.

## Migration Plan

1. Entregar a ferramenta; ensaiar em banco PostgreSQL descartável com registros sintéticos mistos e falhas induzidas. A implementação não executa operações em produção.
2. Na implantação, entrar em manutenção, parar escritores e preparar recuperação conforme política existente. Preparar explicitamente a coluna de formato no schema; verificar capacidade da coluna de senha para o hash de 60 caracteres.
3. Executar somente verificação. Se houver impeditivos, parar sem conversão, resolver o conjunto incompatível por procedimento separado e repetir a verificação completa.
4. Executar migração com a versão nova, sob trava exclusiva dos migradores. Em falha, manter manutenção e reexecutar para retomar lotes pendentes.
5. Confirmar zero pendências, hashes válidos e invariantes de usuários/carteiras/transações. Iniciar apenas a versão nova e verificar cadastros anônimos para ambos os tipos.
6. Antes da conversão, é possível reverter o binário mantendo a coluna adicional. Após converter qualquer lote, preferir correção progressiva e retomada: não voltar a escritores antigos nem converter hashes para texto. Restauração de backup é recuperação excepcional fora desta automação, pois reintroduz dados sensíveis e pode perder alterações.

## Melhorias futuras dos testes

Na revisão de 03/11/2026, ficaram identificadas duas melhorias para trabalho posterior: habilitar PostgreSQL descartável no CI para executar os testes condicionados a `BCRYPT_TEST_JDBC_URL` e mover o teste do JAR para uma fase posterior ao empacotamento (por exemplo, Maven Failsafe), evitando uso de artefato antigo. Os testes PostgreSQL existentes foram mantidos. A suíte padrão continua usando H2; o arquivamento não significa que essas melhorias de automação foram implementadas.
