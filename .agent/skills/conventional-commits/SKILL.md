---
name: conventional-commits
description: Generate and validate Git commit messages according to the project's Conventional Commits rules. Use when creating, reviewing, or validating commit messages.
---

# Conventional Commits

When creating or validating a Git commit message, follow these rules.

## Format

```text
<type>: <message>
```

Example:

```text
test: add test for create product automation
```

## Types

| Type | When to use |
|---|---|
| `test` | Create or modify test code |
| `feat` | Add a new feature, service, functionality, or endpoint |
| `refactor` | Refactor code without changing business behavior |
| `style` | Change formatting or style without changing behavior |
| `fix` | Fix a bug or incorrect behavior |
| `chore` | Project maintenance that does not affect the system or tests |
| `docs` | Change documentation |
| `build` | Change build process or external dependencies |
| `perf` | Improve system performance |
| `ci` | Change CI/CD configuration |
| `revert` | Revert a previous commit |

## Rules

The commit message MUST:

- Be written in English.
- Use the imperative form.
- Start with lowercase.
- Not end with a period.
- Be objective and descriptive.
- Keep the message portion within 72 characters.
- Use the type that represents the actual nature of the change.
- Follow `<type>: <message>` exactly.

Do not select the type based only on the files changed.

## Analyze Changes First

When the commit is based on existing changes:

1. Inspect the working tree and relevant diff.
2. Identify the primary purpose of the changes.
3. Select the commit type based on that purpose.
4. Write the shortest accurate imperative description.
5. Validate the resulting message.

Do not invent changes that are not present in the diff.

## GitHub Issues

Before generating or confirming the final commit message, ask:

> Does this commit need to automatically close a GitHub issue? If so, provide the issue number (e.g. #42). Otherwise, press Enter or answer "no".

If the user provides an issue:

### `fix`

Use:

```text
(fixes #<number>)
```

Example:

```text
fix: prevent duplicate transaction processing (fixes #42)
```

### Other types

For `feat`, `refactor`, `perf`, and other types, use either:

```text
(closes #<number>)
```

or:

```text
(resolves #<number>)
```

Example:

```text
feat: add transaction retry mechanism (closes #42)
```

### Multiple issues

Use:

```text
(closes #12, fixes #15)
```

If no issue is provided, do not add an issue reference.

## Validation Checklist

Before presenting the final message, verify:

- Valid commit type.
- Type matches the actual change.
- English language.
- Imperative form.
- Lowercase beginning.
- No final period.
- Maximum 72 characters for the message portion.
- Correct `<type>: <message>` structure.
- GitHub issue handling completed.

## Examples

```text
feat: add actuator healthcheck endpoint
fix: prevent deadlock by ordering pessimistic locks
test: add unit tests for TransactionService
refactor: extract transaction validation service
style: remove trailing whitespace from entity classes
chore: add .env to .gitignore
docs: add environment variables reference to README
build: add multi-stage Dockerfile
perf: configure HikariCP connection pool settings
ci: add workflow to run tests on pull request
revert: revert "feat: add wallet endpoint"
```

Reference: Conventional Commits Pattern