# Conventional Commits

## Purpose

Generate and validate Git commit messages following the project's Conventional Commits convention.

## Commit Format

```text
<type>: <message>
```

Example:

```text
test: add test for create product automation
```

## Commit Types

Use the type that represents the **actual nature of the change**, not the files that were modified.

| Type | Use when | Example |
|---|---|---|
| `test` | Creating or modifying test code | `test: add unit tests for TransactionService` |
| `feat` | Adding a new feature, service, functionality, or endpoint | `feat: add actuator healthcheck endpoint` |
| `refactor` | Restructuring code without changing business behavior | `refactor: move uniqueness validations to UserServiceImpl` |
| `style` | Formatting or style changes that do not affect behavior | `style: remove trailing whitespace from entity classes` |
| `fix` | Fixing a bug or incorrect system behavior | `fix: prevent deadlock by ordering pessimistic locks` |
| `chore` | Project maintenance that does not affect application behavior or tests | `chore: add .env to .gitignore` |
| `docs` | Documentation changes | `docs: add environment variables reference to README` |
| `build` | Build process or external dependency changes | `build: add multi-stage Dockerfile` |
| `perf` | Changes specifically intended to improve performance | `perf: configure HikariCP connection pool settings` |
| `ci` | CI/CD configuration changes | `ci: add workflow to run tests on pull request` |
| `revert` | Reverting a previous commit | `revert: revert "feat: add wallet endpoint"` |

## Message Rules

Every commit message MUST:

1. Be written in English.
2. Use the imperative form.
3. Start with a lowercase letter.
4. Not end with a period.
5. Be concise and descriptive.
6. Keep the message portion at or below 72 characters.
7. Use the commit type that accurately describes the change.
8. Follow exactly this structure:

```text
<type>: <message>
```

Do not add unnecessary scopes, prefixes, ticket numbers, or additional formatting unless explicitly requested.

## GitHub Issue Linking

Before generating or confirming the final commit message, ask the user:

> Does this commit need to automatically close a GitHub issue? If so, provide the issue number (e.g. #42). Otherwise, press Enter or answer "no".

If the user provides one or more issues, append the appropriate GitHub closing keyword to the end of the commit message.

### Issue keyword rules

For `fix`:

```text
fix: prevent duplicate transaction processing (fixes #42)
```

For `feat`, `refactor`, `perf`, and other commit types:

```text
feat: add transaction retry mechanism (closes #42)
```

or:

```text
feat: add transaction retry mechanism (resolves #42)
```

For multiple issues:

```text
fix: prevent duplicate transaction processing (closes #12, fixes #15)
```

If the user does not provide an issue, do not add an issue reference.

## Validation

Before returning a final commit message, verify:

- [ ] The type is valid.
- [ ] The type reflects the actual nature of the change.
- [ ] The message is in English.
- [ ] The message uses imperative form.
- [ ] The message starts with lowercase.
- [ ] The message does not end with a period.
- [ ] The message portion is no longer than 72 characters.
- [ ] The format is `<type>: <message>`.
- [ ] The GitHub issue requirement has been addressed.

## Examples

Valid:

```text
feat: add actuator healthcheck endpoint
fix: prevent deadlock by ordering pessimistic locks
test: add unit tests for TransactionService
refactor: extract transaction validation service
docs: add environment variables reference to README
build: add multi-stage Dockerfile
ci: add workflow to run tests on pull request
```

Invalid:

```text
Feat: Add actuator healthcheck endpoint
```

Reason: type and message are not lowercase.

```text
feat: added actuator healthcheck endpoint
```

Reason: message is not imperative.

```text
fix: prevent deadlock by ordering pessimistic locks.
```

Reason: message ends with a period.

```text
chore: modify TransactionService
```

Reason: the type should describe the nature of the change, not merely the fact that a file was modified.

## Commit Generation Workflow

When asked to create a commit message:

1. Inspect the changes and determine their actual nature.
2. Select the appropriate commit type.
3. Write the message in English using the imperative form.
4. Keep the message concise and within 72 characters.
5. Ask whether the commit should automatically close a GitHub issue.
6. If an issue is provided, append the appropriate closing keyword.
7. Validate the final message against all rules.
8. Return the final commit message.

Reference: Conventional Commits Pattern