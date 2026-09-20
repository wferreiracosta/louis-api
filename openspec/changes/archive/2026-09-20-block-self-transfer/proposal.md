## Why

`TransactionServiceImpl.transfer()` does not validate whether `payer` and `payee` refer to the same user. This makes the self-transfer scenario reachable in production: the deadlock-prevention logic introduced in plan 003 (`Math.min`/`Math.max`) collapses to the same ID when both values are equal, resulting in two pessimistic locks on the same row — undefined behavior in PostgreSQL — while also creating semantically meaningless ledger records that debit and credit the same wallet with no net effect.

## What Changes

- Add an early guard in `TransactionServiceImpl.transfer()` that compares `parameter.payer()` and `parameter.payee()` **before** any database access.
- If they are equal, throw `BusinessValidationException` with field `"payer"` and message `"Self-transfer is not allowed"`, returning HTTP 422 to the caller.
- No schema changes, no new entities, no API surface changes.

## Capabilities

### New Capabilities

- `transaction/self-transfer-validation`: The system must reject transfer requests where payer and payee identify the same user.

### Modified Capabilities

<!-- No existing spec-level requirements are changing. The deadlock-prevention spec remains valid as-is; it assumes distinct payer/payee, which this change now enforces as a pre-condition. -->

## Impact

- **`TransactionServiceImpl.java`**: single guard added at the top of `transfer()`.
- **API**: `POST /transaction` with equal `payer`/`payee` values now returns HTTP 422.
- **Tests**: new unit test for the self-transfer rejection path.
- No database migrations, no changes to shared utilities or other services.
