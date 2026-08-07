# JustWarp Development Guide

## Principles

- Keep each class focused on one responsibility (SRP).
- Prefer the smallest readable solution (KISS), and centralize shared validation and messaging (DRY).
- Keep the mod server-side. Do not introduce client-only APIs, custom packets, or client installation requirements.
- Treat persisted JSON as user data: validate before replacing live state and never overwrite malformed files during reload.

## Architecture

- `command`: Brigadier registration and command-facing validation only. Create human-text and opaque-tail arguments through `CommandArguments`, and centralize player-only checks and command messaging in `CommandSupport`.
- `domain`: immutable warp, group, location, and configuration values.
- `persistence`: JSON parsing, validation, default-file creation, and atomic writes.
- `service`: mutations, teleport policy, reload, translations, and shared player location capture through `PlayerLocations`.
- `gui`: inventory rendering and click routing.

Commands and GUI must call the same service methods. Persistence code must not contain command, GUI, or teleport logic.

Keep mutation services separate when their atomic write boundaries differ. Keep GUI click handling local while only a small number of flows use it, and keep persistence parsing explicit while the schema remains small and strongly validated.

## Commands and autocomplete

- Use Brigadier-native, client-synchronizable argument types so command hints and completion work without a client mod.
- Route names, authors, and descriptions through the shared quotable-string policy. Unicode and special-character input is supported when quoted; names must still satisfy the domain rule that forbids whitespace.
- Escape dynamic completion values with Brigadier's `StringArgumentType.escapeIfRequired` so Chinese and other values that require quoting can be inserted and executed unchanged.
- Attach localized tooltips to dynamic suggestions. Read mutable warp, group, icon, and related values from the current service state for each request; cache only immutable vanilla registry candidates.
- Reserve greedy strings for opaque final tails such as Base64 payloads. Do not add parallel greedy fallback branches beside typed command paths because overlapping branches make parsing and completion inconsistent.
- Add command-facing validation or player-location logic to the existing shared helpers instead of duplicating it in registrations.

## Localization and persisted data

- Put user-facing command, GUI, validation, and suggestion text behind `Translations` and respect the configured server locale.
- Add every translation key to both `en_us` and `zh_tw`; the locale key-set test must continue to enforce parity.
- Treat persisted JSON as ordered user data. Preserve array order, namespaced item and dimension identifiers, and valid in-memory state when a reload candidate is malformed.
- Validate the complete candidate state before replacing live state, and use atomic writes for mutations. Reload must never overwrite malformed user files.

## Compatibility and verification

- Target Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.156.0+26.2, and Java 25.
- Persist item and dimension identifiers in namespaced form.
- Preserve JSON array order because it controls GUI order.
- Keep `task.md` current as work proceeds. Update help and examples whenever command syntax or behavior changes.
- Add focused tests for changed behavior, including quoted Chinese completion/execution and localized suggestion tooltips when command input is affected.
- Before completing a change, run `gradlew test`, `gradlew build`, and `git diff --check`. Treat remaining in-game acceptance items in `task.md` as release verification, not as silently completed work.

## Review loop

Repeat the following review until another pass finds no material improvement:

1. Check package boundaries, duplicated validation/messaging, command argument construction, translation parity, persistence safety, and documentation alignment.
2. Run focused tests, then the full test and build tasks.
3. Record completed work and genuinely pending acceptance checks in `task.md`.
4. Stop when further centralization would add indirection without removing a repeated responsibility or strengthening an invariant.

## Release workflow

- Set `mod_version` to the intended release version.
- Record all user-visible changes relative to the previous release in `CHANGELOG.md`, using the applicable Keep a Changelog categories such as `Added`, `Changed`, `Fixed`, and `Removed`.
- Run the complete verification workflow and confirm the produced JAR carries the intended version.
- Commit, tag, and push only when the user explicitly requests those repository actions.
