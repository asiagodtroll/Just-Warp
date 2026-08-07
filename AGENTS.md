# JustWarp Development Guide

## Principles

- Keep each class focused on one responsibility (SRP).
- Prefer the smallest readable solution (KISS), and centralize shared validation and messaging (DRY).
- Keep the mod server-side. Do not introduce client-only APIs, custom packets, or client installation requirements.
- Treat persisted JSON as user data: validate before replacing live state and never overwrite malformed files during reload.

## Architecture

- `command`: Brigadier registration and command-facing validation only.
- `domain`: immutable warp, group, location, and configuration values.
- `persistence`: JSON parsing, validation, default-file creation, and atomic writes.
- `service`: mutations, teleport policy, reload, and translations.
- `gui`: inventory rendering and click routing.

Commands and GUI must call the same service methods. Persistence code must not contain command, GUI, or teleport logic.

## Compatibility and verification

- Target Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.156.0+26.2, and Java 25.
- Persist item and dimension identifiers in namespaced form.
- Preserve JSON array order because it controls GUI order.
- Before completing a change, run `gradlew test` and `gradlew build` and keep `task.md` current.
