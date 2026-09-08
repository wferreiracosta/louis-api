# 009 — Corrigir URL de Clone no `README.md`

**Sprint:** 🟠 2 — Limpeza Técnica  
**Categoria:** Documentação  
**Impacto:** 🟢 Baixo  
**Esforço:** ⏱️ Baixo

**Commit:** `docs: fix clone URL in README`  
**Branch:** `docs/9`

---

## Problema

Em [`README.md`](../../README.md), a instrução de clone aponta para o repositório errado:

```bash
# Errado
git clone https://github.com/wferreiracosta/jonah-api.git
```

O nome `jonah-api` indica que o README foi copiado de outro projeto sem atualizar a URL. Qualquer desenvolvedor que seguir o guia de onboarding irá clonar o repositório incorreto.

---

## Solução

Corrigir a URL para apontar para o repositório correto do `louis-api`.

### Arquivo: `README.md`

```markdown
## Getting Started

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/wferreiracosta/louis-api.git
    ```
```

---

## Verificação

- [ ] Confirmar que a URL corrigida está acessível no GitHub
- [ ] Revisar o restante do `README.md` para outros possíveis resquícios de cópia de outros projetos (nomes de serviço, ports, nomes de secrets, etc.)
